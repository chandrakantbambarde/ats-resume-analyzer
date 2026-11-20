export interface MatchedSkill {
  name: string;
  type: 'REQUIRED' | 'PREFERRED' | string;
  category: string;
  context?: string;
}

export interface MissingSkill {
  name: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  type: 'REQUIRED' | 'PREFERRED' | string;
  category: string;
  reason: string;
}

export interface LearningResource {
  title: string;
  type: string;
  platform: string;
  url: string;
  description: string;
}

export interface PracticeProject {
  title: string;
  description: string;
  difficulty: string;
  keyDeliverables: string[];
}

export interface SkillLearningPath {
  skill: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  estimatedTime: string;
  resources: LearningResource[];
  practiceProjects: PracticeProject[];
}

export interface SkillGapResponse {
  fileName?: string;
  skillMatchPercentage: number;
  requiredMatchPercentage: number;
  preferredMatchPercentage: number;
  totalSkillsCount: number;
  matchedSkillsCount: number;
  missingSkillsCount: number;
  highPriorityCount: number;
  mediumPriorityCount: number;
  lowPriorityCount: number;
  resumeSkills: string[];
  requiredSkills: string[];
  preferredSkills: string[];
  matchedSkills: MatchedSkill[];
  missingSkills: MissingSkill[];
  personalizedRecommendations: string[];
  learningPaths: SkillLearningPath[];
}

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
  skillGapAnalysis?: SkillGapResponse;
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