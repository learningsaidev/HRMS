-- ==============================================================================
-- ABCD Identity & Access Service - Seed Data V2
-- ==============================================================================

-- Seed Standard Roles
INSERT INTO roles (id, name, description, is_system_role, created_by, updated_by)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ROLE_SYSTEM_ADMIN', 'Platform Technical Administrator', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000002', 'ROLE_HR_ADMIN', 'Human Resources Lifecycle Administrator', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000003', 'ROLE_MANAGER', 'People Manager for Team Approvals', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000004', 'ROLE_EMPLOYEE', 'Employee Self-Service Access', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000005', 'ROLE_PAYROLL_MAKER', 'Payroll Specialist for Period Runs & Calculations', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000006', 'ROLE_PAYROLL_CHECKER', 'Payroll Approver for Review & Period Lock', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000007', 'ROLE_FINANCE_DIRECTOR', 'Finance Disburser & Ledger Administrator', TRUE, 'SEED', 'SEED'),
    ('a0000000-0000-0000-0000-000000000008', 'ROLE_COMPLIANCE_OFFICER', 'Statutory Auditor & Unmasked PII Reviewer', TRUE, 'SEED', 'SEED')
ON CONFLICT (name) DO NOTHING;

-- Seed Standard Permissions
INSERT INTO permissions (id, code, description, module, created_by, updated_by)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'workforce:read', 'View workforce directory and org chart', 'WORKFORCE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000002', 'workforce:write', 'Create and modify employee master records', 'WORKFORCE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000003', 'time:punch', 'Clock in and out', 'TIME', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000004', 'time:timesheet:approve', 'Approve team timesheets', 'TIME', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000005', 'leave:apply', 'Submit leave applications', 'LEAVE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000006', 'leave:approve', 'Approve direct report leaves', 'LEAVE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000007', 'onboarding:read', 'Access candidate onboarding queue', 'ONBOARDING', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000008', 'bgv:review', 'Review background verification checks', 'ONBOARDING', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000009', 'payroll:simulate', 'Execute payroll simulations', 'PAYROLL', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000010', 'payroll:calculate', 'Execute chunked gross-to-net calculations', 'PAYROLL', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000011', 'payroll:approve', 'Authorize reviewed payroll runs', 'PAYROLL', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000012', 'payroll:lock', 'Execute month-end payroll freeze', 'PAYROLL', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000013', 'payslip:read:self', 'Download personal digital payslips', 'PAYROLL', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000014', 'payment:release', 'Release corporate banking disbursement batches', 'FINANCE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000015', 'payment:reversal', 'Authorize payment return compensating entries', 'FINANCE', 'SEED', 'SEED'),
    ('b0000000-0000-0000-0000-000000000016', 'ledger:view', 'View balanced double-entry general ledger journals', 'FINANCE', 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed Default ABCD Users (BCrypt hashed for 'Password@123')
INSERT INTO users (id, username, email, password_hash, status, created_by, updated_by)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'admin', 'admin@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED'),
    ('c0000000-0000-0000-0000-000000000002', 'hr.admin', 'hr.admin@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED'),
    ('c0000000-0000-0000-0000-000000000003', 'payroll.maker', 'payroll.maker@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED'),
    ('c0000000-0000-0000-0000-000000000004', 'payroll.checker', 'payroll.checker@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED'),
    ('c0000000-0000-0000-0000-000000000005', 'finance.director', 'finance.director@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED'),
    ('c0000000-0000-0000-0000-000000000006', 'john.doe', 'john.doe@abcd.corp', '$2a$12$e8Y5t1hK8o.y61nF6J4e82pW43U1h4C7/rUqN5eM2rF7.4W0rQ.Se', 'ACTIVE', 'SEED', 'SEED')
ON CONFLICT (username) DO NOTHING;

-- Map User Roles
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'), -- admin -> ROLE_SYSTEM_ADMIN
    ('c0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002'), -- hr.admin -> ROLE_HR_ADMIN
    ('c0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000005'), -- payroll.maker -> ROLE_PAYROLL_MAKER
    ('c0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000006'), -- payroll.checker -> ROLE_PAYROLL_CHECKER
    ('c0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000007'), -- finance.director -> ROLE_FINANCE_DIRECTOR
    ('c0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000004')  -- john.doe -> ROLE_EMPLOYEE
ON CONFLICT DO NOTHING;
