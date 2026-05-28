package com.example.zgc.controller;

import com.example.zgc.model.GrowthRecord;
import com.example.zgc.service.GrowthRecordService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class HomeController {

    private final GrowthRecordService recordService;
    private static final String AVATAR_FILE_NAME = "baby-avatar";

    public HomeController(GrowthRecordService recordService) {
        this.recordService = recordService;
    }

    // ==================== 首页 ====================
    @GetMapping("/")
    public String home(Model model) {
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

    // ==================== 头像相关接口 ====================
    /**
     * 上传头像
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

        // 校验文件类型（简单校验，可增强）
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

        // 获取扩展名
        String originalFilename = avatar.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFileName = AVATAR_FILE_NAME + extension;
        File dest = new File(uploadDir + File.separator + newFileName);
        avatar.transferTo(dest);

        result.put("success", true);
        result.put("avatarPath", "/uploads/" + newFileName);
        return result;
    }

    /**
     * 获取最新头像（供前端轮询/加载）
     * 返回图片二进制流，404时前端自动降级为本地缓存或默认头像
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

    /**
     * 添加相册照片（已改为重定向，避免直接显示JSON）
     */
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
        // 删除前清理物理文件
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
        // 清理文件名，防止路径穿越
        String originalFilename = photo.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "photo.jpg";
        }
        // 只保留安全的字符
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String fileName = UUID.randomUUID() + "_" + safeName;
        File dest = new File(uploadDir + File.separator + fileName);
        photo.transferTo(dest);
        return "/uploads/" + fileName;
    }
}
