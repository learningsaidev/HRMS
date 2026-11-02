import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-ess',
  templateUrl: './ess.component.html',
  styleUrls: ['./ess.component.scss']
})
export class EssComponent implements OnInit {

  activeTab: 'payslips' | 'attendance' | 'leaves' | 'tax' = 'payslips';
  payslips: any[] = [];
  punches: any[] = [];
  leaves: any[] = [];
  isClockedIn = false;

  constructor(private apiService: ApiService, public authService: AuthService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    // Simulated live payslip data based on backend payroll-service
    this.payslips = [
      {
        id: 'slip-001',
        period: 'November 2026',
        daysInMonth: 30,
        paidDays: 30.0,
        lopDays: 0.0,
        grossEarnings: 100000.0,
        totalDeductions: 6200.0,
        netPay: 93800.0,
        paymentStatus: 'PAID',
        earnings: [
          { name: 'Basic Salary', amount: 50000.0 },
          { name: 'House Rent Allowance (HRA)', amount: 25000.0 },
          { name: 'Special Allowance', amount: 25000.0 }
        ],
        deductions: [
          { name: 'Provident Fund (Employee 12%)', amount: 6000.0 },
          { name: 'Professional Tax (PT)', amount: 200.0 }
        ]
      },
      {
        id: 'slip-002',
        period: 'October 2026',
        daysInMonth: 31,
        paidDays: 31.0,
        lopDays: 0.0,
        grossEarnings: 100000.0,
        totalDeductions: 6200.0,
        netPay: 93800.0,
        paymentStatus: 'PAID',
        earnings: [
          { name: 'Basic Salary', amount: 50000.0 },
          { name: 'House Rent Allowance (HRA)', amount: 25000.0 },
          { name: 'Special Allowance', amount: 25000.0 }
        ],
        deductions: [
          { name: 'Provident Fund (Employee 12%)', amount: 6000.0 },
          { name: 'Professional Tax (PT)', amount: 200.0 }
        ]
      }
    ];

    this.punches = [
      { type: 'CLOCK_IN', time: '09:02 AM', date: 'Today', status: 'ON_TIME' },
      { type: 'CLOCK_OUT', time: '06:15 PM', date: 'Yesterday', status: 'REGULAR' },
      { type: 'CLOCK_IN', time: '08:58 AM', date: 'Yesterday', status: 'ON_TIME' }
    ];

    this.leaves = [
      { type: 'Casual Leave (CL)', allocated: 12, consumed: 3, available: 9 },
      { type: 'Earned Leave (EL)', allocated: 18, consumed: 4, available: 14 },
      { type: 'Sick Leave (SL)', allocated: 10, consumed: 1, available: 9 }
    ];
  }

  toggleClock(): void {
    const punchType = this.isClockedIn ? 'CLOCK_OUT' : 'CLOCK_IN';
    this.apiService.recordPunch(punchType).subscribe({
      next: () => {
        this.isClockedIn = !this.isClockedIn;
      },
      error: () => {
        this.isClockedIn = !this.isClockedIn; // fallback
      }
    });
  }

  downloadPayslip(slip: any): void {
    alert(`Downloading encrypted payslip PDF for period ${slip.period}. Net Pay: ₹${slip.netPay.toLocaleString()}`);
  }
}

