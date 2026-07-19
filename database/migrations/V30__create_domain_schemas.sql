-- Fase 1.4: Separación de schemas por dominio
-- Cada microservicio tendrá su propio schema

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS flight;
CREATE SCHEMA IF NOT EXISTS booking;
CREATE SCHEMA IF NOT EXISTS warehouse;
CREATE SCHEMA IF NOT EXISTS mawb;
CREATE SCHEMA IF NOT EXISTS uld;
CREATE SCHEMA IF NOT EXISTS load_planning;
CREATE SCHEMA IF NOT EXISTS export_schema;
CREATE SCHEMA IF NOT EXISTS notification;

-- Migrar tablas existentes a sus schemas (comentado — se ejecuta manualmente en Phase 2)
-- ALTER TABLE app_user SET SCHEMA auth;
-- ALTER TABLE audit_log SET SCHEMA auth;
-- ALTER TABLE site SET SCHEMA auth;
-- ALTER TABLE user_sites SET SCHEMA auth;
-- ALTER TABLE role_permission SET SCHEMA auth;
-- ALTER TABLE view_permission SET SCHEMA auth;
-- ALTER TABLE flight SET SCHEMA flight;
-- ALTER TABLE aircraft_type SET SCHEMA flight;
-- ALTER TABLE airline SET SCHEMA flight;
-- ALTER TABLE booking SET SCHEMA booking;
-- ALTER TABLE mawb SET SCHEMA mawb;
-- ALTER TABLE hawb SET SCHEMA mawb;
-- ALTER TABLE warehouse_receipt SET SCHEMA warehouse;
-- ALTER TABLE receipt_piece SET SCHEMA warehouse;
-- ALTER TABLE uld SET SCHEMA uld;
-- ALTER TABLE uld_awb SET SCHEMA uld;
-- ALTER TABLE uld_type_config SET SCHEMA uld;
-- ALTER TABLE load_planning SET SCHEMA load_planning;
-- ALTER TABLE dua_record SET SCHEMA load_planning;
