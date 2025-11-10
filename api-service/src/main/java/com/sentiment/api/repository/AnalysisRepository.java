package com.sentiment.api.repository;

import com.sentiment.api.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findTop10ByOrderByCreatedAtDesc();
}