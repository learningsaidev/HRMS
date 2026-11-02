-- ==============================================================================
-- ABCD Workforce Service - Seed Data V2
-- ==============================================================================

-- Seed Parent Company
INSERT INTO company (id, code, legal_name, tax_identifier, country_code, base_currency, status, created_by, updated_by)
VALUES
    ('d0000000-0000-0000-0000-000000000001', 'ABCD_CORP', 'ABCD Corporation Global Holdings Ltd', 'TAX-ABCD-GLOBAL', 'IND', 'INR', 'ACTIVE', 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed 3 Legal Entities
INSERT INTO legal_entity (id, company_id, entity_code, legal_name, registration_number, tax_identifier, country_code, currency, status, created_by, updated_by)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'ABCD_INDIA', 'ABCD Technologies India Pvt Ltd', 'U72200KA2020PTC123456', '29AABCA1234F1Z5', 'IND', 'INR', 'ACTIVE', 'SEED', 'SEED'),
    ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'ABCD_US', 'ABCD Inc (United States)', 'REG-US-998877', 'EIN-12-3456789', 'USA', 'USD', 'ACTIVE', 'SEED', 'SEED'),
    ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'ABCD_UK', 'ABCD Technologies UK Limited', 'REG-UK-554433', 'VAT-GB-123456789', 'GBR', 'GBP', 'ACTIVE', 'SEED', 'SEED')
ON CONFLICT (entity_code) DO NOTHING;

-- Seed Departments
INSERT INTO department (id, company_id, legal_entity_id, code, name, created_by, updated_by)
VALUES
    ('f0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'ENG', 'Product Engineering', 'SEED', 'SEED'),
    ('f0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'HR', 'Human Capital Management', 'SEED', 'SEED'),
    ('f0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'FIN', 'Finance, Treasury & Payroll', 'SEED', 'SEED'),
    ('f0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'SALES', 'Global Enterprise Sales', 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed Locations
INSERT INTO location (id, company_id, legal_entity_id, code, name, city, state, country_code, is_headquarters, created_by, updated_by)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'BLR-HQ', 'Bangalore Headquarters', 'Bangalore', 'Karnataka', 'IND', TRUE, 'SEED', 'SEED'),
    ('10000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'MUM-BR', 'Mumbai Operations Centre', 'Mumbai', 'Maharashtra', 'IND', FALSE, 'SEED', 'SEED'),
    ('10000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000002', 'NYC-OFF', 'New York Manhattan Office', 'New York', 'New York', 'USA', FALSE, 'SEED', 'SEED'),
    ('10000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003', 'LDN-OFF', 'London Tech Hub', 'London', 'Greater London', 'GBR', FALSE, 'SEED', 'SEED')
ON CONFLICT (code) DO NOTHING;

-- Seed Employees
INSERT INTO employee (id, company_id, employee_number, first_name, last_name, display_name, date_of_birth, gender, marital_status, work_email, current_status, national_id_masked, bank_account_masked, bank_name, bank_ifsc_or_routing, created_by, updated_by)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'ABCD-001', 'Bob', 'Martin', 'Bob Martin', '1980-05-12', 'MALE', 'MARRIED', 'bob.martin@abcd.corp', 'ACTIVE', 'XXXXX1111A', 'XXXXXX1111', 'HDFC Bank', 'HDFC0001234', 'SEED', 'SEED'),
    ('20000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'ABCD-002', 'Alice', 'Smith', 'Alice Smith', '1985-08-23', 'FEMALE', 'MARRIED', 'hr.admin@abcd.corp', 'ACTIVE', 'XXXXX2222B', 'XXXXXX2222', 'ICICI Bank', 'ICIC0005678', 'SEED', 'SEED'),
    ('20000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'ABCD-003', 'Carol', 'Davis', 'Carol Davis', '1982-11-05', 'FEMALE', 'SINGLE', 'finance.director@abcd.corp', 'ACTIVE', 'XXXXX3333C', 'XXXXXX3333', 'State Bank of India', 'SBIN0009988', 'SEED', 'SEED'),
    ('20000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'ABCD-101', 'John', 'Doe', 'John Doe', '1992-03-15', 'MALE', 'MARRIED', 'john.doe@abcd.corp', 'ACTIVE', 'XXXXX4444D', 'XXXXXX4444', 'Axis Bank', 'UTIB0003322', 'SEED', 'SEED')
ON CONFLICT (employee_number) DO NOTHING;

-- Seed Employments
INSERT INTO employment (id, employee_id, legal_entity_id, department_id, location_id, employment_type, designation, grade, joining_date, confirmation_date, notice_period_days, created_by, updated_by)
VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'FULL_TIME', 'VP of Engineering', 'E8', '2020-01-15', '2020-07-15', 90, 'SEED', 'SEED'),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'FULL_TIME', 'Director of Human Resources', 'M5', '2020-03-01', '2020-09-01', 90, 'SEED', 'SEED'),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'FULL_TIME', 'Finance & Treasury Director', 'M5', '2020-04-15', '2020-10-15', 90, 'SEED', 'SEED'),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'FULL_TIME', 'Senior Software Engineer', 'IC4', '2021-06-01', '2021-12-01', 60, 'SEED', 'SEED')
ON CONFLICT DO NOTHING;

-- Seed Manager Relationships: John Doe reports to Bob Martin
INSERT INTO manager_relationship (id, employee_id, manager_employee_id, relationship_type, is_active, created_by, updated_by)
VALUES
    ('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'PRIMARY', TRUE, 'SEED', 'SEED')
ON CONFLICT DO NOTHING;
