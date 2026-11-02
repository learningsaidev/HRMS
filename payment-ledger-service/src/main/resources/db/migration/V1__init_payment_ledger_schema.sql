-- V1__init_payment_ledger_schema.sql
-- ABCD HRMS Enterprise Double-Entry Ledger and Payment Rails Schema

CREATE TABLE IF NOT EXISTS ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    account_name VARCHAR(150) NOT NULL,
    account_type VARCHAR(30) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_ledger_account_code UNIQUE (company_id, account_code)
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    legal_entity_id UUID NOT NULL,
    entry_number VARCHAR(60) NOT NULL,
    entry_date DATE NOT NULL,
    source_module VARCHAR(50) NOT NULL, -- PAYROLL, EXPENSES, REIMBURSEMENTS, MANUAL
    source_reference_id VARCHAR(100),
    narration TEXT,
    total_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    is_posted BOOLEAN NOT NULL DEFAULT FALSE,
    posted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_journal_entry_num UNIQUE (company_id, entry_number)
);

CREATE TABLE IF NOT EXISTS journal_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    entry_type VARCHAR(10) NOT NULL, -- DEBIT, CREDIT
    amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS payment_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    legal_entity_id UUID NOT NULL,
    batch_reference VARCHAR(60) NOT NULL,
    source_type VARCHAR(40) NOT NULL DEFAULT 'PAYROLL',
    source_id VARCHAR(100),
    payment_method VARCHAR(30) NOT NULL DEFAULT 'NEFT', -- NEFT, RTGS, IMPS, NACH
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    total_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    total_transactions INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PROCESSING, COMPLETED, PARTIALLY_FAILED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_payment_batch_ref UNIQUE (company_id, batch_reference)
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL REFERENCES payment_batches(id) ON DELETE CASCADE,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    beneficiary_name VARCHAR(150) NOT NULL,
    account_number_encrypted VARCHAR(255) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    utr_number VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_TRANSIT, SUCCESS, FAILED, RETURNED
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS bank_reconciliations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    bank_statement_reference VARCHAR(100) NOT NULL,
    transaction_date DATE NOT NULL,
    statement_amount NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    matched_transaction_id UUID REFERENCES payment_transactions(id),
    status VARCHAR(30) NOT NULL DEFAULT 'MATCHED', -- MATCHED, UNMATCHED, DISCREPANCY
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
    CONSTRAINT uq_inbox_payment UNIQUE (event_id, consumer_group)
);

CREATE INDEX IF NOT EXISTS idx_journal_entries_date ON journal_entries(company_id, entry_date);
CREATE INDEX IF NOT EXISTS idx_journal_lines_entry ON journal_lines(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_pay_batches_status ON payment_batches(company_id, status);
CREATE INDEX IF NOT EXISTS idx_pay_tx_batch ON payment_transactions(batch_id, status);
