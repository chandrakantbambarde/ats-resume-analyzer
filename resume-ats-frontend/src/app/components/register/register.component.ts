import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth/service/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';
  showPassword = false;
  returnUrl = '/upload';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/upload';
  }

  get f() { return this.registerForm.controls; }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = '';

    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    console.log('🚀 Submitting form:', this.registerForm.value);

    this.authService.register(this.registerForm.value).subscribe({
      next: (response: any) => {
        console.log('✅ Success:', response);
        this.router.navigate(['/login'], { queryParams: { returnUrl: this.returnUrl } });
      },
      error: (error) => {
        console.error('❌ Error:', error);
        this.loading = false;

        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Make sure backend is running on port 8080.';
        } else if (error.status === 404) {
          this.errorMessage = 'API endpoint not found. Check backend URLs.';
        } else if (error.status === 500) {
          this.errorMessage = 'Server error. Check backend logs.';
        } else {
          this.errorMessage = error.error?.error || error.error?.message || 'Registration failed. Please try again.';
        }
      }
    });
  }
}