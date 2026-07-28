-- V38__create_service_schemas.sql
-- Create separate schemas for each microservice

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS flight;
CREATE SCHEMA IF NOT EXISTS booking;
CREATE SCHEMA IF NOT EXISTS mawb;
CREATE SCHEMA IF NOT EXISTS warehouse;
CREATE SCHEMA IF NOT EXISTS uld;
CREATE SCHEMA IF NOT EXISTS load_planning;
CREATE SCHEMA IF NOT EXISTS export_bi;
CREATE SCHEMA IF NOT EXISTS notification;

-- Move auth tables to auth schema
ALTER TABLE IF EXISTS app_user SET SCHEMA auth;
ALTER TABLE IF EXISTS site SET SCHEMA auth;
ALTER TABLE IF EXISTS user_sites SET SCHEMA auth;
ALTER TABLE IF EXISTS audit_log SET SCHEMA auth;
ALTER TABLE IF EXISTS role_permission SET SCHEMA auth;
ALTER TABLE IF EXISTS view_permission SET SCHEMA auth;

-- Move flight tables to flight schema
ALTER TABLE IF EXISTS flight SET SCHEMA flight;
ALTER TABLE IF EXISTS airline SET SCHEMA flight;
ALTER TABLE IF EXISTS uld_type_config SET SCHEMA flight;

-- Move booking tables to booking schema
ALTER TABLE IF EXISTS booking SET SCHEMA booking;

-- Move mawb tables to mawb schema
ALTER TABLE IF EXISTS mawb SET SCHEMA mawb;
ALTER TABLE IF EXISTS hawb SET SCHEMA mawb;

-- Move warehouse tables to warehouse schema
ALTER TABLE IF EXISTS warehouse_receipt SET SCHEMA warehouse;
ALTER TABLE IF EXISTS receipt_piece SET SCHEMA warehouse;

-- Move uld tables to uld schema
ALTER TABLE IF EXISTS uld SET SCHEMA uld;
ALTER TABLE IF EXISTS uld_awb SET SCHEMA uld;
ALTER TABLE IF EXISTS uld_piece SET SCHEMA uld;

-- Create notification table in notification schema
CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    entity_type VARCHAR(50),
    entity_id UUID,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now()
);

-- Grant permissions for each schema
GRANT ALL ON SCHEMA auth TO aircargo_user;
GRANT ALL ON SCHEMA flight TO aircargo_user;
GRANT ALL ON SCHEMA booking TO aircargo_user;
GRANT ALL ON SCHEMA mawb TO aircargo_user;
GRANT ALL ON SCHEMA warehouse TO aircargo_user;
GRANT ALL ON SCHEMA uld TO aircargo_user;
GRANT ALL ON SCHEMA load_planning TO aircargo_user;
GRANT ALL ON SCHEMA export_bi TO aircargo_user;
GRANT ALL ON SCHEMA notification TO aircargo_user;

-- Grant table permissions
GRANT ALL ON ALL TABLES IN SCHEMA auth TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA flight TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA booking TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA mawb TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA warehouse TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA uld TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA load_planning TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA export_bi TO aircargo_user;
GRANT ALL ON ALL TABLES IN SCHEMA notification TO aircargo_user;
