-- ==============================================================================
-- ABCD Onboarding & Background Verification Service - Seed Data V2
-- ==============================================================================

-- Seed Preboarding Candidate
INSERT INTO onboarding_case (id, company_id, candidate_name, candidate_email, phone_number, target_legal_entity_id, target_department_id, target_location_id, proposed_designation, proposed_joining_date, secure_token, status, created_by, updated_by)
VALUES
    ('80000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'David Miller', 'david.miller.cand@example.com', '+91 9876543210', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Staff Systems Engineer', '2026-12-01', 'cand-sec-tok-david-miller-01', 'PREBOARDING', 'SEED', 'SEED')
ON CONFLICT (secure_token) DO NOTHING;
