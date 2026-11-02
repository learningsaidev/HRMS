import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {

  constructor(public authService: AuthService, private router: Router) {}

  switchRole(role: string): void {
    this.authService.login(role, `${role.toLowerCase()}@abcd.corp`);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

