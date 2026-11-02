-- ==============================================================================
-- ABCD Time, Attendance & Leave Service - Schema Migration V1
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS abcd_time_leave;

CREATE TABLE IF NOT EXISTS shift (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    grace_period_minutes INT NOT NULL DEFAULT 15,
    half_day_hours NUMERIC(5, 2) NOT NULL DEFAULT 4.00,
    full_day_hours NUMERIC(5, 2) NOT NULL DEFAULT 8.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS attendance_punch (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    punch_time TIMESTAMP WITH TIME ZONE NOT NULL,
    punch_type VARCHAR(10) NOT NULL, -- IN, OUT
    device_id VARCHAR(50),
    location VARCHAR(100),
    verification_mode VARCHAR(30) DEFAULT 'WEB_PORTAL',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_punch_emp_time ON attendance_punch(employee_id, punch_time);

CREATE TABLE IF NOT EXISTS daily_attendance (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    shift_id UUID REFERENCES shift(id),
    first_punch_in TIMESTAMP WITH TIME ZONE,
    last_punch_out TIMESTAMP WITH TIME ZONE,
    total_worked_hours NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    overtime_hours NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, HALF_DAY, ON_LEAVE, WEEKLY_OFF, HOLIDAY
    is_regularized BOOLEAN NOT NULL DEFAULT FALSE,
    regularization_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_emp_attendance_date UNIQUE (employee_id, attendance_date)
);

CREATE TABLE IF NOT EXISTS timesheet (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_payable_hours NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    total_overtime_hours NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- DRAFT, SUBMITTED, APPROVED, REJECTED
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS leave_type (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    max_days_per_year NUMERIC(5, 2) NOT NULL DEFAULT 12.00,
    can_carry_forward BOOLEAN NOT NULL DEFAULT TRUE,
    is_encashable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS leave_application (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    leave_type_id UUID NOT NULL REFERENCES leave_type(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days NUMERIC(5, 2) NOT NULL,
    is_half_day BOOLEAN NOT NULL DEFAULT FALSE,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, CANCELLED
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

-- Immutable Leave Transaction Ledger
CREATE TABLE IF NOT EXISTS leave_ledger (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    leave_type_id UUID NOT NULL REFERENCES leave_type(id),
    transaction_units NUMERIC(6, 2) NOT NULL, -- Positive for accrual/opening, negative for consumption
    entry_type VARCHAR(30) NOT NULL, -- OPENING, ACCRUAL, CONSUMPTION, LAPSE, ENCASHMENT, ADJUSTMENT
    effective_date DATE NOT NULL,
    reference_id VARCHAR(100), -- E.g. Leave Application UUID
    balance_after NUMERIC(6, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_leave_ledger_emp_type ON leave_ledger(employee_id, leave_type_id, effective_date);

CREATE TABLE IF NOT EXISTS attendance_lock (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT TRUE,
    locked_by VARCHAR(100) NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_company_year_month UNIQUE (company_id, period_year, period_month)
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    destination_topic VARCHAR(150) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS inbox_event (
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_group)
);
