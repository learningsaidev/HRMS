import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  selectedRole = 'SUPER_ADMIN';
  email = 'superadmin@abcd.corp';

  constructor(private authService: AuthService, private router: Router) {}

  onRoleChange(role: string): void {
    this.selectedRole = role;
    this.email = `${role.toLowerCase()}@abcd.corp`;
  }

  onSubmit(): void {
    this.authService.login(this.selectedRole, this.email);
    this.router.navigate(['/dashboard']);
  }
}

