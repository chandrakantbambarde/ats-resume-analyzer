export interface AnalysisResponse {
    atsScore: number;
    missingKeywords: string;
    suggestions: string[];
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
    analyzedAt: string;
}