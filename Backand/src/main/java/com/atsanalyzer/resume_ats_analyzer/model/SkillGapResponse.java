package com.atsanalyzer.resume_ats_analyzer.model;

import java.util.ArrayList;
import java.util.List;

public class SkillGapResponse {

    private String fileName;
    private int skillMatchPercentage;
    private int requiredMatchPercentage;
    private int preferredMatchPercentage;
    private int totalSkillsCount;
    private int matchedSkillsCount;
    private int missingSkillsCount;

    private int highPriorityCount;
    private int mediumPriorityCount;
    private int lowPriorityCount;

    private List<String> resumeSkills = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();
    private List<String> preferredSkills = new ArrayList<>();

    private List<MatchedSkill> matchedSkills = new ArrayList<>();
    private List<MissingSkill> missingSkills = new ArrayList<>();

    private List<String> personalizedRecommendations = new ArrayList<>();
    private List<SkillLearningPath> learningPaths = new ArrayList<>();

    public SkillGapResponse() {}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getSkillMatchPercentage() { return skillMatchPercentage; }
    public void setSkillMatchPercentage(int skillMatchPercentage) { this.skillMatchPercentage = skillMatchPercentage; }

    public int getRequiredMatchPercentage() { return requiredMatchPercentage; }
    public void setRequiredMatchPercentage(int requiredMatchPercentage) { this.requiredMatchPercentage = requiredMatchPercentage; }

    public int getPreferredMatchPercentage() { return preferredMatchPercentage; }
    public void setPreferredMatchPercentage(int preferredMatchPercentage) { this.preferredMatchPercentage = preferredMatchPercentage; }

    public int getTotalSkillsCount() { return totalSkillsCount; }
    public void setTotalSkillsCount(int totalSkillsCount) { this.totalSkillsCount = totalSkillsCount; }

    public int getMatchedSkillsCount() { return matchedSkillsCount; }
    public void setMatchedSkillsCount(int matchedSkillsCount) { this.matchedSkillsCount = matchedSkillsCount; }

    public int getMissingSkillsCount() { return missingSkillsCount; }
    public void setMissingSkillsCount(int missingSkillsCount) { this.missingSkillsCount = missingSkillsCount; }

    public int getHighPriorityCount() { return highPriorityCount; }
    public void setHighPriorityCount(int highPriorityCount) { this.highPriorityCount = highPriorityCount; }

    public int getMediumPriorityCount() { return mediumPriorityCount; }
    public void setMediumPriorityCount(int mediumPriorityCount) { this.mediumPriorityCount = mediumPriorityCount; }

    public int getLowPriorityCount() { return lowPriorityCount; }
    public void setLowPriorityCount(int lowPriorityCount) { this.lowPriorityCount = lowPriorityCount; }

    public List<String> getResumeSkills() { return resumeSkills; }
    public void setResumeSkills(List<String> resumeSkills) { this.resumeSkills = resumeSkills; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public List<MatchedSkill> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<MatchedSkill> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<MissingSkill> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<MissingSkill> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getPersonalizedRecommendations() { return personalizedRecommendations; }
    public void setPersonalizedRecommendations(List<String> personalizedRecommendations) { this.personalizedRecommendations = personalizedRecommendations; }

    public List<SkillLearningPath> getLearningPaths() { return learningPaths; }
    public void setLearningPaths(List<SkillLearningPath> learningPaths) { this.learningPaths = learningPaths; }

    // ===== Inner Classes =====

    public static class MatchedSkill {
        private String name;
        private String type; // "REQUIRED" or "PREFERRED"
        private String category;
        private String context;

        public MatchedSkill() {}

        public MatchedSkill(String name, String type, String category, String context) {
            this.name = name;
            this.type = type;
            this.category = category;
            this.context = context;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }

    public static class MissingSkill {
        private String name;
        private String priority; // "HIGH", "MEDIUM", "LOW"
        private String type; // "REQUIRED" or "PREFERRED"
        private String category;
        private String reason;

        public MissingSkill() {}

        public MissingSkill(String name, String priority, String type, String category, String reason) {
            this.name = name;
            this.priority = priority;
            this.type = type;
            this.category = category;
            this.reason = reason;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class SkillLearningPath {
        private String skill;
        private String priority;
        private String estimatedTime;
        private List<LearningResource> resources = new ArrayList<>();
        private List<PracticeProject> practiceProjects = new ArrayList<>();

        public SkillLearningPath() {}

        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

        public List<LearningResource> getResources() { return resources; }
        public void setResources(List<LearningResource> resources) { this.resources = resources; }

        public List<PracticeProject> getPracticeProjects() { return practiceProjects; }
        public void setPracticeProjects(List<PracticeProject> practiceProjects) { this.practiceProjects = practiceProjects; }
    }

    public static class LearningResource {
        private String title;
        private String type; // "Documentation", "Course", "Interactive Tutorial", "Certification"
        private String platform; // e.g. "Official Docs", "Coursera", "Udemy", "freeCodeCamp"
        private String url;
        private String description;

        public LearningResource() {}

        public LearningResource(String title, String type, String platform, String url, String description) {
            this.title = title;
            this.type = type;
            this.platform = platform;
            this.url = url;
            this.description = description;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class PracticeProject {
        private String title;
        private String description;
        private String difficulty; // "Beginner", "Intermediate", "Advanced"
        private List<String> keyDeliverables = new ArrayList<>();

        public PracticeProject() {}

        public PracticeProject(String title, String description, String difficulty, List<String> keyDeliverables) {
            this.title = title;
            this.description = description;
            this.difficulty = difficulty;
            this.keyDeliverables = keyDeliverables;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public List<String> getKeyDeliverables() { return keyDeliverables; }
        public void setKeyDeliverables(List<String> keyDeliverables) { this.keyDeliverables = keyDeliverables; }
    }
}
