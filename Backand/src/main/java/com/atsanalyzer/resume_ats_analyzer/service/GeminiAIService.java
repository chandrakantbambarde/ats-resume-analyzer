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
        System.out.println("🔵 ===== GEMINI AI SERVICE CALLED =====");
        System.out.println("📝 Resume Length: " + resumeText.length());
        System.out.println("📋 JD Length: " + jobDescription.length());

        String prompt = createPrompt(resumeText, jobDescription);
        String aiResponse = callGeminiAPI(prompt);
        return parseAIResponse(aiResponse, resumeText, jobDescription);
    }

    private String createPrompt(String resumeText, String jobDescription) {
        return String.format("""
            You are an expert ATS analyzer. Analyze resume STRICTLY against this job description only.

            STRICT RULES:
            - missingKeywords: ONLY keywords from JD that are NOT in resume
            - strongMatches: skills/keywords present in BOTH resume AND JD
            - suggestions: must be ROLE-SPECIFIC to this exact JD, not generic advice
            - skillsScore, experienceScore, educationScore: based on THIS JD only

            JOB DESCRIPTION:
            %s

            RESUME:
            %s

            Return ONLY valid JSON (no markdown, no backticks):
            {
                "atsScore": <0-100>,
                "missingKeywords": "comma,separated,JD,keywords,missing,in,resume",
                "strongMatches": ["matched skill 1", "matched skill 2", "matched skill 3"],
                "skillsScore": <0-100>,
                "experienceScore": <0-100>,
                "educationScore": <0-100>,
                "suggestions": ["role-specific tip 1", "role-specific tip 2", "role-specific tip 3", "role-specific tip 4", "role-specific tip 5"],
                "formatIssues": "specific issues or empty string",
                "overallRecommendation": "role-specific recommendation for this exact job"
            }
            """, jobDescription, resumeText);
    }

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

            System.out.println("✅ Score: " + response.getAtsScore() + " | Skills: " + response.getSkillsScore() + " | Exp: " + response.getExperienceScore());

        } catch (Exception e) {
            System.err.println("❌ Parse Error: " + e.getMessage());
            int score = calculateMockScore(resumeText, jobDescription);
            response.setAtsScore(score);
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
            response.setFormatIssues("Use standard ATS-friendly section headings");
            response.setOverallRecommendation("Resume scored " + score + "% against this job description. Add missing keywords to improve.");
        }
        return response;
    }

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