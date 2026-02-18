package com.atsanalyzer.resume_ats_analyzer.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "analysis_history")
public class AnalysisHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "job_description", length = 5000)
    private String jobDescription;
    
    @Column(name = "ats_score")
    private int atsScore;
    
    @Column(name = "missing_keywords", length = 2000)
    private String missingKeywords;
    
    @Column(name = "suggestions", length = 5000)
    private String suggestions;
    
    @Column(name = "format_issues", length = 2000)
    private String formatIssues;
    
    @Column(name = "recommendation", length = 2000)
    private String recommendation;
    
    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt = LocalDateTime.now();
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    
    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }
    
    public String getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(String missingKeywords) { this.missingKeywords = missingKeywords; }
    
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    
    public String getFormatIssues() { return formatIssues; }
    public void setFormatIssues(String formatIssues) { this.formatIssues = formatIssues; }
    
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}