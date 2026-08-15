-- V2__seed_extra_airlines.sql
-- Seed additional airlines beyond the seeded reference airline (UPS).
-- Idempotent: ON CONFLICT DO NOTHING.

INSERT INTO airline (id, code, name, iata_code, country, is_active, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000002', 'FDX', 'FedEx Express', 'FX', 'US', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000003', 'DHL', 'DHL Express', 'D0', 'DE', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000004', 'M6', 'Amerijet International', 'M6', 'US', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000005', 'UC', 'LATAM Cargo Chile', 'UC', 'CL', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000006', 'QT', 'Avianca Cargo', 'QT', 'CO', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000007', '5Y', 'Atlas Air', '5Y', 'US', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000008', 'K4', 'Kalitta Air', 'K4', 'US', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000009', 'GG', 'Sky Lease Cargo', 'GG', 'US', true, NOW(), NOW()),
('00000000-0000-0000-0000-000000000010', 'M7', 'MAS Air', 'M7', 'MX', true, NOW(), NOW())
ON CONFLICT DO NOTHING;
