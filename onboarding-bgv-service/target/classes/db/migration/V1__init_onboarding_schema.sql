-- ==============================================================================
-- ABCD Onboarding & Background Verification Service - Schema Migration V1
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS abcd_onboarding;

CREATE TABLE IF NOT EXISTS onboarding_case (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    candidate_name VARCHAR(150) NOT NULL,
    candidate_email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30),
    target_legal_entity_id UUID NOT NULL,
    target_department_id UUID NOT NULL,
    target_location_id UUID NOT NULL,
    proposed_designation VARCHAR(100) NOT NULL,
    proposed_joining_date DATE NOT NULL,
    secure_token VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PREBOARDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS candidate_document (
    id UUID PRIMARY KEY,
    onboarding_case_id UUID NOT NULL REFERENCES onboarding_case(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS consent_record (
    id UUID PRIMARY KEY,
    onboarding_case_id UUID NOT NULL REFERENCES onboarding_case(id) ON DELETE CASCADE,
    consent_text TEXT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(255),
    consented_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consent_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bgv_case (
    id UUID PRIMARY KEY,
    onboarding_case_id UUID NOT NULL REFERENCES onboarding_case(id) ON DELETE CASCADE,
    vendor_name VARCHAR(100) NOT NULL DEFAULT 'ABCD_INTERNAL_REVIEW',
    vendor_reference_id VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    manual_reviewer_notes TEXT,
    decided_by UUID,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bgv_check_item (
    id UUID PRIMARY KEY,
    bgv_case_id UUID NOT NULL REFERENCES bgv_case(id) ON DELETE CASCADE,
    check_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    discrepancy_notes VARCHAR(500),
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

CREATE TABLE IF NOT EXISTS inbox_event (
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_group)
);
