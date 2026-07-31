ALTER TABLE app_user ALTER COLUMN role TYPE varchar(50) USING role::text;
DROP TYPE IF EXISTS user_role;

UPDATE app_user SET role = 'WAREHOUSE_ASSISTANT' WHERE role = 'WAREHOUSE';
UPDATE app_user SET role = 'OPERATIONS' WHERE role = 'OPERATIONS';
UPDATE app_user SET role = 'ADMIN' WHERE role = 'ADMIN';

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'supervisor@aircargo.com', 'Supervisor', 'SUPER_USER', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'supervisor@aircargo.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'traffic@aircargo.com', 'Traffic User', 'TRAFFIC', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'traffic@aircargo.com');

INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at, failed_login_attempts, mfa_enabled, mfa_locked, must_change_password)
SELECT gen_random_uuid(), id, 'loadplanner@aircargo.com', 'Load Planner', 'LOAD_PLANNER', true, now(), now(), 0, false, false, false
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'loadplanner@aircargo.com');
