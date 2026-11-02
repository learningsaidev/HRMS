import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    details?: string[];
  };
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  // Endpoints mapped to microservice ports
  private readonly URLS = {
    identity: 'http://localhost:8081/api/v1/auth',
    workforce: 'http://localhost:8082/api/v1/workforce',
    timeLeave: 'http://localhost:8083/api/v1',
    onboarding: 'http://localhost:8084/api/v1',
    tax: 'http://localhost:8086/api/v1/tax',
    ledger: 'http://localhost:8087/api/v1/ledger',
    reporting: 'http://localhost:8088/api/v1/reporting',
    notifications: 'http://localhost:8088/api/v1/notifications',
    payroll: 'http://localhost:8089/api/v1/payroll'
  };

  constructor(private http: HttpClient) {}

  // Workforce API
  getEmployees(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.workforce}/employees`);
  }

  // Time & Leave API
  getPunches(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.timeLeave}/attendance/my-punches`);
  }

  recordPunch(punchType: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.timeLeave}/attendance/punch`, {
      punchType,
      latitude: 12.9716,
      longitude: 77.5946,
      deviceInfo: 'ABCD-Web-Browser'
    });
  }

  getLeaveBalances(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.timeLeave}/leaves/balances`);
  }

  // Payroll API
  getPayrollRuns(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.payroll}/runs`);
  }

  initiatePayrollRun(payGroupId: string, periodStart: string, periodEnd: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.payroll}/runs`, {
      legalEntityId: 'e0000000-0000-0000-0000-000000000001',
      payGroupId,
      periodStart,
      periodEnd,
      runType: 'REGULAR'
    });
  }

  calculatePayrollRun(runId: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.payroll}/runs/${runId}/calculate`, {});
  }

  lockPayrollRun(runId: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.payroll}/runs/${runId}/lock`, {});
  }

  getMyPayslips(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.payroll}/payslips/my-payslips`);
  }

  // General Ledger & Payments
  getJournalEntries(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.ledger}/journals`);
  }

  getPaymentBatches(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.ledger}/payments/batches`);
  }

  // Executive Dashboard
  getExecutiveDashboard(period: string = '2026-11'): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.URLS.reporting}/dashboard/executive?period=${period}`);
  }

  // Onboarding & BGV API
  getOnboardingCases(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.onboarding}/onboarding/cases`);
  }

  createOnboardingCase(candidate: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.onboarding}/onboarding/cases`, candidate);
  }

  initiateBgv(onboardingCaseId: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.onboarding}/bgv/initiate/${onboardingCaseId}`, {});
  }

  reviewBgv(bgvCaseId: string, review: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.onboarding}/bgv/cases/${bgvCaseId}/review`, review);
  }

  // Time & Leave Extended
  applyLeave(application: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.timeLeave}/leaves/apply`, application);
  }

  approveLeave(applicationId: string, approverId?: string): Observable<ApiResponse<any>> {
    const url = approverId ? `${this.URLS.timeLeave}/leaves/${applicationId}/approve?approverId=${approverId}` : `${this.URLS.timeLeave}/leaves/${applicationId}/approve`;
    return this.http.patch<ApiResponse<any>>(url, {});
  }

  lockAttendancePeriod(req: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.timeLeave}/attendance/lock`, req);
  }

  getAttendanceSummary(employeeId: string, year: number, month: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.URLS.timeLeave}/attendance/summary/${employeeId}?year=${year}&month=${month}`);
  }

  // Tax & Benefits API
  getTaxDeclarations(financialYear: string = '2026-2027'): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.tax}/declarations?financialYear=${financialYear}`);
  }

  submitTaxDeclaration(declaration: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.tax}/declarations`, declaration);
  }

  verifyTaxItem(declarationId: string, verification: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.tax}/declarations/${declarationId}/verify`, verification);
  }

  getLoansForEmployee(employeeId: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.URLS.tax}/loans/employee/${employeeId}`);
  }

  applyLoan(loanRequest: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.tax}/loans`, loanRequest);
  }

  disburseLoan(loanId: string, req: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.URLS.tax}/loans/${loanId}/disburse`, req);
  }
}
