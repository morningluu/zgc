package com.example.zgc.repository;

import com.example.zgc.model.GrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GrowthRecordRepository extends JpaRepository<GrowthRecord, Long> {

    List<GrowthRecord> findAllByOrderByRecordDateDesc();

    // ✅ 新增这一行
    List<GrowthRecord> findByCategoryOrderByRecordDateDesc(String category);
}