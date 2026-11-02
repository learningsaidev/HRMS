-- V2__seed_ledger_data.sql
-- Seed Standard Chart of Accounts for ABCD Corporation

INSERT INTO ledger_accounts (id, company_id, account_code, account_name, account_type, currency, is_active, created_by, updated_by)
VALUES 
    ('e1000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'EXP-SAL-001', 'Payroll Salaries Expense', 'EXPENSE', 'INR', true, 'SYSTEM', 'SYSTEM'),
    ('e1000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'LIA-SAL-001', 'Salaries Payable Clearing', 'LIABILITY', 'INR', true, 'SYSTEM', 'SYSTEM'),
    ('e1000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'LIA-PF-001', 'Provident Fund Payable', 'LIABILITY', 'INR', true, 'SYSTEM', 'SYSTEM'),
    ('e1000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'LIA-TDS-001', 'Income Tax TDS Payable', 'LIABILITY', 'INR', true, 'SYSTEM', 'SYSTEM'),
    ('e1000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001', 'AST-BNK-001', 'Corporate Operating Bank Account (HDFC/ICICI)', 'ASSET', 'INR', true, 'SYSTEM', 'SYSTEM')
ON CONFLICT (account_code) DO NOTHING;

