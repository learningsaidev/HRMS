import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EssComponent } from './features/ess/ess.component';
import { WorkforceComponent } from './features/workforce/workforce.component';
import { TimeLeaveComponent } from './features/time-leave/time-leave.component';
import { OnboardingComponent } from './features/onboarding/onboarding.component';
import { PayrollComponent } from './features/payroll/payroll.component';
import { TaxBenefitsComponent } from './features/tax-benefits/tax-benefits.component';
import { LedgerComponent } from './features/ledger/ledger.component';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'ess', component: EssComponent, canActivate: [AuthGuard] },
  { path: 'workforce', component: WorkforceComponent, canActivate: [AuthGuard] },
  { path: 'time-leave', component: TimeLeaveComponent, canActivate: [AuthGuard] },
  { path: 'onboarding', component: OnboardingComponent, canActivate: [AuthGuard] },
  { path: 'payroll', component: PayrollComponent, canActivate: [AuthGuard] },
  { path: 'tax-benefits', component: TaxBenefitsComponent, canActivate: [AuthGuard] },
  { path: 'ledger', component: LedgerComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

