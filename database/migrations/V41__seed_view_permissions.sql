-- Seed del catálogo de transacciones (view_permission) y accesos por rol (role_permission)
-- Idempotente: UPSERT por código de vista; role_permission se reconstruye por rol.

INSERT INTO view_permission (id, code, name, category, description, is_active, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'DASHBOARD',      'Dashboard',            'PRINCIPAL',       'Panel principal con métricas operativas del día', true, NOW(), NOW()),
  (gen_random_uuid(), 'BOOKINGS',       'Bookings',             'OPERACIONES',     'Gestión de reservas y asignación de AWB',          true, NOW(), NOW()),
  (gen_random_uuid(), 'RECEIPTS',       'Warehouse Receipts',   'OPERACIONES',     'Recibos de bodega, piezas y evidencias',            true, NOW(), NOW()),
  (gen_random_uuid(), 'FLIGHTS',        'Flights',              'OPERACIONES',     'Gestión de vuelos y aerolíneas',                    true, NOW(), NOW()),
  (gen_random_uuid(), 'MAWBS',          'MAWBs',                'OPERACIONES',     'MAWB, HAWB y documentos de carga',                  true, NOW(), NOW()),
  (gen_random_uuid(), 'LOAD_PLANNING',  'Load Planning',        'OPERACIONES',     'Plan de carga, manifiestos y pallet sheets',        true, NOW(), NOW()),
  (gen_random_uuid(), 'ULDS',           'ULDs',                 'OPERACIONES',     'ULD, escaneo de piezas y transferencias',           true, NOW(), NOW()),
  (gen_random_uuid(), 'USERS',          'Users',                'ADMINISTRACION',  'Usuarios conectados, roles y auditoría',            true, NOW(), NOW()),
  (gen_random_uuid(), 'SETTINGS',       'Settings',             'CONFIGURACION',   'Gestión de usuarios, roles y sitios',               true, NOW(), NOW()),
  (gen_random_uuid(), 'EXPORTS',        'Exports',              'ADMINISTRACION',  'Exportaciones y reportes',                          true, NOW(), NOW()),
  (gen_random_uuid(), 'API_CATALOG',    'API Catalog',          'CONFIGURACION',   'Catálogo de endpoints de la API',                   true, NOW(), NOW()),
  (gen_random_uuid(), 'BI',             'BI',                   'ADMINISTRACION',  'Indicadores e inteligencia de negocio',             true, NOW(), NOW())
ON CONFLICT (code) DO UPDATE SET
  name        = EXCLUDED.name,
  category    = EXCLUDED.category,
  description = EXCLUDED.description,
  is_active   = EXCLUDED.is_active,
  updated_at  = NOW();

-- Accesos por rol (se reconstruyen; esta tabla es gestionada desde la UI de Roles y Permisos)
DELETE FROM role_permission;

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'READ_ONLY', v.id, true, NOW(), NOW()
FROM view_permission v;

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'WAREHOUSE_ASSISTANT', v.id, true, NOW(), NOW()
FROM view_permission v WHERE v.code IN ('DASHBOARD', 'RECEIPTS');

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'OPERATIONS', v.id, true, NOW(), NOW()
FROM view_permission v WHERE v.code IN ('DASHBOARD', 'FLIGHTS', 'MAWBS', 'LOAD_PLANNING', 'ULDS');

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'TRAFFIC', v.id, true, NOW(), NOW()
FROM view_permission v WHERE v.code IN ('DASHBOARD', 'BOOKINGS', 'MAWBS', 'LOAD_PLANNING', 'ULDS');

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'LOAD_PLANNER', v.id, true, NOW(), NOW()
FROM view_permission v WHERE v.code IN ('DASHBOARD', 'FLIGHTS', 'LOAD_PLANNING', 'ULDS');

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'ADMIN', v.id, true, NOW(), NOW()
FROM view_permission v WHERE v.code <> 'SETTINGS';

INSERT INTO role_permission (id, role, view_permission_id, can_access, created_at, updated_at)
SELECT gen_random_uuid(), 'SUPER_USER', v.id, true, NOW(), NOW()
FROM view_permission v;
