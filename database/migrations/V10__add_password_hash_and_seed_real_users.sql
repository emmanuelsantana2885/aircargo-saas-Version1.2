ALTER TABLE app_user ADD COLUMN IF NOT EXISTS password_hash varchar(255);

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'jsantos@rannik.com', 'Jose Santos', 'ADMIN', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'jsantos@rannik.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'esantana@rannik.com', 'Edward Santana', 'SUPER_USER', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'esantana@rannik.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'dchestaro@rannik.com', 'Danny Chestaro', 'OPERATIONS', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'dchestaro@rannik.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'ilsantana@rannik.com', 'Ilsa Santana', 'WAREHOUSE_ASSISTANT', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'ilsantana@rannik.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'earellano@ups.com', 'Eduardo Arellano', 'TRAFFIC', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'earellano@ups.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'jcastrolopez@ups.com', 'Jairo Castro', 'LOAD_PLANNER', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'jcastrolopez@ups.com');
