import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/service/auth.service';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
        <div class="container mt-4">
            <h2>Welcome, {{ authService.getCurrentUser()?.name }}!</h2>
            
            <div class="row mt-4">
                <div class="col-md-4">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">Upload Resume</h5>
                            <p class="card-text">Analyze a new resume</p>
                            <a routerLink="/upload" class="btn btn-primary">Upload</a>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-4">
                    <div class="card text-center">
                        <div class="card-body">
                            <h5 class="card-title">View History</h5>
                            <p class="card-text">Check past analyses</p>
                            <a routerLink="/history" class="btn btn-info">History</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class DashboardComponent {
    constructor(public authService: AuthService) {}
}