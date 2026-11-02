-- V2__seed_tax_data.sql
-- Default Tax Regimes for Financial Year 2026-2027

INSERT INTO tax_regimes (id, regime_code, regime_name, financial_year, is_default, standard_deduction)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'NEW', 'New Simplified Tax Regime (Default)', '2026-2027', TRUE, 75000.0000),
    ('a0000000-0000-0000-0000-000000000002', 'OLD', 'Old Tax Regime with Deductions (80C, 80D, HRA)', '2026-2027', FALSE, 50000.0000)
ON CONFLICT (regime_code, financial_year) DO NOTHING;
