-- V1__init_tax_benefits_schema.sql
-- ABCD HRMS Tax Regimes, Declarations, Benefits, and Employee Loans Schema

CREATE TABLE IF NOT EXISTS tax_regimes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    regime_code VARCHAR(30) NOT NULL, -- OLD, NEW
    regime_name VARCHAR(100) NOT NULL,
    financial_year VARCHAR(20) NOT NULL, -- 2026-2027
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    standard_deduction NUMERIC(18, 4) NOT NULL DEFAULT 50000.0000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_regime_fy UNIQUE (regime_code, financial_year)
);

CREATE TABLE IF NOT EXISTS tax_declarations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    financial_year VARCHAR(20) NOT NULL,
    regime_code VARCHAR(30) NOT NULL DEFAULT 'NEW',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, VERIFIED, REJECTED
    total_declared_deductions NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_verified_deductions NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_emp_tax_decl UNIQUE (company_id, employee_id, financial_year)
);

CREATE TABLE IF NOT EXISTS declaration_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_id UUID NOT NULL REFERENCES tax_declarations(id) ON DELETE CASCADE,
    section_code VARCHAR(50) NOT NULL, -- 80C, 80D, 80E, 24B, HRA
    description VARCHAR(255),
    declared_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    verified_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    proof_storage_path VARCHAR(500),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS loan_masters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    loan_type VARCHAR(50) NOT NULL DEFAULT 'SALARY_ADVANCE', -- SALARY_ADVANCE, HOME_LOAN, PERSONAL_LOAN
    principal_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    interest_rate NUMERIC(7, 4) NOT NULL DEFAULT 0.0000,
    tenure_months INT NOT NULL DEFAULT 12,
    monthly_emi NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    disbursement_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, ACTIVE, CLOSED, REJECTED
    total_paid NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    balance_principal NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS loan_installments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL REFERENCES loan_masters(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    emi_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    principal_component NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    interest_component NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, DEDUCTED, WAIVED
    payroll_run_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
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
    CONSTRAINT uq_inbox_tax UNIQUE (event_id, consumer_group)
);

CREATE INDEX IF NOT EXISTS idx_tax_decl_emp ON tax_declarations(company_id, employee_id, financial_year);
CREATE INDEX IF NOT EXISTS idx_decl_items_decl ON declaration_items(declaration_id);
CREATE INDEX IF NOT EXISTS idx_loan_emp ON loan_masters(company_id, employee_id, status);
CREATE INDEX IF NOT EXISTS idx_loan_inst_due ON loan_installments(loan_id, status, due_date);
