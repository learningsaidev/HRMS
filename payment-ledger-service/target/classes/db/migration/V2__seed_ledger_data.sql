-- V2__seed_ledger_data.sql
-- Default Standard Chart of Accounts for ABCD Corporation

INSERT INTO ledger_accounts (id, company_id, account_code, account_name, account_type, currency, is_active)
VALUES 
    ('f0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', '5001', 'Salaries and Wages Expense', 'EXPENSE', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', '5002', 'Employer PF Contribution Expense', 'EXPENSE', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', '2001', 'Salaries Payable Clearing Account', 'LIABILITY', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', '2002', 'Provident Fund Payable', 'LIABILITY', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', '2003', 'Professional Tax Payable', 'LIABILITY', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001', '2004', 'TDS (Income Tax) Payable', 'LIABILITY', 'INR', TRUE),
    ('f0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000001', '1001', 'Corporate Operating Bank Account', 'ASSET', 'INR', TRUE)
ON CONFLICT (company_id, account_code) DO NOTHING;
