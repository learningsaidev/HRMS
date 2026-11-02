import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-workforce',
  templateUrl: './workforce.component.html',
  styleUrls: ['./workforce.component.scss']
})
export class WorkforceComponent implements OnInit {

  employees: any[] = [];
  filterStatus = 'ACTIVE';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.apiService.getEmployees().subscribe({
      next: (res) => {
        if (res.success && res.data && res.data.length > 0) {
          this.employees = res.data;
        } else {
          this.useFallbackEmployees();
        }
      },
      error: () => {
        this.useFallbackEmployees();
      }
    });
  }

  private useFallbackEmployees(): void {
    this.employees = [
      {
        employeeNumber: 'EMP-000001',
        name: 'Alice Johnson',
        workEmail: 'alice.johnson@abcd.corp',
        department: 'Engineering',
        designation: 'Principal Staff Engineer',
        employmentType: 'FULL_TIME',
        status: 'ACTIVE',
        hireDate: '2024-01-15'
      },
      {
        employeeNumber: 'EMP-000002',
        name: 'Bob Smith',
        workEmail: 'bob.smith@abcd.corp',
        department: 'Product',
        designation: 'Lead Product Manager',
        employmentType: 'FULL_TIME',
        status: 'ACTIVE',
        hireDate: '2024-03-01'
      },
      {
        employeeNumber: 'EMP-000003',
        name: 'Charlie Davis',
        workEmail: 'charlie.davis@abcd.corp',
        department: 'Finance & Operations',
        designation: 'Senior Treasury Analyst',
        employmentType: 'FULL_TIME',
        status: 'ACTIVE',
        hireDate: '2024-06-15'
      },
      {
        employeeNumber: 'EMP-000004',
        name: 'Diana Prince',
        workEmail: 'diana.prince@abcd.corp',
        department: 'Human Resources',
        designation: 'HRBP Lead',
        employmentType: 'FULL_TIME',
        status: 'ACTIVE',
        hireDate: '2025-01-10'
      }
    ];
  }
}

