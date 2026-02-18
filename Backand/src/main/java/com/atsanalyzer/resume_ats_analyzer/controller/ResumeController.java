package com.atsanalyzer.resume_ats_analyzer.controller;

import com.atsanalyzer.resume_ats_analyzer.model.AnalysisResponse;
import com.atsanalyzer.resume_ats_analyzer.model.AnalysisHistory;
import com.atsanalyzer.resume_ats_analyzer.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:4200")
public class ResumeController {
    
    @Autowired
    private AnalysisService analysisService;
    
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // ✅ Fixed: Passing 3 parameters
        AnalysisResponse response = analysisService.analyzeResume(file, jobDescription, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<AnalysisHistory>> getUserHistory(@PathVariable Long userId) {
        List<AnalysisHistory> history = analysisService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }
}