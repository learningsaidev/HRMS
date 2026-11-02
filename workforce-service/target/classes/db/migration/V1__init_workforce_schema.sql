-- ==============================================================================
-- ABCD Workforce Service - Schema Migration V1
-- ==============================================================================

CREATE TABLE IF NOT EXISTS company (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    legal_name VARCHAR(255) NOT NULL,
    tax_identifier VARCHAR(100),
    country_code VARCHAR(3) NOT NULL DEFAULT 'IND',
    base_currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS legal_entity (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    entity_code VARCHAR(50) NOT NULL UNIQUE,
    legal_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100),
    tax_identifier VARCHAR(100),
    country_code VARCHAR(3) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS department (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    legal_entity_id UUID REFERENCES legal_entity(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    parent_department_id UUID REFERENCES department(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS location (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    legal_entity_id UUID REFERENCES legal_entity(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country_code VARCHAR(3) NOT NULL,
    is_headquarters BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS employee (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    employee_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    marital_status VARCHAR(20),
    personal_email VARCHAR(255),
    work_email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(30),
    national_id_encrypted TEXT,
    national_id_masked VARCHAR(50),
    bank_account_encrypted TEXT,
    bank_account_masked VARCHAR(50),
    bank_name VARCHAR(100),
    bank_ifsc_or_routing VARCHAR(50),
    current_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_emp_company_status ON employee(company_id, current_status);

CREATE TABLE IF NOT EXISTS employment (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    legal_entity_id UUID NOT NULL REFERENCES legal_entity(id),
    department_id UUID NOT NULL REFERENCES department(id),
    location_id UUID NOT NULL REFERENCES location(id),
    employment_type VARCHAR(30) NOT NULL DEFAULT 'FULL_TIME',
    designation VARCHAR(100) NOT NULL,
    grade VARCHAR(20),
    joining_date DATE NOT NULL,
    confirmation_date DATE,
    notice_period_days INT NOT NULL DEFAULT 60,
    exit_date DATE,
    exit_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_employment_dept_loc ON employment(department_id, location_id);

CREATE TABLE IF NOT EXISTS manager_relationship (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    manager_employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    relationship_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY', -- PRIMARY, DOTTED_LINE
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
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

CREATE INDEX IF NOT EXISTS idx_wrk_outbox_status ON outbox_event(status, created_at);

CREATE TABLE IF NOT EXISTS inbox_event (
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_group)
);
