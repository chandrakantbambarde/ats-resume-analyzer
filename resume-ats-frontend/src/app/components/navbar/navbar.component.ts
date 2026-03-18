import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  username = '';
  menuOpen = false;

  constructor(private router: Router) {}

  ngOnInit() {
    this.isLoggedIn = !!localStorage.getItem('token');
    this.username = localStorage.getItem('username') || 'User';
  }

  getInitial(): string {
    return this.username.charAt(0).toUpperCase();
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  // Close menu when clicking outside
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu') && !target.closest('.dropdown')) {
      this.menuOpen = false;
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.isLoggedIn = false;
    this.menuOpen = false;
    this.router.navigate(['/home']);
  }
}