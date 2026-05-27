package com.example.zgc.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "growth_records")
public class GrowthRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String category;
    
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "record_date")
    private LocalDate recordDate;
    
    // 身体数据字段
    @Column(name = "height_cm")
    private Double heightCm;
    
    @Column(name = "weight_kg")
    private Double weightKg;
    
    // 成绩字段
    private Double chinese;
    private Double math;
    private Double english;
    
    // 记录人
    @Column(name = "recorded_by")
    private String recordedBy;
    
    // 照片相关
    @Column(name = "photo_path")
    private String photoPath;
    
    private boolean featured = false;
    
    // 构造函数
    public GrowthRecord() {}
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public Double getHeightCm() {
        return heightCm;
    }
    
    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }
    
    public Double getWeightKg() {
        return weightKg;
    }
    
    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
    
    public Double getChinese() {
        return chinese;
    }
    
    public void setChinese(Double chinese) {
        this.chinese = chinese;
    }
    
    public Double getMath() {
        return math;
    }
    
    public void setMath(Double math) {
        this.math = math;
    }
    
    public Double getEnglish() {
        return english;
    }
    
    public void setEnglish(Double english) {
        this.english = english;
    }
    
    public String getRecordedBy() {
        return recordedBy;
    }
    
    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
