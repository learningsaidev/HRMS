-- ==============================================================================
-- ABCD Tax, Deductions & Benefits Service - Schema Migration V1
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS abcd_tax_benefits;

CREATE TABLE IF NOT EXISTS tax_regimes (
    id UUID PRIMARY KEY,
    company_id UUID,
    employee_id UUID NOT NULL,
    financial_year VARCHAR(20) NOT NULL,
    selected_regime VARCHAR(20) NOT NULL, -- NEW, OLD
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_employee_fy UNIQUE (employee_id, financial_year)
);

CREATE TABLE IF NOT EXISTS tax_declarations (
    id UUID PRIMARY KEY,
    company_id UUID,
    employee_id UUID NOT NULL,
    financial_year VARCHAR(20) NOT NULL,
    regime VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- DRAFT, SUBMITTED, VERIFIED, REJECTED
    total_declared NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_verified NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_decl_emp_fy UNIQUE (employee_id, financial_year)
);

CREATE TABLE IF NOT EXISTS declaration_items (
    id UUID PRIMARY KEY,
    declaration_id UUID NOT NULL REFERENCES tax_declarations(id) ON DELETE CASCADE,
    section VARCHAR(50) NOT NULL, -- 80C, 80D, 10(13A), 80CCD, 24(B)
    category VARCHAR(100) NOT NULL,
    declared_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    verified_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    max_limit NUMERIC(18, 4) NOT NULL DEFAULT 150000.0000,
    proof_document_url VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS loan_masters (
    id UUID PRIMARY KEY,
    company_id UUID,
    employee_id UUID NOT NULL,
    loan_type VARCHAR(50) NOT NULL, -- EMERGENCY_MEDICAL, HIGHER_EDUCATION, RELOCATION_ADVANCE
    principal_amount NUMERIC(18, 4) NOT NULL,
    annual_interest_rate NUMERIC(5, 2) NOT NULL DEFAULT 7.50,
    tenure_months INT NOT NULL,
    monthly_emi NUMERIC(18, 4) NOT NULL,
    total_interest NUMERIC(18, 4) NOT NULL,
    disbursement_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SANCTIONED', -- APPLIED, SANCTIONED, DISBURSED, CLOSED, DEFAULTED
    approved_by VARCHAR(100),
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS loan_installments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL REFERENCES loan_masters(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    principal_component NUMERIC(18, 4) NOT NULL,
    interest_component NUMERIC(18, 4) NOT NULL,
    total_installment_amount NUMERIC(18, 4) NOT NULL,
    remaining_principal NUMERIC(18, 4) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, DEDUCTED, RECOVERED, WAIVED
    deducted_payroll_run_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE
);

