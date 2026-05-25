package com.example.zgc.controller;

import com.example.zgc.model.GrowthRecord;
import com.example.zgc.service.FileStorageService;
import com.example.zgc.service.GrowthRecordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final GrowthRecordService recordService;
    private final FileStorageService fileStorageService;

    public AdminController(GrowthRecordService recordService, FileStorageService fileStorageService) {
        this.recordService = recordService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("records", recordService.findAllByOrderByRecordDateDesc());
        return "admin-upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam("recordDate") String recordDate,
                               @RequestParam("photo") MultipartFile photo) {
        GrowthRecord record = new GrowthRecord();
        record.setTitle(title);
        record.setDescription(description);
        record.setRecordDate(LocalDate.parse(recordDate));

        if (!photo.isEmpty()) {
            String photoPath = fileStorageService.saveFile(photo);
            record.setPhotoPath(photoPath);
        }

        recordService.save(record);
        return "redirect:/admin/upload";
    }

    @GetMapping("/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        recordService.delete(id);
        return "redirect:/admin/upload";
    }
}