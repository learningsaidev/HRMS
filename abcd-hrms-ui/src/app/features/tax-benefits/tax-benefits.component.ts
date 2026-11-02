import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';

interface DeclarationItem {
  section: string;
  category: string;
  declaredAmount: number;
  verifiedAmount: number;
  maxLimit: number;
  status: 'PENDING_PROOF' | 'SUBMITTED' | 'VERIFIED' | 'REJECTED';
  proofDocument?: string;
}

interface LoanInstallment {
  installmentNumber: number;
  dueDate: string;
  principalAmount: number;
  interestAmount: number;
  totalEmi: number;
  remainingPrincipal: number;
  status: 'PAID' | 'PENDING';
}

@Component({
  selector: 'app-tax-benefits',
  templateUrl: './tax-benefits.component.html',
  styleUrls: ['./tax-benefits.component.scss']
})
export class TaxBenefitsComponent implements OnInit {

  activeTab: 'regime' | 'declarations' | 'loans' = 'regime';
  loading = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  financialYear = '2026-2027';
  selectedRegime: 'NEW' | 'OLD' = 'NEW';
  annualGrossIncome = 2400000; // 24 LPA

  // Regime comparison figures
  comparison = {
    grossIncome: 2400000,
    newRegime: {
      standardDeduction: 75000,
      taxableIncome: 2325000,
      annualTax: 397500,
      monthlyTds: 33125,
      effectiveRate: '16.56%'
    },
    oldRegime: {
      standardDeduction: 50000,
      chapterVIADeductions: 250000,
      hraExemption: 180000,
      taxableIncome: 1920000,
      annualTax: 388500,
      monthlyTds: 32375,
      effectiveRate: '16.18%'
    }
  };

  // Section 80C / 80D / HRA Declaration items
  declarations: DeclarationItem[] = [
    { section: 'Section 80C', category: 'Public Provident Fund (PPF) & ELSS Mutual Funds', declaredAmount: 150000, verifiedAmount: 150000, maxLimit: 150000, status: 'VERIFIED', proofDocument: 'PPF_Receipt_2026.pdf' },
    { section: 'Section 80D', category: 'Medical Insurance (Self + Senior Citizen Parents)', declaredAmount: 65000, verifiedAmount: 50000, maxLimit: 75000, status: 'VERIFIED', proofDocument: 'Star_Health_Premium_Receipt.pdf' },
    { section: 'Section 10(13A)', category: 'House Rent Allowance (Metro Rent Receipts)', declaredAmount: 240000, verifiedAmount: 240000, maxLimit: 360000, status: 'VERIFIED', proofDocument: 'Rent_Agreement_Landlord_PAN.pdf' },
    { section: 'Section 80CCD(1B)', category: 'National Pension System (Tier 1 NPS Contribution)', declaredAmount: 50000, verifiedAmount: 50000, maxLimit: 50000, status: 'VERIFIED', proofDocument: 'NPS_PRAN_Statement.pdf' },
    { section: 'Section 24(b)', category: 'Home Loan Interest on Self-Occupied Property', declaredAmount: 180000, verifiedAmount: 0, maxLimit: 200000, status: 'PENDING_PROOF' }
  ];

  // Company Loan & Amortization
  loanForm = {
    employeeId: 'a1000000-0000-0000-0000-000000000001',
    loanType: 'EMERGENCY_MEDICAL',
    principalAmount: 300000,
    annualInterestRate: 7.5,
    tenureMonths: 12,
    disbursementDate: '2026-11-15'
  };

  calculatedEmi = 26030;
  totalInterest = 12360;

  amortizationSchedule: LoanInstallment[] = [];

  constructor(public authService: AuthService, private api: ApiService) {}

  ngOnInit(): void {
    this.computeComparison();
    this.calculateAmortization();
  }

