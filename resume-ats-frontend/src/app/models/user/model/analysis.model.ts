export interface AnalysisResponse {
  atsScore: number;
  jdMatchScore: number;
  missingKeywords: string;
  strongMatches: string[];
  jdMatchedSkills: string[];
  jdMissingSkills: string[];
  skillsGap: string[];
  skillsScore: number;
  experienceScore: number;
  educationScore: number;
  suggestions: string[];
  interviewTips: string[];
  coverLetter: string;
  formatIssues: string;
  overallRecommendation: string;
  fileName: string;
  analyzedAt: string;
}

export interface AnalysisHistory {
  id: number;
  fileName: string;
  jobDescription: string;
  atsScore: number;
  missingKeywords: string;
  suggestions: string;
  formatIssues: string;
  recommendation: string;
  coverLetter: string;
  interviewTips: string;
  skillsGap: string;
  jdMatchScore: number;
  analyzedAt: string;
}