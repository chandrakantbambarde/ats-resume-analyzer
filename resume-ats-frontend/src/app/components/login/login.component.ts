import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/service/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']  // ✅ CSS add केली
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  submitted = false;
  showPassword = false;   // ✅ नवीन - password toggle
  errorMessage = '';      // ✅ नवीन - error message

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get f() { return this.loginForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        console.log('Login success', response);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Login error', error);
        this.errorMessage = error.error?.message || 'Invalid email or password.';
        this.loading = false;
      }
    });
  }
}