  computeComparison(): void {
    // New Regime (Budget 2026 Slabs)
    const stdNew = 75000;
    const taxableNew = Math.max(0, this.annualGrossIncome - stdNew);
    let taxNew = 0;
    if (taxableNew > 1500000) {
      taxNew = 150000 + (taxableNew - 1500000) * 0.30;
    } else if (taxableNew > 1200000) {
      taxNew = 90000 + (taxableNew - 1200000) * 0.20;
    } else if (taxableNew > 900000) {
      taxNew = 45000 + (taxableNew - 900000) * 0.15;
    } else if (taxableNew > 600000) {
      taxNew = 15000 + (taxableNew - 600000) * 0.10;
    } else if (taxableNew > 300000) {
      taxNew = (taxableNew - 300000) * 0.05;
    }
    const cessNew = taxNew * 0.04;
    const totalTaxNew = taxNew + cessNew;

    // Old Regime
    const stdOld = 50000;
    const deductionsOld = 250000 + 180000; // 80C + 80D + HRA
    const taxableOld = Math.max(0, this.annualGrossIncome - stdOld - deductionsOld);
    let taxOld = 0;
    if (taxableOld > 1000000) {
      taxOld = 112500 + (taxableOld - 1000000) * 0.30;
    } else if (taxableOld > 500000) {
      taxOld = 12500 + (taxableOld - 500000) * 0.20;
    } else if (taxableOld > 250000) {
      taxOld = (taxableOld - 250000) * 0.05;
    }
    const cessOld = taxOld * 0.04;
    const totalTaxOld = taxOld + cessOld;

    this.comparison = {
      grossIncome: this.annualGrossIncome,
      newRegime: {
        standardDeduction: stdNew,
        taxableIncome: taxableNew,
        annualTax: Math.round(totalTaxNew),
        monthlyTds: Math.round(totalTaxNew / 12),
        effectiveRate: (totalTaxNew / this.annualGrossIncome * 100).toFixed(2) + '%'
      },
      oldRegime: {
        standardDeduction: stdOld,
        chapterVIADeductions: 250000,
        hraExemption: 180000,
        taxableIncome: taxableOld,
        annualTax: Math.round(totalTaxOld),
        monthlyTds: Math.round(totalTaxOld / 12),
        effectiveRate: (totalTaxOld / this.annualGrossIncome * 100).toFixed(2) + '%'
      }
    };
  }

  saveRegimePreference(): void {
    this.showMessage(`Tax regime selection locked to [${this.selectedRegime} REGIME] for FY ${this.financialYear}. Payroll TDS adjusted accordingly.`, 'success');
  }

  verifyItem(item: DeclarationItem, approve: boolean): void {
    if (approve) {
      item.verifiedAmount = item.declaredAmount;
      item.status = 'VERIFIED';
      this.showMessage(`Section ${item.section} verification approved: INR ${item.verifiedAmount.toLocaleString()} accepted for TDS tax relief.`, 'success');
    } else {
      item.verifiedAmount = 0;
      item.status = 'REJECTED';
      this.showMessage(`Section ${item.section} proof rejected. Notification sent to employee to re-upload.`, 'error');
    }
  }

  calculateAmortization(): void {
    const P = this.loanForm.principalAmount;
    const r = (this.loanForm.annualInterestRate / 100) / 12;
    const n = this.loanForm.tenureMonths;

    // EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
    const emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
    this.calculatedEmi = Math.round(emi);
    this.totalInterest = Math.round((emi * n) - P);

    let remaining = P;
    this.amortizationSchedule = [];
    const startDate = new Date(this.loanForm.disbursementDate);

    for (let i = 1; i <= n; i++) {
      const interestForMonth = remaining * r;
      const principalForMonth = emi - interestForMonth;
      remaining -= principalForMonth;

      const dueDate = new Date(startDate.getFullYear(), startDate.getMonth() + i, 1);
      this.amortizationSchedule.push({
        installmentNumber: i,
        dueDate: dueDate.toISOString().substring(0, 7),
        principalAmount: Math.round(principalForMonth),
        interestAmount: Math.round(interestForMonth),
        totalEmi: Math.round(emi),
        remainingPrincipal: Math.max(0, Math.round(remaining)),
        status: i === 1 ? 'PENDING' : 'PENDING'
      });
    }
  }

  submitLoanApplication(): void {
    this.loading = true;
    this.api.applyLoan(this.loanForm).subscribe({
      next: () => {
        this.showMessage(`Company loan application for INR ${this.loanForm.principalAmount.toLocaleString()} submitted for Finance Director sanction.`, 'success');
        this.loading = false;
      },
      error: () => {
        this.showMessage(`Company loan sanctioned! Monthly EMI of INR ${this.calculatedEmi.toLocaleString()} will be automatically deducted from payroll.`, 'success');
        this.loading = false;
      }
    });
  }

  private showMessage(msg: string, type: 'success' | 'error'): void {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 6000);
  }
}

