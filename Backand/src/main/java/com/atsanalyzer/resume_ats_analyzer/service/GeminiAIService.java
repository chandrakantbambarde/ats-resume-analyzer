package com.atsanalyzer.resume_ats_analyzer.service;

import com.atsanalyzer.resume_ats_analyzer.model.AnalysisResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AnalysisResponse analyzeResume(String resumeText, String jobDescription) {
        System.out.println("===== GEMINI AI SERVICE CALLED =====");
        String prompt = createPrompt(resumeText, jobDescription);
        String aiResponse = callGeminiAPI(prompt);
        return parseAIResponse(aiResponse, resumeText, jobDescription);
    }

    private String createPrompt(String resumeText, String jobDescription) {
        return """
            You are a strict ATS (Applicant Tracking System) resume analyzer. You must compare the RESUME against the JOB DESCRIPTION (JD) and return accurate, evidence-based results. Never give a generic or default score.
            
            STEP 1 - Extract from the JD:
            - Required hard skills, tools, technologies, certifications
            - Required years of experience and education
            - Important keywords and role-specific phrases
            
            STEP 2 - For EACH JD keyword/skill, search the RESUME text.
            - Count it as MATCHED only if it (or a clear synonym, e.g. "JS" = "JavaScript") appears in the resume.
            - Otherwise it is MISSING. Do NOT guess or assume skills the resume does not mention.
            
            STEP 3 - Score each category from 0 to 100:
            - keywordScore   = (matched JD keywords / total JD keywords) x 100
            - skillsScore    = (matched required skills / total required skills) x 100
            - experienceScore: compare resume experience (years, relevance, achievements) with JD requirement.
              Fully relevant = 80-100, partly relevant = 40-79, little or none = 0-39
            - educationScore: meets JD requirement = 80-100, partly = 40-79, not = 0-39
            - formatScore: 100 minus 10 per issue (no clear sections, tables/images, missing contact info, too long/short, no measurable achievements)
            
            STEP 4 - Final scores:
            - atsScore = keywordScore*0.40 + skillsScore*0.25 + experienceScore*0.20 + educationScore*0.10 + formatScore*0.05
            - jdMatchScore = (keywordScore + skillsScore) / 2
            - Round to whole numbers. Different resumes MUST produce different scores.
            
            RULES:
            - missingKeywords and jdMissingSkills must contain ONLY items that are in the JD and NOT in the resume.
            - strongMatches and jdMatchedSkills must contain ONLY items present in BOTH.
            - If the resume is unrelated to the JD, the score must be below 30.
            - Suggestions must be specific, e.g. "Add Docker experience to your projects section", not generic advice.
            - Return ONLY valid JSON. No markdown, no explanation, no code fences.
            
            JSON FORMAT:
            {
              "atsScore": 0,
              "jdMatchScore": 0,
              "missingKeywords": "keyword1, keyword2, keyword3",
              "strongMatches": ["..."],
              "jdMatchedSkills": ["..."],
              "jdMissingSkills": ["..."],
              "skillsGap": ["..."],
              "skillsScore": 0,
              "experienceScore": 0,
              "educationScore": 0,
              "suggestions": ["..."],
              "interviewTips": ["..."],
              "coverLetter": "...",
              "formatIssues": "...",
              "overallRecommendation": "..."
            }
            
            JOB DESCRIPTION:
            """ + jobDescription + """
            
            RESUME:
            """ + resumeText;
    }

    // callGeminiAPI() - same as before, no change needed
    private String callGeminiAPI(String prompt) {
        try {
            String url = apiUrl + "?key=" + apiKey;
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("✅ API Time: " + (System.currentTimeMillis() - start) + "ms | Status: " + response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            System.err.println("❌ Gemini API Failed: " + e.getMessage());
            return "{\"error\": \"AI service unavailable\"}";
        }
    }

    // ✅ UPDATED - navi fields parse keli
    private AnalysisResponse parseAIResponse(String aiResponse, String resumeText, String jobDescription) {
        AnalysisResponse response = new AnalysisResponse();
        try {
            JsonObject jsonResponse = JsonParser.parseString(aiResponse).getAsJsonObject();
            if (jsonResponse.has("error")) throw new Exception("API Error");

            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) throw new Exception("No candidates");

            String text = candidates.get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString()
                    .trim().replaceAll("```json", "").replaceAll("```", "").trim();

            JsonObject a = JsonParser.parseString(text).getAsJsonObject();

            // Existing fields
            response.setAtsScore(a.get("atsScore").getAsInt());
            response.setMissingKeywords(a.get("missingKeywords").getAsString());
            response.setFormatIssues(a.get("formatIssues").getAsString());
            response.setOverallRecommendation(a.get("overallRecommendation").getAsString());
            response.setSkillsScore(a.has("skillsScore") ? a.get("skillsScore").getAsInt() : 70);
            response.setExperienceScore(a.has("experienceScore") ? a.get("experienceScore").getAsInt() : 70);
            response.setEducationScore(a.has("educationScore") ? a.get("educationScore").getAsInt() : 70);

            if (a.has("strongMatches")) {
                List<String> matches = new ArrayList<>();
                a.getAsJsonArray("strongMatches").forEach(m -> matches.add(m.getAsString()));
                response.setStrongMatches(matches);
            }

            List<String> suggestions = new ArrayList<>();
            a.getAsJsonArray("suggestions").forEach(s -> suggestions.add(s.getAsString()));
            response.setSuggestions(suggestions);

            // ✅ NEW fields parse
            response.setJdMatchScore(a.has("jdMatchScore") ? a.get("jdMatchScore").getAsInt() : 0);
            response.setCoverLetter(a.has("coverLetter") ? a.get("coverLetter").getAsString() : "");

            if (a.has("interviewTips")) {
                List<String> tips = new ArrayList<>();
                a.getAsJsonArray("interviewTips").forEach(t -> tips.add(t.getAsString()));
                response.setInterviewTips(tips);
            }

            if (a.has("skillsGap")) {
                List<String> gap = new ArrayList<>();
                a.getAsJsonArray("skillsGap").forEach(g -> gap.add(g.getAsString()));
                response.setSkillsGap(gap);
            }

            if (a.has("jdMatchedSkills")) {
                List<String> matched = new ArrayList<>();
                a.getAsJsonArray("jdMatchedSkills").forEach(m -> matched.add(m.getAsString()));
                response.setJdMatchedSkills(matched);
            }

            if (a.has("jdMissingSkills")) {
                List<String> missing = new ArrayList<>();
                a.getAsJsonArray("jdMissingSkills").forEach(m -> missing.add(m.getAsString()));
                response.setJdMissingSkills(missing);
            }

            System.out.println("✅ Score: " + response.getAtsScore() + " | JD Match: " + response.getJdMatchScore());

        } catch (Exception e) {
            System.err.println("❌ Parse Error: " + e.getMessage());
            // Fallback - existing mock logic same rahil
            int score = calculateMockScore(resumeText, jobDescription);
            response.setAtsScore(score);
            response.setJdMatchScore(Math.max(40, score - 15));
            response.setMissingKeywords(extractMissingKeywords(resumeText, jobDescription));
            response.setStrongMatches(extractStrongMatches(resumeText, jobDescription));
            response.setSkillsScore(Math.max(50, score - 10));
            response.setExperienceScore(Math.max(50, score - 5));
            response.setEducationScore(Math.max(60, score));
            response.setSuggestions(Arrays.asList(
                "Add missing keywords from the job description to your skills section",
                "Quantify your achievements with specific numbers and percentages",
                "Tailor your resume summary to match this specific role",
                "Highlight relevant projects that match the job requirements",
                "Use action verbs and industry-specific terminology from the JD"
            ));
            response.setInterviewTips(Arrays.asList(
                "Tell me about your most relevant project for this role?",
                "How do you handle tight deadlines and pressure?",
                "Describe a challenging technical problem you solved recently.",
                "How do you stay updated with new technologies?",
                "Where do you see yourself in 3 years in this field?"
            ));
            response.setSkillsGap(Arrays.asList(
                "Review JD carefully and add missing technical keywords",
                "Consider learning cloud technologies if not already present",
                "Add testing frameworks experience to your resume"
            ));
            response.setCoverLetter("Dear Hiring Manager,\n\nI am excited to apply for this position...\n\nMy experience aligns well with your requirements...\n\nI look forward to discussing this opportunity.\n\nSincerely,\n[Your Name]");
            response.setFormatIssues("Use standard ATS-friendly section headings");
            response.setOverallRecommendation("Resume scored " + score + "% against this job description.");
        }
        return response;
    }

    // Existing helper methods - same as before
    private String extractMissingKeywords(String resumeText, String jobDescription) {
        List<String> keywords = Arrays.asList("Java","Python","JavaScript","TypeScript","SQL","MySQL",
            "PostgreSQL","MongoDB","AWS","Azure","GCP","Docker","Kubernetes","Spring","Spring Boot",
            "React","Angular","Node.js","Git","REST","API","Microservices","Linux","Agile","Scrum",
            "CI/CD","Jenkins","Maven","Gradle","Hibernate","Redis","Kafka","GraphQL","C++","C#",
            ".NET","PHP","Ruby","Go","Kotlin","Swift","Flutter","HTML","CSS");
        String jdL = jobDescription.toLowerCase(), resumeL = resumeText.toLowerCase();
        List<String> missing = new ArrayList<>();
        for (String kw : keywords)
            if (jdL.contains(kw.toLowerCase()) && !resumeL.contains(kw.toLowerCase()))
                missing.add(kw);
        return missing.isEmpty() ? "" : String.join(", ", missing);
    }

    private List<String> extractStrongMatches(String resumeText, String jobDescription) {
        List<String> keywords = Arrays.asList("Java","Python","JavaScript","TypeScript","SQL","MySQL",
            "MongoDB","AWS","Docker","Spring","Spring Boot","React","Angular","Node.js","Git",
            "REST","API","Microservices","Linux","Agile","Scrum","Maven","Hibernate","C++","HTML","CSS");
        String jdL = jobDescription.toLowerCase(), resumeL = resumeText.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String kw : keywords) {
            if (jdL.contains(kw.toLowerCase()) && resumeL.contains(kw.toLowerCase())) {
                matches.add(kw);
                if (matches.size() >= 8) break;
            }
        }
        return matches;
    }

    private int calculateMockScore(String resumeText, String jobDescription) {
        return Math.min(95, 60 + Math.min(25, jobDescription.length() / 100) + Math.min(15, resumeText.length() / 200));
    }
}