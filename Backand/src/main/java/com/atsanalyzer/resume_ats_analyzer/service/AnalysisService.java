package com.atsanalyzer.resume_ats_analyzer.service;

import com.atsanalyzer.resume_ats_analyzer.model.AnalysisResponse;
import com.atsanalyzer.resume_ats_analyzer.model.AnalysisHistory;
import com.atsanalyzer.resume_ats_analyzer.model.User;
import com.atsanalyzer.resume_ats_analyzer.repository.AnalysisHistoryRepository;
import com.atsanalyzer.resume_ats_analyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AnalysisService {

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private AnalysisHistoryRepository historyRepository;  // ✅ NEW

    @Autowired
    private UserRepository userRepository;  // ✅ NEW

    public AnalysisResponse analyzeResume(MultipartFile file, String jobDescription, Long userId) {
        try {
            System.out.println("📥 AnalysisService: Starting analysis");

            String resumeText = pdfParserService.extractText(file);
            System.out.println("📄 Extracted text length: " + resumeText.length());

            System.out.println("🤖 Calling Gemini AI...");
            AnalysisResponse response = geminiAIService.analyzeResume(resumeText, jobDescription);

            response.setFileName(file.getOriginalFilename());
            response.setAnalyzedAt(new Date().toString());

            // ✅ NEW - History DB madhe save kara
            if (userId != null) {
                saveHistory(response, jobDescription, userId);
            }

            System.out.println("✅ Analysis complete - Score: " + response.getAtsScore());
            return response;

        } catch (Exception e) {
            System.err.println("❌ Analysis failed: " + e.getMessage());
            e.printStackTrace();
            AnalysisResponse error = new AnalysisResponse();
            error.setAtsScore(0);
            error.setOverallRecommendation("Error: " + e.getMessage());
            return error;
        }
    }

    // ✅ NEW - DB madhe save
    private void saveHistory(AnalysisResponse response, String jobDescription, Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return;

            AnalysisHistory history = new AnalysisHistory();
            history.setUser(userOpt.get());
            history.setFileName(response.getFileName());
            history.setJobDescription(jobDescription);
            history.setAtsScore(response.getAtsScore());
            history.setMissingKeywords(response.getMissingKeywords());
            history.setSuggestions(response.getSuggestions() != null ? String.join("|", response.getSuggestions()) : "");
            history.setFormatIssues(response.getFormatIssues());
            history.setRecommendation(response.getOverallRecommendation());
            history.setCoverLetter(response.getCoverLetter());
            history.setInterviewTips(response.getInterviewTips() != null ? String.join("|", response.getInterviewTips()) : "");
            history.setSkillsGap(response.getSkillsGap() != null ? String.join("|", response.getSkillsGap()) : "");
            history.setJdMatchScore(response.getJdMatchScore());

            historyRepository.save(history);
            System.out.println("✅ History saved for userId: " + userId);
        } catch (Exception e) {
            System.err.println("⚠️ History save failed (non-critical): " + e.getMessage());
        }
    }

    // ✅ FIXED - actually DB varun data yeto aata
    public List<AnalysisHistory> getUserHistory(Long userId) {
        return historyRepository.findByUserIdOrderByAnalyzedAtDesc(userId);
    }
}