import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth/service/auth.service';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule],
    template: `
        <div class="container mt-4">
            <h2>User Profile</h2>
            <div class="card">
                <div class="card-body">
                    <p><strong>Name:</strong> {{ authService.getCurrentUser()?.name }}</p>
                    <p><strong>Email:</strong> {{ authService.getCurrentUser()?.email }}</p>
                </div>
            </div>
        </div>
    `
})
export class ProfileComponent {
    constructor(public authService: AuthService) {}
}