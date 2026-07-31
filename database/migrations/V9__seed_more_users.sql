INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'warehouse@aircargo.com', 'Warehouse Assistant', 'WAREHOUSE_ASSISTANT', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'warehouse@aircargo.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'operations@aircargo.com', 'Operations User', 'OPERATIONS', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'operations@aircargo.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'admin@aircargo.com', 'Admin User', 'ADMIN', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@aircargo.com');

UPDATE app_user SET email = 'warehouse@aircargo.com', full_name = 'Warehouse Asst', role = 'WAREHOUSE_ASSISTANT'
WHERE role = 'WAREHOUSE_ASSISTANT' AND email IS NULL;

UPDATE app_user SET email = 'admin@aircargo.com', full_name = 'Admin', role = 'ADMIN'
WHERE role = 'ADMIN' AND email IS NULL;
