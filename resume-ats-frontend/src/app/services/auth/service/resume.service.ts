import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timeout, catchError, TimeoutError } from 'rxjs';
import { AnalysisResponse } from '../../../models/auth.model';

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

@Injectable({
    providedIn: 'root'
})
export class ResumeService {
    private apiUrl = 'http://localhost:8080/api/resume';

    constructor(private http: HttpClient) { }

    // ✅ FIXED: getUserHistory method - Returns Observable
    getUserHistory(userId: number): Observable<AnalysisHistory[]> {
        console.log('🔍 Getting history for user:', userId);
        return this.http.get<AnalysisHistory[]>(`${this.apiUrl}/history/${userId}`).pipe(
            timeout(30000),
            catchError(error => {
                console.error('❌ Error loading history:', error);
                throw error;
            })
        );
    }

    // ✅ analyzeResume method - Already working
    analyzeResume(file: File, jobDescription: string, userId?: number): Observable<AnalysisResponse> {
        console.log('🔍 API Call Started:', this.apiUrl + '/analyze');
        
        const formData = new FormData();
        formData.append('file', file);
        formData.append('jobDescription', jobDescription);
        
        if (userId) {
            formData.append('userId', userId.toString());
        }

        return this.http.post<AnalysisResponse>(`${this.apiUrl}/analyze`, formData).pipe(
            timeout(30000),
            catchError(error => {
                if (error instanceof TimeoutError) {
                    console.error('❌ Timeout - Server not responding');
                    throw new Error('Server timeout. Please try again.');
                }
                console.error('❌ API Error:', error);
                throw error;
            })
        );
    }

    // ✅ Optional: deleteHistory method
    deleteHistory(analysisId: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/history/${analysisId}`).pipe(
            timeout(30000),
            catchError(error => {
                console.error('❌ Error deleting history:', error);
                throw error;
            })
        );
    }
}