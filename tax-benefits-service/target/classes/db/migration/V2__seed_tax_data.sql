-- V2__seed_tax_data.sql
-- Seed statutory tax slabs and demo loans for testing

INSERT INTO tax_regimes (id, employee_id, financial_year, selected_regime, locked, created_by, updated_by)
VALUES ('70000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', '2026-2027', 'NEW', true, 'SYSTEM', 'SYSTEM')
ON CONFLICT (employee_id, financial_year) DO NOTHING;

