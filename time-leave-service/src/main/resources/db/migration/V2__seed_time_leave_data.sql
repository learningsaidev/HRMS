-- ==============================================================================
-- ABCD Time, Attendance & Leave Service - Seed Data V2
-- ==============================================================================

-- Seed General Shift
INSERT INTO shift (id, company_id, code, name, start_time, end_time, grace_period_minutes, half_day_hours, full_day_hours, created_by, updated_by)
VALUES
    ('50000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'GEN-SHIFT', 'Standard General Shift (09:00 - 18:00)', '09:00:00', '18:00:00', 15, 4.00, 8.00, 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed Standard Leave Types
INSERT INTO leave_type (id, company_id, code, name, is_paid, max_days_per_year, can_carry_forward, is_encashable, created_by, updated_by)
VALUES
    ('60000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'CL', 'Casual Leave', TRUE, 12.00, FALSE, FALSE, 'SEED', 'SEED'),
    ('60000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'SL', 'Sick / Medical Leave', TRUE, 12.00, TRUE, FALSE, 'SEED', 'SEED'),
    ('60000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'EL', 'Earned / Privilege Leave', TRUE, 18.00, TRUE, TRUE, 'SEED', 'SEED'),
    ('60000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'LOP', 'Loss of Pay (Unpaid Leave)', FALSE, 365.00, FALSE, FALSE, 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed Opening Balance Ledger for John Doe
INSERT INTO leave_ledger (id, company_id, employee_id, leave_type_id, transaction_units, entry_type, effective_date, reference_id, balance_after, created_by, updated_by)
VALUES
    ('70000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', 10.00, 'OPENING', '2026-01-01', 'OPENING_2026', 10.00, 'SEED', 'SEED'),
    ('70000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000002', 10.00, 'OPENING', '2026-01-01', 'OPENING_2026', 10.00, 'SEED', 'SEED'),
    ('70000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000003', 15.00, 'OPENING', '2026-01-01', 'OPENING_2026', 15.00, 'SEED', 'SEED')
ON CONFLICT DO NOTHING;
