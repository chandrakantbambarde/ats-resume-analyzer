package com.atsanalyzer.resume_ats_analyzer.model;

import java.util.List;

public class AnalysisResponse {

    private int atsScore;
    private String missingKeywords;
    private List<String> suggestions;
    private String formatIssues;
    private String overallRecommendation;

    //AnalysisService 
    private String fileName;
    private String analyzedAt;

    // New fields
    private List<String> strongMatches;
    private int skillsScore;
    private int experienceScore;
    private int educationScore;

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public String getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(String missingKeywords) { this.missingKeywords = missingKeywords; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getFormatIssues() { return formatIssues; }
    public void setFormatIssues(String formatIssues) { this.formatIssues = formatIssues; }

    public String getOverallRecommendation() { return overallRecommendation; }
    public void setOverallRecommendation(String overallRecommendation) { this.overallRecommendation = overallRecommendation; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(String analyzedAt) { this.analyzedAt = analyzedAt; }

    public List<String> getStrongMatches() { return strongMatches; }
    public void setStrongMatches(List<String> strongMatches) { this.strongMatches = strongMatches; }

    public int getSkillsScore() { return skillsScore; }
    public void setSkillsScore(int skillsScore) { this.skillsScore = skillsScore; }

    public int getExperienceScore() { return experienceScore; }
    public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }

    public int getEducationScore() { return educationScore; }
    public void setEducationScore(int educationScore) { this.educationScore = educationScore; }
}