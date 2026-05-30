package com.example.zgc.service;

import com.example.zgc.model.GrowthRecord;
import com.example.zgc.repository.GrowthRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GrowthRecordService {

    private final GrowthRecordRepository repository;

    public GrowthRecordService(GrowthRecordRepository repository) {
        this.repository = repository;
    }

    public GrowthRecord save(GrowthRecord record) {
        return repository.save(record);
    }

    public List<GrowthRecord> findAllByOrderByRecordDateDesc() {
        return repository.findAllByOrderByRecordDateDesc();
    }

    public List<GrowthRecord> findByCategory(String category) {
        return repository.findByCategoryOrderByRecordDateDesc(category);
    }

    public GrowthRecord findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
