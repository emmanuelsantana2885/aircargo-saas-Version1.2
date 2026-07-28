-- V1__init.sql
-- Initial schema for booking-service

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'commodity_type') THEN
        CREATE TYPE commodity_type AS ENUM (
            'DRY_CARGO', 'ELECTRONICS', 'PERISHABLE', 'HIGH_VALUES', 'CIGARETTES',
            'SMALL_PACKAGES', 'WWEF', 'LIVE_PLANTS', 'GENERAL', 'COMAT', 'FCC',
            'EMPTY_ULD', 'EMPTY_PALLET', 'RED_TAG', 'EMPTY_BAGS', 'NETS',
            'SDQ_SDF', 'SDQ_MIA'
        );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS booking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    airline_id UUID NOT NULL,
    flight_id UUID NOT NULL,
    mawb_id UUID,
    client_name VARCHAR(150),
    contact_name VARCHAR(150),
    cnee VARCHAR(150),
    shipper_name VARCHAR(150),
    awb_number VARCHAR(50),
    skids INTEGER,
    units INTEGER,
    reserved_kg NUMERIC(10,3) NOT NULL DEFAULT 0,
    confirmed_kg NUMERIC(10,3),
    received_kg NUMERIC(10,3),
    fulfillment_pct NUMERIC(10,4),
    destination VARCHAR(100),
    priority VARCHAR(50),
    commodity_type commodity_type,
    day_received DATE,
    time_hours VARCHAR(20),
    positions INTEGER,
    real_positions INTEGER,
    last_week_kg NUMERIC(10,3),
    last_week_positions INTEGER,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_booking_airline_id ON booking(airline_id);
CREATE INDEX IF NOT EXISTS idx_booking_flight_id ON booking(flight_id);
CREATE INDEX IF NOT EXISTS idx_booking_mawb_id ON booking(mawb_id);
CREATE INDEX IF NOT EXISTS idx_booking_is_confirmed ON booking(is_confirmed);