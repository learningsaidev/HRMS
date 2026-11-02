-- V2__seed_payroll_data.sql
-- Default Salary Components & Standard Pay Group

INSERT INTO pay_groups (id, company_id, legal_entity_id, group_code, group_name, pay_frequency, currency, salary_disbursement_day, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000001',
    'e0000000-0000-0000-0000-000000000001',
    'PG_CORP_MONTHLY',
    'ABCD Corporate Monthly Pay Group',
    'MONTHLY',
    'INR',
    30,
    TRUE
) ON CONFLICT (company_id, group_code) DO NOTHING;

-- Standard Earnings
INSERT INTO salary_components (id, company_id, component_code, component_name, component_type, calculation_type, taxable, affects_gross, affects_net, display_order)
VALUES 
    ('c0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'BASIC', 'Basic Salary', 'EARNING', 'FIXED', TRUE, TRUE, TRUE, 1),
    ('c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'HRA', 'House Rent Allowance', 'EARNING', 'PERCENTAGE_OF', TRUE, TRUE, TRUE, 2),
    ('c0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'SPECIAL_ALLOWANCE', 'Special Allowance', 'EARNING', 'FIXED', TRUE, TRUE, TRUE, 3),
    ('c0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'CONVEYANCE', 'Conveyance Allowance', 'EARNING', 'FIXED', TRUE, TRUE, TRUE, 4),
    ('c0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', 'MEDICAL_ALLOWANCE', 'Medical Allowance', 'EARNING', 'FIXED', TRUE, TRUE, TRUE, 5)
ON CONFLICT (company_id, component_code) DO NOTHING;

-- Standard Deductions & Statutory Items
INSERT INTO salary_components (id, company_id, component_code, component_name, component_type, calculation_type, taxable, affects_gross, affects_net, display_order)
VALUES 
    ('c0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001', 'PF_EMPLOYEE', 'Provident Fund (Employee)', 'STATUTORY_EMPLOYEE', 'PERCENTAGE_OF', FALSE, FALSE, TRUE, 10),
    ('c0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000001', 'PF_EMPLOYER', 'Provident Fund (Employer)', 'STATUTORY_EMPLOYER', 'PERCENTAGE_OF', FALSE, FALSE, FALSE, 11),
    ('c0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000001', 'PROFESSIONAL_TAX', 'Professional Tax (PT)', 'STATUTORY_EMPLOYEE', 'FIXED', FALSE, FALSE, TRUE, 12),
    ('c0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000001', 'ESI_EMPLOYEE', 'Employee State Insurance', 'STATUTORY_EMPLOYEE', 'PERCENTAGE_OF', FALSE, FALSE, TRUE, 13),
    ('c0000000-0000-0000-0000-000000000010', 'd0000000-0000-0000-0000-000000000001', 'TDS', 'Tax Deducted at Source', 'DEDUCTION', 'FIXED', FALSE, FALSE, TRUE, 14)
ON CONFLICT (company_id, component_code) DO NOTHING;

