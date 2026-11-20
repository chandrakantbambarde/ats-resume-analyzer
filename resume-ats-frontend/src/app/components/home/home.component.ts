import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  isLoggedIn = false;

  constructor(private router: Router) {}

  ngOnInit() {
    this.isLoggedIn = !!localStorage.getItem('token');
  }

  goToUpload() {
    if (this.isLoggedIn) {
      this.router.navigate(['/upload']);
    } else {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/upload' } });
    }
  }
  goToLogin() { this.router.navigate(['/login'], { queryParams: { returnUrl: '/upload' } }); }
  goToRegister() { this.router.navigate(['/register'], { queryParams: { returnUrl: '/upload' } }); }
}