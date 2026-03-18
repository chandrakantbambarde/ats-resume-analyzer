import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth/service/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {
    constructor(private authService: AuthService, private router: Router) {}

    // Can user access this route
    canActivate(): boolean {
        if (this.authService.isLoggedIn()) {
            return true;  // Allow access
        }
        
        this.router.navigate(['/login']);   
        return false;
    }
}