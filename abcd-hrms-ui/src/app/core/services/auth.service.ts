import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: string;
  companyId: string;
  companyName: string;
  legalEntityId: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject: BehaviorSubject<UserProfile | null>;
  public currentUser$: Observable<UserProfile | null>;

  constructor() {
    const saved = localStorage.getItem('abcd_current_user');
    let initialUser: UserProfile | null = null;
    if (saved) {
      try {
        initialUser = JSON.parse(saved);
      } catch (e) {
        initialUser = null;
      }
    } else {
      // Default initial corporate user for immediate interactive exploration
      initialUser = {
        id: '00000000-0000-0000-0000-000000000001',
        email: 'superadmin@abcd.corp',
        fullName: 'Super Administrator',
        role: 'SUPER_ADMIN',
        companyId: 'd0000000-0000-0000-0000-000000000001',
        companyName: 'ABCD Corporation Global Holdings',
        legalEntityId: 'e0000000-0000-0000-0000-000000000001',
        token: 'mock-jwt-token-abcd-enterprise-2026'
      };
      localStorage.setItem('abcd_current_user', JSON.stringify(initialUser));
    }

    this.currentUserSubject = new BehaviorSubject<UserProfile | null>(initialUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): UserProfile | null {
    return this.currentUserSubject.value;
  }

  public login(role: string, email: string): void {
    const roleNames: Record<string, string> = {
      'SUPER_ADMIN': 'Super Administrator',
      'HR_ADMIN': 'Chief People Officer',
      'PAYROLL_OFFICER': 'Payroll Lead Controller',
      'FINANCE_CONTROLLER': 'Treasury Director',
      'MANAGER': 'Engineering Director',
      'EMPLOYEE': 'Senior Principal Engineer'
    };

    const user: UserProfile = {
      id: '00000000-0000-0000-0000-000000000001',
      email: email || `${role.toLowerCase()}@abcd.corp`,
      fullName: roleNames[role] || 'Corporate User',
      role: role,
      companyId: 'd0000000-0000-0000-0000-000000000001',
      companyName: 'ABCD Corporation Global Holdings',
      legalEntityId: 'e0000000-0000-0000-0000-000000000001',
      token: 'jwt-token-' + role.toLowerCase() + '-' + Date.now()
    };

    localStorage.setItem('abcd_current_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  public logout(): void {
    localStorage.removeItem('abcd_current_user');
    this.currentUserSubject.next(null);
  }

  public isAuthenticated(): boolean {
    return !!this.currentUserValue;
  }
}

