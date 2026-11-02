import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-ledger',
  templateUrl: './ledger.component.html',
  styleUrls: ['./ledger.component.scss']
})
export class LedgerComponent implements OnInit {

  activeView: 'journals' | 'disbursements' = 'journals';
  journalEntries: any[] = [];
  paymentBatches: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.journalEntries = [
      {
        entryNumber: 'JV-20261130-C9EC18',
        entryDate: '2026-11-30',
        sourceModule: 'PAYROLL',
        sourceReferenceId: 'RUN-202611-CORP',
        narration: 'Payroll accrual and salary disbursement for November 2026',
        totalAmount: 29500000.0,
        isPosted: true,
        lines: [
          { accountCode: '5001', accountName: 'Salaries and Wages Expense', type: 'DEBIT', amount: 29500000.0 },
          { accountCode: '2002', accountName: 'Provident Fund Payable', type: 'CREDIT', amount: 1770000.0 },
          { accountCode: '2003', accountName: 'Professional Tax Payable', type: 'CREDIT', amount: 57000.0 },
          { accountCode: '2004', accountName: 'ESI Payable', type: 'CREDIT', amount: 2000.0 },
          { accountCode: '2001', accountName: 'Salaries Payable Clearing', type: 'CREDIT', amount: 27671000.0 }
        ]
      },
      {
        entryNumber: 'JV-20261130-DISB01',
        entryDate: '2026-11-30',
        sourceModule: 'PAYROLL_DISBURSEMENT',
        sourceReferenceId: 'BATCH-202611-001',
        narration: 'Corporate bank debit via NEFT for employee disbursement',
        totalAmount: 27671000.0,
        isPosted: true,
        lines: [
          { accountCode: '2001', accountName: 'Salaries Payable Clearing', type: 'DEBIT', amount: 27671000.0 },
          { accountCode: '1001', accountName: 'Corporate Operating Bank Account', type: 'CREDIT', amount: 27671000.0 }
        ]
      }
    ];

    this.paymentBatches = [
      {
        batchReference: 'BATCH-202611-001',
        paymentMethod: 'NEFT',
        currency: 'INR',
        totalAmount: 27671000.0,
        totalTransactions: 285,
        status: 'COMPLETED',
        createdAt: '2026-11-30 10:15 AM'
      }
    ];
  }

  simulateReturn(): void {
    alert('Triggered compensating saga for failed bank transaction. Posted reversing Journal Voucher (Debit Bank 1001, Credit Salaries Payable 2001) and notified employee via Splunk log & event queue.');
  }
}
