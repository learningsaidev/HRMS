-- ==============================================================================
-- ABCD Identity & Access Service - Sync Standard User Passwords V3
-- Ensures seeded users have the verified 12-round BCrypt hash for 'Password@123'
-- ==============================================================================

UPDATE users
SET password_hash = '$2a$12$jaB28jlNJ7YkOGLQ.k9uQ.02cnBB5m.rf4YDdU5yRNSlWazmX2f6e',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'MIGRATION_V3'
WHERE username IN (
    'admin',
    'hr.admin',
    'payroll.maker',
    'payroll.checker',
    'finance.director',
    'john.doe'
);

