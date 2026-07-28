-- V1__init.sql
-- Initial schema for mawb-service (MAWB and HAWB tables)

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'mawb_status') THEN
        CREATE TYPE mawb_status AS ENUM ('BOOKED', 'RECEIVED', 'MANIFESTED', 'DEPARTED', 'ARRIVED', 'CANCELLED');
    END IF;
END $$;
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

CREATE TABLE IF NOT EXISTS mawb (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    airline_id UUID NOT NULL,
    flight_id UUID,
    awb_number VARCHAR(50) NOT NULL UNIQUE,
    shipper_name VARCHAR(200),
    consignee_name VARCHAR(200),
    origin VARCHAR(3),
    destination VARCHAR(3),
    pieces INTEGER,
    reported_weight_kg NUMERIC(10,3),
    chargeable_weight_kg NUMERIC(10,3),
    commodity_type commodity_type,
    status mawb_status NOT NULL DEFAULT 'BOOKED',
    cash_only BOOLEAN DEFAULT FALSE,
    booked_in_acoms BOOLEAN DEFAULT FALSE,
    docs_provided BOOLEAN DEFAULT FALSE,
    customs_completed BOOLEAN DEFAULT FALSE,
    pre_built BOOLEAN DEFAULT FALSE,
    loose_tender BOOLEAN DEFAULT FALSE,
    supporting_docs TEXT DEFAULT '[]',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS hawb (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mawb_id UUID NOT NULL REFERENCES mawb(id),
    airline_id UUID NOT NULL,
    hawb_number VARCHAR(50) NOT NULL UNIQUE,
    consignee_name VARCHAR(200),
    destination VARCHAR(3),
    pieces INTEGER,
    weight_kg NUMERIC(10,3),
    commodity_type commodity_type,
    status mawb_status NOT NULL DEFAULT 'BOOKED',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mawb_airline_id ON mawb(airline_id);
CREATE INDEX IF NOT EXISTS idx_mawb_flight_id ON mawb(flight_id);
CREATE INDEX IF NOT EXISTS idx_mawb_status ON mawb(status);
CREATE INDEX IF NOT EXISTS idx_mawb_awb_number ON mawb(awb_number);

CREATE INDEX IF NOT EXISTS idx_hawb_mawb_id ON hawb(mawb_id);
CREATE INDEX IF NOT EXISTS idx_hawb_airline_id ON hawb(airline_id);
CREATE INDEX IF NOT EXISTS idx_hawb_number ON hawb(hawb_number);