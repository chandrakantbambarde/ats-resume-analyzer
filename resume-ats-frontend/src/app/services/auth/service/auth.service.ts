import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    userId: number;
    email: string;
    name: string;
    
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = 'http://localhost:8080/api/auth';
    private currentUserSubject = new BehaviorSubject<LoginResponse | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient, private router: Router) {
        this.loadStoredUser();
    }

    private loadStoredUser() {
        const storedUser = localStorage.getItem('currentUser');
        if (storedUser) {
            this.currentUserSubject.next(JSON.parse(storedUser));
        }
    }

    register(userData: RegisterRequest): Observable<any> {
        console.log('📤 Sending register request:', userData);
        return this.http.post(`${this.apiUrl}/register`, userData).pipe(
            tap(response => {
                console.log('✅ Register response:', response);
            }),
            catchError(error => {
                console.error('❌ Register error:', error);
                throw error;
            })
        );
    }

    login(credentials: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap(response => {
                localStorage.setItem('currentUser', JSON.stringify(response));
                localStorage.setItem('token', response.token);
                this.currentUserSubject.next(response);
            })
        );
    }

    logout(): void {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
    }

    getCurrentUser(): LoginResponse | null {
        return this.currentUserSubject.value;
    }

    isLoggedIn(): boolean {
        return !!this.getCurrentUser() && !!this.getToken();
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    getUserId(): number | null {
        return this.getCurrentUser()?.userId || null;
    }
}