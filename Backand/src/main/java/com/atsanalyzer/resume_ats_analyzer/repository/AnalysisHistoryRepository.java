package com.atsanalyzer.resume_ats_analyzer.repository;
import com.atsanalyzer.resume_ats_analyzer.model.AnalysisHistory;
import com.atsanalyzer.resume_ats_analyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findByUserOrderByAnalyzedAtDesc(User user);
    List<AnalysisHistory> findByUserIdOrderByAnalyzedAtDesc(Long userId);
}