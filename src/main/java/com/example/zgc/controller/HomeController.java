package com.example.zgc.controller;

import com.example.zgc.model.GrowthRecord;
import com.example.zgc.service.GrowthRecordService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class HomeController {

    private final GrowthRecordService recordService;
    private static final String AVATAR_FILE_NAME = "baby-avatar";

    public HomeController(GrowthRecordService recordService) {
        this.recordService = recordService;
    }

    // ==================== 首页（含开屏动画） ====================
    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(value = "standalone", required = false) String standalone,
                       @RequestParam(value = "loaded", required = false) String loaded) {

        // 如果是 PWA 启动（standalone=true）或过渡页跳转（loaded=true），直接显示首页内容
        // 不显示过渡动画，因为动画在 splash.html 里已经播放过了
        boolean skipSplash = "true".equals(standalone) || "true".equals(loaded);

        // 如果是浏览器直接访问（无任何参数），则显示过渡动画页
        if (!skipSplash) {
            return "splash";
        }

        // ====== 以下是首页的正常数据加载 ======
        List<GrowthRecord> records = recordService.findAllByOrderByRecordDateDesc();

        // 计算最新的身高和体重
        Double latestHeight = null;
        Double latestWeight = null;
        LocalDate latestHeightDate = null;
        LocalDate latestWeightDate = null;

        for (GrowthRecord record : records) {
            if (record.getHeightCm() != null) {
                if (latestHeight == null ||
                        (record.getRecordDate() != null &&
                                (latestHeightDate == null || record.getRecordDate().isAfter(latestHeightDate)))) {
                    latestHeight = record.getHeightCm();
                    latestHeightDate = record.getRecordDate();
                }
            }
            if (record.getWeightKg() != null) {
                if (latestWeight == null ||
                        (record.getRecordDate() != null &&
                                (latestWeightDate == null || record.getRecordDate().isAfter(latestWeightDate)))) {
                    latestWeight = record.getWeightKg();
                    latestWeightDate = record.getRecordDate();
                }
            }
        }

        model.addAttribute("records", records);
        model.addAttribute("latestHeight", latestHeight);
        model.addAttribute("latestWeight", latestWeight);

        // 传递头像路径（可选，前端已用 /api/avatar 接口动态加载，此处保留兼容性）
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        String avatarPath = null;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith(AVATAR_FILE_NAME + "."));
            if (files != null && files.length > 0) {
                avatarPath = "/uploads/" + files[0].getName();
            }
        }
        model.addAttribute("babyAvatarPath", avatarPath);

        return "index";
    }

    // ==================== 过渡页（PWA 启动或浏览器首次访问时显示） ====================
    @GetMapping("/splash.html")
    public String splash() {
        return "splash";
    }

    // ==================== 头像相关接口 ====================
    /**
     * 上传头像（自动压缩到 300px 宽，统一 JPG 格式）
     */
    @PostMapping("/api/avatar/upload")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (avatar.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空");
            return result;
        }

        // 校验文件类型
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("success", false);
            result.put("message", "只允许上传图片文件");
            return result;
        }

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 删除所有旧头像文件
        File[] oldFiles = dir.listFiles((d, name) -> name.startsWith(AVATAR_FILE_NAME + "."));
        if (oldFiles != null) {
            for (File f : oldFiles) {
                f.delete();
            }
        }

        // 压缩图片并保存（如果压缩失败则直接保存原图）
        String newFileName = AVATAR_FILE_NAME + ".jpg";
        File dest = new File(uploadDir + File.separator + newFileName);

        try {
            // 读取原图
            BufferedImage originalImage = ImageIO.read(avatar.getInputStream());
            if (originalImage == null) {
                throw new IOException("无法解析图片文件");
            }

            // 缩放至最大宽度 300px（保持比例）
            int maxWidth = 300;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            if (width > maxWidth) {
                height = (int) ((double) height * maxWidth / width);
                width = maxWidth;
            }

            // 创建缩放后的图片
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            // 写入临时文件（避免直接覆盖可能失败）
            File tempFile = File.createTempFile("avatar", ".jpg");
            ImageIO.write(resizedImage, "jpg", tempFile);
            Files.copy(tempFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.delete();

        } catch (Exception e) {
            // 压缩失败时，直接保存原文件（保留原始扩展名）
            String originalFilename = avatar.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            newFileName = AVATAR_FILE_NAME + extension;
            dest = new File(uploadDir + File.separator + newFileName);
            avatar.transferTo(dest);
        }

        result.put("success", true);
        result.put("avatarPath", "/uploads/" + newFileName);
        return result;
    }

    /**
     * 获取最新头像（带浏览器缓存，1天内不重复下载）
     */
    @GetMapping("/api/avatar")
    @ResponseBody
    public ResponseEntity<UrlResource> getAvatar() throws MalformedURLException, IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith(AVATAR_FILE_NAME + "."));
            if (files != null && files.length > 0) {
                File avatarFile = files[0];
                Path path = avatarFile.toPath();
                UrlResource resource = new UrlResource(path.toUri());
                if (resource.exists() && resource.isReadable()) {
                    String mimeType = Files.probeContentType(path);
                    if (mimeType == null) {
                        mimeType = "image/jpeg";
                    }
                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                            .contentType(MediaType.parseMediaType(mimeType))
                            .body(resource);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== 登录页面 ====================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ==================== 相册 ====================
    @GetMapping("/gallery")
    public String gallery(Model model) {
        List<GrowthRecord> records = recordService.findByCategory("gallery");
        model.addAttribute("records", records);
        return "gallery";
    }

    @PostMapping("/gallery/add")
    public String addGallery(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String recordDate,
                             @RequestParam(required = false) MultipartFile photo) throws IOException {
        GrowthRecord record = new GrowthRecord();
        record.setCategory("gallery");
        record.setTitle(title);
        record.setDescription(description);
        record.setRecordDate(LocalDate.parse(recordDate));
        if (photo != null && !photo.isEmpty()) {
            record.setPhotoPath(savePhoto(photo));
        }
        recordService.save(record);
        return "redirect:/gallery";
    }

    @PostMapping("/gallery/toggle-featured/{id}")
    @ResponseBody
    public String toggleFeatured(@PathVariable Long id) {
        GrowthRecord record = recordService.findById(id);
        if (record != null) {
            record.setFeatured(!record.isFeatured());
            recordService.save(record);
            return "ok";
        }
        return "error";
    }

    // ==================== 成长数据 ====================
    @GetMapping("/growth")
    public String growth(Model model) {
        List<GrowthRecord> records = recordService.findByCategory("growth");
        List<GrowthRecord> scoreRecords = recordService.findByCategory("score");
        model.addAttribute("records", records);
        model.addAttribute("scoreRecords", scoreRecords);
        return "growth";
    }

    @PostMapping("/growth/add")
    public String addGrowth(@RequestParam String title,
                            @RequestParam String recordDate,
                            @RequestParam(required = false) Double heightCm,
                            @RequestParam(required = false) Double weightKg,
                            @RequestParam String recordedBy) {
        GrowthRecord record = new GrowthRecord();
        record.setCategory("growth");
        record.setTitle(title);
        record.setRecordDate(LocalDate.parse(recordDate));
        record.setHeightCm(heightCm);
        record.setWeightKg(weightKg);
        record.setRecordedBy(recordedBy);
        recordService.save(record);
        return "redirect:/growth";
    }

    @PostMapping("/growth/addScore")
    public String addScore(@RequestParam String title,
                           @RequestParam String recordDate,
                           @RequestParam(required = false) Double chinese,
                           @RequestParam(required = false) Double math,
                           @RequestParam(required = false) Double english,
                           @RequestParam String recordedBy) {
        GrowthRecord record = new GrowthRecord();
        record.setCategory("score");
        record.setTitle(title);
        record.setRecordDate(LocalDate.parse(recordDate));
        record.setChinese(chinese);
        record.setMath(math);
        record.setEnglish(english);
        record.setRecordedBy(recordedBy);
        recordService.save(record);
        return "redirect:/growth";
    }

    // ==================== 日记 ====================
    @GetMapping("/diary")
    public String diary(Model model) {
        List<GrowthRecord> records = recordService.findByCategory("diary");
        model.addAttribute("records", records);
        return "diary";
    }

    @PostMapping("/diary/add")
    public String addDiary(@RequestParam String title,
                           @RequestParam String description,
                           @RequestParam String recordDate) {
        GrowthRecord record = new GrowthRecord();
        record.setCategory("diary");
        record.setTitle(title);
        record.setDescription(description);
        record.setRecordDate(LocalDate.parse(recordDate));
        recordService.save(record);
        return "redirect:/diary";
    }

    // ==================== 留言 ====================
    @GetMapping("/messages")
    public String messages(Model model) {
        List<GrowthRecord> records = recordService.findByCategory("messages");
        model.addAttribute("records", records);
        return "messages";
    }

    @PostMapping("/messages/add")
    public String addMessage(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String recordDate) {
        GrowthRecord record = new GrowthRecord();
        record.setCategory("messages");
        record.setTitle(title);
        record.setDescription(description);
        record.setRecordDate(LocalDate.parse(recordDate));
        recordService.save(record);
        return "redirect:/messages";
    }

    // ==================== 删除记录 ====================
    @PostMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id,
                               @RequestHeader(value = "Referer", defaultValue = "/") String referer) {
        GrowthRecord record = recordService.findById(id);
        if (record != null && record.getPhotoPath() != null) {
            String filePath = System.getProperty("user.dir") + record.getPhotoPath();
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // 日志记录，不影响删除流程
            }
        }
        recordService.delete(id);
        return "redirect:" + referer;
    }

    @PostMapping("/deleteScore/{id}")
    public String deleteScoreRecord(@PathVariable Long id) {
        GrowthRecord record = recordService.findById(id);
        if (record != null && record.getPhotoPath() != null) {
            String filePath = System.getProperty("user.dir") + record.getPhotoPath();
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // 忽略
            }
        }
        recordService.delete(id);
        return "redirect:/growth";
    }

    // ==================== 工具方法 ====================
    private String savePhoto(MultipartFile photo) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String originalFilename = photo.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "photo.jpg";
        }
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String fileName = UUID.randomUUID() + "_" + safeName;
        File dest = new File(uploadDir + File.separator + fileName);
        photo.transferTo(dest);
        return "/uploads/" + fileName;
    }
}
