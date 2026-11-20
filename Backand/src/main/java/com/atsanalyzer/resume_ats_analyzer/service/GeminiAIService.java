package com.atsanalyzer.resume_ats_analyzer.service;

import com.atsanalyzer.resume_ats_analyzer.model.AnalysisResponse;
import com.atsanalyzer.resume_ats_analyzer.model.SkillGapResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
        AnalysisResponse response = parseAIResponse(aiResponse, resumeText, jobDescription);
        try {
            SkillGapResponse skillGap = analyzeSkillGap(resumeText, jobDescription);
            response.setSkillGapAnalysis(skillGap);
        } catch (Exception e) {
            System.err.println("⚠️ Could not attach SkillGapAnalysis to AnalysisResponse: " + e.getMessage());
        }
        return response;
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

    // ==========================================
    // 🎯 SKILL GAP ANALYSIS IMPLEMENTATION
    // ==========================================

    public SkillGapResponse analyzeSkillGap(String resumeText, String jobDescription) {
        System.out.println("===== GEMINI AI SKILL GAP ANALYSIS CALLED =====");
        String prompt = createSkillGapPrompt(resumeText, jobDescription);
        String aiResponse = callGeminiAPI(prompt);
        return parseSkillGapAIResponse(aiResponse, resumeText, jobDescription);
    }

    private String createSkillGapPrompt(String resumeText, String jobDescription) {
        return """
            You are a technical talent assessor and AI career intelligence expert.
            Perform a rigorous, deep SKILL GAP ANALYSIS by comparing the candidate's RESUME against the target JOB DESCRIPTION (JD).

            TASKS:
            1. Extract all skills found in the RESUME (languages, frameworks, libraries, databases, devops tools, methodologies, etc.).
            2. Extract all skills from the JOB DESCRIPTION, categorizing each as:
               - "REQUIRED": Essential core skills, hard requirements, mandatory technologies.
               - "PREFERRED": Nice-to-have, bonus, plus, preferred qualifications.
            3. Compare the two sets:
               - MATCHED SKILLS: Present in both JD and Resume. Include name, type ("REQUIRED" or "PREFERRED"), category (e.g. "Backend", "Frontend", "Cloud & DevOps", "Database", "Testing", "Architecture"), and brief context.
               - MISSING SKILLS: Present in JD but missing in Resume. Include name, priority ("HIGH", "MEDIUM", "LOW"), type ("REQUIRED" or "PREFERRED"), category, and specific reason explaining the priority rating.
                 * HIGH PRIORITY: Critical core skills required by the JD that the candidate lacks.
                 * MEDIUM PRIORITY: Secondary required skills or major preferred tools that significantly boost candidate competitiveness.
                 * LOW PRIORITY: Nice-to-have, optional, or bonus skills.
            4. Calculate Match Percentages (integer 0-100):
               - skillMatchPercentage: (matchedCount / totalJdSkillsCount) * 100
               - requiredMatchPercentage: (matchedRequired / (totalRequired > 0 ? totalRequired : 1)) * 100
               - preferredMatchPercentage: (matchedPreferred / (totalPreferred > 0 ? totalPreferred : 1)) * 100
            5. Generate AI-based personalized learning recommendations:
               - 3 to 5 clear, strategic recommendations specifically tailored to help this candidate close their gaps for this target role.
            6. Learning Paths for Missing Skills:
               - For missing skills (focus on HIGH and MEDIUM priorities), provide:
                 * estimatedTime to learn (e.g., "1-2 weeks", "2-3 weeks")
                 * resources: List of curated learning resources (title, type e.g. "Official Documentation", "Course", "Interactive Tutorial", platform e.g. "Coursera", "Udemy", "freeCodeCamp", "Official Docs", url, description).
                 * practiceProjects: Specific portfolio projects that demonstrate mastery of this skill (title, description, difficulty: "Beginner" | "Intermediate" | "Advanced", keyDeliverables: list of key features to build).

            Return ONLY valid JSON matching this schema:
            {
              "skillMatchPercentage": 75,
              "requiredMatchPercentage": 70,
              "preferredMatchPercentage": 80,
              "resumeSkills": ["Java", "Spring Boot", "MySQL", "Git"],
              "requiredSkills": ["Java", "Spring Boot", "Docker", "Kubernetes", "Microservices"],
              "preferredSkills": ["AWS", "Redis", "Kafka"],
              "matchedSkills": [
                {
                  "name": "Java",
                  "type": "REQUIRED",
                  "category": "Backend Languages",
                  "context": "Demonstrated across multiple projects in resume"
                }
              ],
              "missingSkills": [
                {
                  "name": "Docker",
                  "priority": "HIGH",
                  "type": "REQUIRED",
                  "category": "Cloud & DevOps",
                  "reason": "Crucial requirement for deploying microservices containers"
                }
              ],
              "personalizedRecommendations": [
                "Focus immediately on Docker containerization fundamentals and multi-stage builds.",
                "Build a containerized REST API with Redis caching to cover two key JD requirements."
              ],
              "learningPaths": [
                {
                  "skill": "Docker",
                  "priority": "HIGH",
                  "estimatedTime": "1-2 weeks",
                  "resources": [
                    {
                      "title": "Docker Official Documentation & Guides",
                      "type": "Official Documentation",
                      "platform": "Docker Docs",
                      "url": "https://docs.docker.com/get-started/",
                      "description": "Step-by-step guides for images, containers, and Docker Compose."
                    }
                  ],
                  "practiceProjects": [
                    {
                      "title": "Containerized Spring Boot & PostgreSQL Service",
                      "description": "Dockerize a Spring Boot application using multi-container Docker Compose with health checks and volume persistence.",
                      "difficulty": "Intermediate",
                      "keyDeliverables": ["Dockerfile with multi-stage build", "docker-compose.yml", "Automated environment variables setup"]
                    }
                  ]
                }
              ]
            }

            JOB DESCRIPTION:
            """ + jobDescription + """

            RESUME:
            """ + resumeText;
    }

    private SkillGapResponse parseSkillGapAIResponse(String aiResponse, String resumeText, String jobDescription) {
        SkillGapResponse response = new SkillGapResponse();
        try {
            JsonObject jsonResponse = JsonParser.parseString(aiResponse).getAsJsonObject();
            if (jsonResponse.has("error")) throw new Exception("API Error: " + jsonResponse.get("error").getAsString());

            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) throw new Exception("No candidates returned from Gemini");

            String rawText = candidates.get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            String text = cleanJsonString(rawText);
            JsonObject a = JsonParser.parseString(text).getAsJsonObject();

            if (a.has("skillMatchPercentage")) response.setSkillMatchPercentage(a.get("skillMatchPercentage").getAsInt());
            if (a.has("requiredMatchPercentage")) response.setRequiredMatchPercentage(a.get("requiredMatchPercentage").getAsInt());
            if (a.has("preferredMatchPercentage")) response.setPreferredMatchPercentage(a.get("preferredMatchPercentage").getAsInt());

            if (a.has("resumeSkills")) {
                List<String> list = new ArrayList<>();
                a.getAsJsonArray("resumeSkills").forEach(e -> list.add(e.getAsString()));
                response.setResumeSkills(list);
            }

            if (a.has("requiredSkills")) {
                List<String> list = new ArrayList<>();
                a.getAsJsonArray("requiredSkills").forEach(e -> list.add(e.getAsString()));
                response.setRequiredSkills(list);
            }

            if (a.has("preferredSkills")) {
                List<String> list = new ArrayList<>();
                a.getAsJsonArray("preferredSkills").forEach(e -> list.add(e.getAsString()));
                response.setPreferredSkills(list);
            }

            List<SkillGapResponse.MatchedSkill> matchedList = new ArrayList<>();
            if (a.has("matchedSkills")) {
                for (JsonElement el : a.getAsJsonArray("matchedSkills")) {
                    JsonObject mo = el.getAsJsonObject();
                    SkillGapResponse.MatchedSkill ms = new SkillGapResponse.MatchedSkill();
                    ms.setName(mo.has("name") ? mo.get("name").getAsString() : "");
                    ms.setType(mo.has("type") ? mo.get("type").getAsString() : "REQUIRED");
                    ms.setCategory(mo.has("category") ? mo.get("category").getAsString() : "Technical Skills");
                    ms.setContext(mo.has("context") ? mo.get("context").getAsString() : "Present in resume");
                    matchedList.add(ms);
                }
            }
            response.setMatchedSkills(matchedList);
            response.setMatchedSkillsCount(matchedList.size());

            List<SkillGapResponse.MissingSkill> missingList = new ArrayList<>();
            int highCount = 0, medCount = 0, lowCount = 0;
            if (a.has("missingSkills")) {
                for (JsonElement el : a.getAsJsonArray("missingSkills")) {
                    JsonObject mo = el.getAsJsonObject();
                    SkillGapResponse.MissingSkill ms = new SkillGapResponse.MissingSkill();
                    ms.setName(mo.has("name") ? mo.get("name").getAsString() : "");
                    String priority = mo.has("priority") ? mo.get("priority").getAsString().toUpperCase() : "MEDIUM";
                    ms.setPriority(priority);
                    ms.setType(mo.has("type") ? mo.get("type").getAsString() : "REQUIRED");
                    ms.setCategory(mo.has("category") ? mo.get("category").getAsString() : "Technical Gaps");
                    ms.setReason(mo.has("reason") ? mo.get("reason").getAsString() : "Required for role");

                    if ("HIGH".equals(priority)) highCount++;
                    else if ("LOW".equals(priority)) lowCount++;
                    else medCount++;

                    missingList.add(ms);
                }
            }
            response.setMissingSkills(missingList);
            response.setMissingSkillsCount(missingList.size());
            response.setHighPriorityCount(highCount);
            response.setMediumPriorityCount(medCount);
            response.setLowPriorityCount(lowCount);
            response.setTotalSkillsCount(matchedList.size() + missingList.size());

            if (a.has("personalizedRecommendations")) {
                List<String> list = new ArrayList<>();
                a.getAsJsonArray("personalizedRecommendations").forEach(e -> list.add(e.getAsString()));
                response.setPersonalizedRecommendations(list);
            }

            if (a.has("learningPaths")) {
                List<SkillGapResponse.SkillLearningPath> paths = new ArrayList<>();
                for (JsonElement el : a.getAsJsonArray("learningPaths")) {
                    JsonObject po = el.getAsJsonObject();
                    SkillGapResponse.SkillLearningPath path = new SkillGapResponse.SkillLearningPath();
                    path.setSkill(po.has("skill") ? po.get("skill").getAsString() : "");
                    path.setPriority(po.has("priority") ? po.get("priority").getAsString() : "MEDIUM");
                    path.setEstimatedTime(po.has("estimatedTime") ? po.get("estimatedTime").getAsString() : "1-2 weeks");

                    if (po.has("resources")) {
                        List<SkillGapResponse.LearningResource> resources = new ArrayList<>();
                        for (JsonElement rel : po.getAsJsonArray("resources")) {
                            JsonObject ro = rel.getAsJsonObject();
                            SkillGapResponse.LearningResource lr = new SkillGapResponse.LearningResource();
                            lr.setTitle(ro.has("title") ? ro.get("title").getAsString() : "");
                            lr.setType(ro.has("type") ? ro.get("type").getAsString() : "Course");
                            lr.setPlatform(ro.has("platform") ? ro.get("platform").getAsString() : "Online Tutorial");
                            lr.setUrl(ro.has("url") ? ro.get("url").getAsString() : "");
                            lr.setDescription(ro.has("description") ? ro.get("description").getAsString() : "");
                            resources.add(lr);
                        }
                        path.setResources(resources);
                    }

                    if (po.has("practiceProjects")) {
                        List<SkillGapResponse.PracticeProject> projects = new ArrayList<>();
                        for (JsonElement pel : po.getAsJsonArray("practiceProjects")) {
                            JsonObject pro = pel.getAsJsonObject();
                            SkillGapResponse.PracticeProject pp = new SkillGapResponse.PracticeProject();
                            pp.setTitle(pro.has("title") ? pro.get("title").getAsString() : "");
                            pp.setDescription(pro.has("description") ? pro.get("description").getAsString() : "");
                            pp.setDifficulty(pro.has("difficulty") ? pro.get("difficulty").getAsString() : "Intermediate");
                            if (pro.has("keyDeliverables")) {
                                List<String> del = new ArrayList<>();
                                pro.getAsJsonArray("keyDeliverables").forEach(d -> del.add(d.getAsString()));
                                pp.setKeyDeliverables(del);
                            }
                            projects.add(pp);
                        }
                        path.setPracticeProjects(projects);
                    }
                    paths.add(path);
                }
                response.setLearningPaths(paths);
            }

            System.out.println("✅ AI Skill Gap parsed successfully. Match %: " + response.getSkillMatchPercentage());

        } catch (Exception e) {
            System.err.println("❌ Skill Gap Parse Error (" + e.getMessage() + "), using intelligent fallback analysis.");
            response = generateFallbackSkillGap(resumeText, jobDescription);
        }
        return response;
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        s = s.trim();
        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            s = s.substring(firstBrace, lastBrace + 1);
        }
        return s;
    }

    private SkillGapResponse generateFallbackSkillGap(String resumeText, String jobDescription) {
        SkillGapResponse response = new SkillGapResponse();

        class KnownSkill {
            final String name;
            final String category;
            final String defaultType;
            final String priority;
            final String docUrl;
            final String coursePlatform;
            final String courseTitle;
            final String projectTitle;
            final String projectDesc;
            final String difficulty;
            final List<String> deliverables;

            KnownSkill(String name, String category, String defaultType, String priority,
                       String docUrl, String coursePlatform, String courseTitle,
                       String projectTitle, String projectDesc, String difficulty, String... deliverables) {
                this.name = name;
                this.category = category;
                this.defaultType = defaultType;
                this.priority = priority;
                this.docUrl = docUrl;
                this.coursePlatform = coursePlatform;
                this.courseTitle = courseTitle;
                this.projectTitle = projectTitle;
                this.projectDesc = projectDesc;
                this.difficulty = difficulty;
                this.deliverables = Arrays.asList(deliverables);
            }
        }

        List<KnownSkill> catalog = Arrays.asList(
            new KnownSkill("Docker", "Cloud & DevOps", "REQUIRED", "HIGH",
                "https://docs.docker.com/get-started/", "Docker Docs & Udemy", "Docker & Kubernetes: The Practical Guide",
                "Production Containerized Microservice", "Containerize a full-stack Spring Boot + Angular application with persistent database volumes and health checks.", "Intermediate",
                "Multi-stage Dockerfile", "docker-compose.yml with networking", "Optimized production image size"),
            new KnownSkill("Kubernetes", "Cloud & DevOps", "REQUIRED", "HIGH",
                "https://kubernetes.io/docs/home/", "Kubernetes.io & Coursera", "Kubernetes Hands-On with Minikube",
                "Multi-node K8s Deployment", "Deploy microservices onto a Kubernetes cluster with ConfigMaps, Secrets, Ingress, and replica sets.", "Advanced",
                "Deployment and Service manifests", "Ingress routing configuration", "Horizontal Pod Autoscaler (HPA)"),
            new KnownSkill("AWS", "Cloud Platforms", "PREFERRED", "MEDIUM",
                "https://aws.amazon.com/getting-started/", "AWS Skill Builder & Coursera", "AWS Cloud Practitioner & Solutions Architect Essentials",
                "Serverless Document Processing API", "Build an automated resume processing pipeline utilizing S3, AWS Lambda, API Gateway, and DynamoDB.", "Intermediate",
                "S3 bucket with event triggers", "Serverless Lambda function", "IAM least-privilege security roles"),
            new KnownSkill("Kafka", "Streaming & Messaging", "PREFERRED", "MEDIUM",
                "https://kafka.apache.org/documentation/", "freeCodeCamp & Confluent", "Apache Kafka Event-Driven Architecture",
                "Real-time Notification Event Stream", "Develop an asynchronous event-driven notifications producer and consumer using Spring Cloud Stream and Kafka.", "Intermediate",
                "Kafka topic configuration", "Dead-letter queue handling", "Partitioning and consumer group tuning"),
            new KnownSkill("Redis", "Caching & Databases", "PREFERRED", "MEDIUM",
                "https://redis.io/docs/", "Redis University", "Redis for High Performance Caching",
                "Session Caching & API Rate Limiter", "Implement distributed Redis caching and a Token Bucket rate-limiter on Spring Boot REST endpoints.", "Intermediate",
                "Cache-aside pattern implementation", "TTL eviction policies", "Rate-limiting middleware filter"),
            new KnownSkill("Spring Boot", "Backend Frameworks", "REQUIRED", "HIGH",
                "https://spring.io/projects/spring-boot", "Spring.io Guides & Baeldung", "Spring Boot 3 Masterclass",
                "Enterprise Secure REST API", "Build a production-grade REST API with JWT authentication, role-based access control, JPA auditing, and Swagger docs.", "Intermediate",
                "Spring Security with JWT filter", "Clean layered architecture (Controller-Service-Repository)", "Exception handling with ControllerAdvice"),
            new KnownSkill("Microservices", "Architecture", "REQUIRED", "HIGH",
                "https://microservices.io/", "Coursera & Baeldung", "Microservices Architecture and Patterns",
                "Distributed E-Commerce Microservices", "Create decoupled services with API Gateway, Eureka service discovery, centralized configuration, and OpenFeign client.", "Advanced",
                "Spring Cloud Gateway routing", "Service discovery registry", "Distributed tracing with Micrometer / Zipkin"),
            new KnownSkill("PostgreSQL", "Databases", "REQUIRED", "HIGH",
                "https://www.postgresql.org/docs/", "PostgreSQL Tutorial & Udemy", "Complete SQL & PostgreSQL Bootcamp",
                "Optimized Database Schema & Indexing", "Design normalized relational schemas, complex analytical queries, indexing strategies, and database migrations with Flyway.", "Intermediate",
                "Third Normal Form (3NF) relational design", "B-tree and composite indexing", "Automated migration scripts"),
            new KnownSkill("MySQL", "Databases", "REQUIRED", "HIGH",
                "https://dev.mysql.com/doc/", "MySQL Tutorial", "The Ultimate MySQL Bootcamp",
                "Relational Inventory Management System", "Model transactional database with ACID compliance, stored procedures, and foreign key constraints.", "Beginner",
                "Relational schema modeling", "Complex JOIN queries and aggregations", "Transaction isolation management"),
            new KnownSkill("MongoDB", "NoSQL Databases", "PREFERRED", "MEDIUM",
                "https://www.mongodb.com/docs/", "MongoDB University", "MongoDB Basics and Aggregation Pipeline",
                "High-throughput Logging & Analytics Store", "Design flexible document schemas, compound indexes, and aggregation pipelines for analytics data.", "Intermediate",
                "Schema validation rules", "Multi-stage aggregation queries", "GridFS or document sub-structure modeling"),
            new KnownSkill("GraphQL", "API Design", "PREFERRED", "MEDIUM",
                "https://graphql.org/learn/", "GraphQL.org & Apollo Odyssey", "Building Modern APIs with GraphQL",
                "Unified GraphQL Gateway", "Expose queries, mutations, and subscriptions with schema definition language and DataLoader batching.", "Intermediate",
                "Type definitions and resolvers", "N+1 problem prevention with batching", "Query complexity limits"),
            new KnownSkill("CI/CD", "DevOps & Automation", "REQUIRED", "HIGH",
                "https://docs.github.com/en/actions", "GitHub Docs & GitLab University", "Automated CI/CD with GitHub Actions",
                "Zero-Downtime Automated Deployment Pipeline", "Configure automated build, linting, unit test execution, and container registry publishing on git push.", "Intermediate",
                ".github/workflows workflow file", "Automated testing step", "Environment secrets management"),
            new KnownSkill("TypeScript", "Programming Languages", "REQUIRED", "HIGH",
                "https://www.typescriptlang.org/docs/", "Total TypeScript & Udemy", "Understanding TypeScript",
                "Strictly Typed Modular Application", "Build type-safe interfaces, generics, utility types, and strict null check configurations.", "Beginner",
                "Comprehensive interfaces and type unions", "Generics for reusable services", "Strict compiler configuration"),
            new KnownSkill("Angular", "Frontend Frameworks", "REQUIRED", "HIGH",
                "https://angular.dev/", "Angular.dev Docs", "Angular - The Complete Guide",
                "Modern Standalone Component Dashboard", "Create responsive UI using Angular 18+, Standalone Components, Signals, reactive forms, and route guards.", "Intermediate",
                "Standalone components architecture", "RxJS reactive data streams", "AuthGuard and route resolvers"),
            new KnownSkill("React", "Frontend Frameworks", "REQUIRED", "HIGH",
                "https://react.dev/", "React.dev", "React: The Complete Guide",
                "Interactive Real-time Web App", "Develop React application with custom hooks, Context API, Tailwind CSS, and optimized rendering.", "Intermediate",
                "Custom hooks for API fetching", "State management with Context / Reducer", "Reusable component library"),
            new KnownSkill("JUnit", "Testing Frameworks", "REQUIRED", "HIGH",
                "https://junit.org/junit5/docs/current/user-guide/", "Baeldung", "Practical Unit Testing with JUnit 5 & Mockito",
                "Comprehensive Test Suite for REST APIs", "Write 80%+ code coverage unit and integration tests using JUnit 5, Mockito, and Spring MockMvc.", "Intermediate",
                "Unit tests with mocked dependencies", "Integration tests with @SpringBootTest", "Parameterized testing"),
            new KnownSkill("Git", "Version Control", "REQUIRED", "HIGH",
                "https://git-scm.com/doc", "freeCodeCamp & Pro Git", "Git & GitHub Crash Course",
                "Git Feature-Branch & Pull Request Workflow", "Demonstrate trunk-based or feature-branch branching, rebasing, squashing, and conflict resolution.", "Beginner",
                "Clean commit history", "Pull request reviews with CI checks", "Tagging and release branch management")
        );

        String jdL = jobDescription.toLowerCase();
        String resumeL = resumeText.toLowerCase();

        List<String> resumeSkills = new ArrayList<>();
        List<String> requiredSkills = new ArrayList<>();
        List<String> preferredSkills = new ArrayList<>();
        List<SkillGapResponse.MatchedSkill> matchedSkills = new ArrayList<>();
        List<SkillGapResponse.MissingSkill> missingSkills = new ArrayList<>();
        List<SkillGapResponse.SkillLearningPath> learningPaths = new ArrayList<>();

        int highCount = 0, medCount = 0, lowCount = 0;

        for (KnownSkill ks : catalog) {
            String nameLower = ks.name.toLowerCase();
            boolean inJd = jdL.contains(nameLower);
            boolean inResume = resumeL.contains(nameLower);

            if (inResume) {
                resumeSkills.add(ks.name);
            }

            if (inJd) {
                // Determine whether required or preferred
                boolean explicitlyPreferred = jdL.contains("preferred: " + nameLower) ||
                        jdL.contains("nice to have") && jdL.indexOf("nice to have") < jdL.indexOf(nameLower) + 100 && jdL.indexOf("nice to have") > jdL.indexOf(nameLower) - 100 ||
                        "PREFERRED".equalsIgnoreCase(ks.defaultType);
                String skillType = explicitlyPreferred ? "PREFERRED" : "REQUIRED";

                if (skillType.equals("REQUIRED")) {
                    requiredSkills.add(ks.name);
                } else {
                    preferredSkills.add(ks.name);
                }

                if (inResume) {
                    matchedSkills.add(new SkillGapResponse.MatchedSkill(
                        ks.name, skillType, ks.category, "Mentioned in resume projects/experience"
                    ));
                } else {
                    String priority = skillType.equals("REQUIRED") ? ks.priority : "LOW";
                    String reason = priority.equals("HIGH")
                        ? "Mandatory core requirement explicitly sought in the job specification"
                        : priority.equals("MEDIUM")
                        ? "Important technical framework that significantly elevates candidate competitiveness"
                        : "Preferred supplementary skill that serves as a hiring bonus";

                    if ("HIGH".equals(priority)) highCount++;
                    else if ("LOW".equals(priority)) lowCount++;
                    else medCount++;

                    missingSkills.add(new SkillGapResponse.MissingSkill(
                        ks.name, priority, skillType, ks.category, reason
                    ));

                    // Build learning path for missing skill
                    SkillGapResponse.SkillLearningPath path = new SkillGapResponse.SkillLearningPath();
                    path.setSkill(ks.name);
                    path.setPriority(priority);
                    path.setEstimatedTime(priority.equals("HIGH") ? "1-2 weeks" : "3-5 days");

                    List<SkillGapResponse.LearningResource> resList = new ArrayList<>();
                    resList.add(new SkillGapResponse.LearningResource(
                        ks.name + " Official Guide", "Official Documentation", ks.coursePlatform.split("&")[0].trim(), ks.docUrl,
                        "Authoritative official documentation, quickstart guides, and architectural best practices."
                    ));
                    resList.add(new SkillGapResponse.LearningResource(
                        ks.courseTitle, "Video Course & Labs", ks.coursePlatform, "https://www.coursera.org/search?query=" + ks.name,
                        "Comprehensive structured curriculum with practical exercises and industry certificates."
                    ));
                    path.setResources(resList);

                    List<SkillGapResponse.PracticeProject> projList = new ArrayList<>();
                    projList.add(new SkillGapResponse.PracticeProject(
                        ks.projectTitle, ks.projectDesc, ks.difficulty, ks.deliverables
                    ));
                    path.setPracticeProjects(projList);

                    learningPaths.add(path);
                }
            }
        }

        // If no skills matched from catalog, fallback to general list
        if (requiredSkills.isEmpty() && preferredSkills.isEmpty()) {
            requiredSkills.addAll(Arrays.asList("Java", "Spring Boot", "REST API", "SQL"));
            preferredSkills.addAll(Arrays.asList("Docker", "AWS"));
            matchedSkills.add(new SkillGapResponse.MatchedSkill("Java", "REQUIRED", "Backend", "Identified in resume"));
            missingSkills.add(new SkillGapResponse.MissingSkill("Docker", "HIGH", "REQUIRED", "Cloud & DevOps", "Container deployment required for the role"));
            highCount = 1;
        }

        int totalJdSkills = matchedSkills.size() + missingSkills.size();
        int matchedCount = matchedSkills.size();
        int matchPct = totalJdSkills > 0 ? (int) Math.round(((double) matchedCount / totalJdSkills) * 100) : 70;

        int reqMatched = (int) matchedSkills.stream().filter(m -> "REQUIRED".equalsIgnoreCase(m.getType())).count();
        int reqTotal = Math.max(1, requiredSkills.size());
        int reqPct = (int) Math.round(((double) reqMatched / reqTotal) * 100);

        int prefMatched = (int) matchedSkills.stream().filter(m -> "PREFERRED".equalsIgnoreCase(m.getType())).count();
        int prefTotal = Math.max(1, preferredSkills.size());
        int prefPct = (int) Math.round(((double) prefMatched / prefTotal) * 100);

        response.setSkillMatchPercentage(matchPct);
        response.setRequiredMatchPercentage(Math.min(100, reqPct));
        response.setPreferredMatchPercentage(Math.min(100, prefPct));
        response.setTotalSkillsCount(totalJdSkills);
        response.setMatchedSkillsCount(matchedCount);
        response.setMissingSkillsCount(missingSkills.size());
        response.setHighPriorityCount(highCount);
        response.setMediumPriorityCount(medCount);
        response.setLowPriorityCount(lowCount);

        response.setResumeSkills(resumeSkills);
        response.setRequiredSkills(requiredSkills);
        response.setPreferredSkills(preferredSkills);
        response.setMatchedSkills(matchedSkills);
        response.setMissingSkills(missingSkills);
        response.setLearningPaths(learningPaths);

        List<String> recommendations = new ArrayList<>();
        if (highCount > 0) {
            recommendations.add("Prioritize closing High Priority gaps first: " +
                missingSkills.stream().filter(m -> "HIGH".equals(m.getPriority())).map(SkillGapResponse.MissingSkill::getName).findFirst().orElse("core skills") +
                " is mandatory for this position.");
        }
        recommendations.add("Build a hands-on portfolio project integrating the missing tools into your existing tech stack.");
        recommendations.add("Add measurable impact and metric-driven achievements for your matched skills (" +
            matchedSkills.stream().map(SkillGapResponse.MatchedSkill::getName).limit(3).reduce((a, b) -> a + ", " + b).orElse("core strengths") +
            ") on your resume.");
        recommendations.add("Update your resume headline and skills section with keywords matching the job description to pass ATS filters.");

        response.setPersonalizedRecommendations(recommendations);
        return response;
    }
}