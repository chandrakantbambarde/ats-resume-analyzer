import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AnalysisHistory } from '../../../models/user/model/analysis.model';

@Injectable({ providedIn: 'root' })
export class ResumeService {

  private apiUrl = 'http://localhost:8080/api/resume';

  constructor(private http: HttpClient) {}

  analyzeResume(file: File, jobDescription: string, userId: number): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('jobDescription', jobDescription);
    formData.append('userId', userId.toString());
    return this.http.post(`${this.apiUrl}/analyze`, formData);
  }

  getUserHistory(userId: number): Observable<AnalysisHistory[]> {
    return this.http.get<AnalysisHistory[]>(`${this.apiUrl}/history/${userId}`);
  }

  // ✅ NEW - PDF export
  exportPdf(analysisData: any): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/export-pdf`, analysisData, {
      responseType: 'blob'
    });
  }
}