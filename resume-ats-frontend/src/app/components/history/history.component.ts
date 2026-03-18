import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ResumeService } from '../../services/auth/service/resume.service';
import { AuthService } from '../../services/auth/service/auth.service';
import { AnalysisHistory } from '../../models/user/model/analysis.model';

@Component({
    selector: 'app-history',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
        <div class="container mt-4">
            <h2>Analysis History</h2>
            <p>Your previous resume analyses will appear here.</p>
            
            @if (loading) {
                <div class="text-center">
                    <div class="spinner-border text-primary"></div>
                    <p>Loading history...</p>
                </div>
            } @else {
                @if (error) {
                    <div class="alert alert-danger">
                        {{ error }}
                    </div>
                }
                @if (history.length === 0) {
                    <div class="alert alert-info">
                        No analysis history found. 
                        <a routerLink="/upload">Upload your first resume</a>
                    </div>
                } @else {
                    <div class="list-group">
                        @for (item of history; track item.id) {
                            <div class="list-group-item">
                                <h5>{{ item.fileName }}</h5>
                                <p>Score: <strong>{{ item.atsScore }}%</strong></p>
                                <small>{{ item.analyzedAt | date:'medium' }}</small>
                            </div>
                        }
                    </div>
                }
            }
        </div>
    `
})
export class HistoryComponent implements OnInit {
    history: AnalysisHistory[] = [];
    loading = true;
    error: string | null = null;

    constructor(
        private resumeService: ResumeService,
        private authService: AuthService
    ) {}

    ngOnInit(): void {
        this.loadHistory();
    }

    loadHistory(): void {
        const userId = this.authService.getUserId();
        console.log('Loading history for userId:', userId);
        
        if (!userId) {
            this.error = 'User not authenticated. Please login.';
            this.loading = false;
            return;
        }

        this.resumeService.getUserHistory(userId).subscribe({
            next: (data) => {
                console.log('History loaded:', data);
                this.history = data;
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading history:', error);
                this.error = error.message || 'Failed to load history';
                this.loading = false;
            }
        });
    }
}