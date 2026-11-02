-- V1__init_payroll_schema.sql
-- ABCD HRMS Enterprise Double-Entry Payroll & Calculation Engine Schema

CREATE SCHEMA IF NOT EXISTS abcd_payroll;

CREATE TABLE IF NOT EXISTS pay_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    legal_entity_id UUID NOT NULL,
    group_code VARCHAR(50) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    pay_frequency VARCHAR(30) NOT NULL DEFAULT 'MONTHLY',
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    salary_disbursement_day INT NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_pay_groups_code UNIQUE (company_id, group_code)
);

CREATE TABLE IF NOT EXISTS salary_components (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    component_code VARCHAR(50) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(40) NOT NULL, -- EARNING, DEDUCTION, STATUTORY_EMPLOYEE, STATUTORY_EMPLOYER, REIMBURSEMENT
    calculation_type VARCHAR(30) NOT NULL DEFAULT 'FIXED', -- FIXED, PERCENTAGE_OF, FORMULA
    taxable BOOLEAN NOT NULL DEFAULT TRUE,
    affects_gross BOOLEAN NOT NULL DEFAULT TRUE,
    affects_net BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_salary_components_code UNIQUE (company_id, component_code)
);

CREATE TABLE IF NOT EXISTS employee_compensations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    pay_group_id UUID NOT NULL REFERENCES pay_groups(id),
    effective_from DATE NOT NULL,
    effective_to DATE,
    ctc_annual NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    gross_monthly NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS compensation_components (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compensation_id UUID NOT NULL REFERENCES employee_compensations(id) ON DELETE CASCADE,
    component_id UUID NOT NULL REFERENCES salary_components(id),
    amount_monthly NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    percentage_value NUMERIC(7, 4),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS payroll_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    legal_entity_id UUID NOT NULL,
    pay_group_id UUID NOT NULL REFERENCES pay_groups(id),
    run_code VARCHAR(60) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    run_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR', -- REGULAR, OFF_CYCLE, BONUS, FNF
    total_employees INT NOT NULL DEFAULT 0,
    total_gross NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_deductions NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_net NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_CALCULATION, CALCULATED, REVIEWED, APPROVED, LOCKED, DISBURSED
    created_by UUID,
    approved_by UUID,
    locked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT uq_payroll_run_code UNIQUE (company_id, run_code)
);

CREATE TABLE IF NOT EXISTS payslips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payroll_run_id UUID NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL,
    company_id UUID NOT NULL,
    pay_group_id UUID NOT NULL,
    days_in_month INT NOT NULL DEFAULT 30,
    paid_days NUMERIC(6, 2) NOT NULL DEFAULT 30.00,
    lop_days NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    gross_earnings NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_deductions NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    net_pay NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, PAID, FAILED
    pdf_storage_path VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_payslip_run_emp UNIQUE (payroll_run_id, employee_id)
);

CREATE TABLE IF NOT EXISTS calculation_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payslip_id UUID NOT NULL REFERENCES payslips(id) ON DELETE CASCADE,
    component_id UUID NOT NULL REFERENCES salary_components(id),
    component_code VARCHAR(50) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(40) NOT NULL,
    amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    ytd_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    is_statutory BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Transactional Outbox
CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    topic VARCHAR(150) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Idempotent Inbox
CREATE TABLE IF NOT EXISTS inbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSED',
    CONSTRAINT uq_inbox_payroll UNIQUE (event_id, consumer_group)
);

CREATE INDEX IF NOT EXISTS idx_payroll_runs_period ON payroll_runs(company_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_payslips_run ON payslips(payroll_run_id);
CREATE INDEX IF NOT EXISTS idx_payslips_emp ON payslips(employee_id);
CREATE INDEX IF NOT EXISTS idx_calc_lines_payslip ON calculation_lines(payslip_id);
CREATE INDEX IF NOT EXISTS idx_comp_emp ON employee_compensations(company_id, employee_id, is_active);

