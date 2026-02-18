package com.atsanalyzer.resume_ats_analyzer.service;

import com.atsanalyzer.resume_ats_analyzer.model.AnalysisResponse;
import com.atsanalyzer.resume_ats_analyzer.model.AnalysisHistory;
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

    public AnalysisResponse analyzeResume(MultipartFile file, String jobDescription, Long userId) {
        try {
            System.out.println("📥 AnalysisService: Starting analysis");

            // Extract text from PDF
            String resumeText = pdfParserService.extractText(file);
            System.out.println("📄 Extracted text length: " + resumeText.length());

            // Call Gemini AI
            System.out.println("🤖 Calling Gemini AI...");
            AnalysisResponse response = geminiAIService.analyzeResume(resumeText, jobDescription);

            // ✅ Extra info set करा
            response.setFileName(file.getOriginalFilename());
            response.setAnalyzedAt(new Date().toString());

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

    public List<AnalysisHistory> getUserHistory(Long userId) {
        return new ArrayList<>();
    }
}