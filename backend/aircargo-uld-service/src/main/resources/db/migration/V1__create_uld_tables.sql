CREATE TABLE IF NOT EXISTS uld (
    id UUID PRIMARY KEY,
    airline_id UUID NOT NULL,
    flight_id UUID,
    uld_number VARCHAR(30) NOT NULL,
    uld_type VARCHAR(10) NOT NULL,
    position VARCHAR(10),
    config VARCHAR(10),
    seal_number VARCHAR(50),
    tare_lbs NUMERIC(8,2) NOT NULL DEFAULT 0,
    tare_notes VARCHAR(200),
    gross_weight_lbs NUMERIC(10,2) NOT NULL DEFAULT 0,
    net_weight_lbs NUMERIC(10,2),
    tare_kg NUMERIC(10,2),
    gross_weight_kg NUMERIC(10,2),
    net_weight_kg NUMERIC(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    built_at TIMESTAMP WITH TIME ZONE,
    loaded_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS uld_awb (
    id UUID PRIMARY KEY,
    uld_id UUID NOT NULL,
    mawb_id UUID,
    mawb_label VARCHAR(50),
    description VARCHAR(30) NOT NULL DEFAULT 'DRY_CARGO',
    destination VARCHAR(3),
    pieces INTEGER DEFAULT 0,
    pieces_pct INTEGER DEFAULT 100,
    temp_inbound NUMERIC(6,2),
    temp_outbound NUMERIC(6,2),
    hc BOOLEAN DEFAULT FALSE,
    comments TEXT,
    consumption_pallets NUMERIC(6,3),
    start_time TIME,
    end_time TIME,
    avg_time_per_piece_sec INTEGER DEFAULT 5,
    lapse_minutes NUMERIC(10,2),
    pcs_per_min NUMERIC(10,2),
    operative_worked_hours NUMERIC(10,2),
    earned_hours NUMERIC(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS uld_piece (
    id UUID PRIMARY KEY,
    uld_id UUID NOT NULL,
    mawb_id UUID,
    awb_number VARCHAR(20),
    hawb_number VARCHAR(30),
    piece_number INTEGER NOT NULL,
    source VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    scanned_by UUID,
    scanned_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS uld_type_config (
    id UUID PRIMARY KEY,
    airline_id UUID NOT NULL,
    uld_type VARCHAR(10) NOT NULL,
    default_tare_lbs NUMERIC(8,2) NOT NULL,
    max_gross_lbs NUMERIC(10,2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_uld_airline_id ON uld(airline_id);
CREATE INDEX IF NOT EXISTS idx_uld_flight_id ON uld(flight_id);
CREATE INDEX IF NOT EXISTS idx_uld_number ON uld(uld_number);
CREATE INDEX IF NOT EXISTS idx_uld_awb_uld_id ON uld_awb(uld_id);
CREATE INDEX IF NOT EXISTS idx_uld_awb_mawb_id ON uld_awb(mawb_id);
CREATE INDEX IF NOT EXISTS idx_uld_awb_uld_mawb ON uld_awb(uld_id, mawb_id);
CREATE INDEX IF NOT EXISTS idx_uld_piece_uld_id ON uld_piece(uld_id);
CREATE INDEX IF NOT EXISTS idx_uld_piece_mawb_id ON uld_piece(mawb_id);
CREATE INDEX IF NOT EXISTS idx_uld_piece_awb ON uld_piece(awb_number);
CREATE INDEX IF NOT EXISTS idx_uld_piece_uld_mawb ON uld_piece(uld_id, mawb_id);
CREATE INDEX IF NOT EXISTS idx_uld_type_config_airline ON uld_type_config(airline_id);
