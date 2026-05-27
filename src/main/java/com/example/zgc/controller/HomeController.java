package com.example.zgc.controller;

import com.example.zgc.model.GrowthRecord;
import com.example.zgc.service.GrowthRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class HomeController {

    private final GrowthRecordService recordService;

    public HomeController(GrowthRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<GrowthRecord> records = recordService.findAllByOrderByRecordDateDesc();
        model.addAttribute("records", records);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        List<GrowthRecord> records = recordService.findByCategory("gallery");
        model.addAttribute("records", records);
        return "gallery";
    }

    @PostMapping("/gallery/add")
    @ResponseBody
    public Map<String, Object> addGallery(@RequestParam String title,
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

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("photoPath", record.getPhotoPath());
        result.put("recordDate", record.getRecordDate().toString());
        result.put("title", record.getTitle());
        result.put("description", record.getDescription());
        result.put("featured", record.isFeatured());
        return result;
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

    @PostMapping("/delete/{id}")
    @ResponseBody
    public String deleteRecord(@PathVariable Long id) {
        recordService.delete(id);
        return "ok";
    }

    @PostMapping("/deleteScore/{id}")
    @ResponseBody
    public String deleteScoreRecord(@PathVariable Long id) {
        recordService.delete(id);
        return "ok";
    }

    private String savePhoto(MultipartFile photo) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        File dest = new File(uploadDir + File.separator + fileName);
        photo.transferTo(dest);
        return "/uploads/" + fileName;
    }
}
