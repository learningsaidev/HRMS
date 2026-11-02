import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './layout/header/header.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EssComponent } from './features/ess/ess.component';
import { WorkforceComponent } from './features/workforce/workforce.component';
import { TimeLeaveComponent } from './features/time-leave/time-leave.component';
import { OnboardingComponent } from './features/onboarding/onboarding.component';
import { PayrollComponent } from './features/payroll/payroll.component';
import { TaxBenefitsComponent } from './features/tax-benefits/tax-benefits.component';
import { LedgerComponent } from './features/ledger/ledger.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    SidebarComponent,
    LoginComponent,
    DashboardComponent,
    EssComponent,
    WorkforceComponent,
    TimeLeaveComponent,
    OnboardingComponent,
    PayrollComponent,
    TaxBenefitsComponent,
    LedgerComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
