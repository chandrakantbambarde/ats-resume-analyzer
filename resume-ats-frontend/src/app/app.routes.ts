import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./components/home/home.component')
            .then(m => m.HomeComponent)
    },
    {
        path: 'login',
        loadComponent: () => import('./components/login/login.component')
            .then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./components/register/register.component')
            .then(m => m.RegisterComponent)
    },
    {
        path: 'upload',
        canActivate: [AuthGuard],
        loadComponent: () => import('./components/upload-resume/upload-resume.component')
            .then(m => m.UploadResumeComponent)
    },
    {
        path: 'profile',
        canActivate: [AuthGuard],
        loadComponent: () => import('./components/profile/profile.component')
            .then(m => m.ProfileComponent)
    },
    {
        path: 'dashboard',
        canActivate: [AuthGuard],
        loadComponent: () => import('./components/dashboard/dashboard.component')
            .then(m => m.DashboardComponent)
    },
    {
        path: 'history',
        canActivate: [AuthGuard],
        loadComponent: () => import('./components/history/history.component')
            .then(m => m.HistoryComponent)
    },
    {
        path: 'skill-gap',
        canActivate: [AuthGuard],
        loadComponent: () => import('./components/skill-gap/skill-gap.component')
            .then(m => m.SkillGapComponent)
    },
    {
        path: '**',
        redirectTo: ''
    }
];