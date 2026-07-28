-- V1__init.sql
-- Initial schema for warehouse-service (warehouse_receipt and receipt_piece tables)

CREATE TYPE receipt_piece_status AS ENUM ('PENDING', 'SCANNED', 'VALIDATED');

CREATE TABLE warehouse_receipt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    airline_id UUID NOT NULL,
    mawb_id UUID,
    created_by_user_id UUID,
    gateway_cfs VARCHAR(50),
    shipper_name VARCHAR(200),
    consignee_name VARCHAR(200),
    agent_name VARCHAR(200),
    origin BPCHAR(3),
    destination BPCHAR(3),
    awb_reported_pieces INTEGER,
    mawb_weight_greatest NUMERIC(10,3),
    shipper_reported_weight NUMERIC(10,3),
    start_datetime TIMESTAMPTZ,
    receipt_date TIMESTAMPTZ,
    cash_only BOOLEAN DEFAULT FALSE,
    booked_in_acoms BOOLEAN DEFAULT FALSE,
    docs_provided BOOLEAN DEFAULT FALSE,
    customs_completed BOOLEAN DEFAULT FALSE,
    pre_built BOOLEAN DEFAULT FALSE,
    loose_tender BOOLEAN DEFAULT FALSE,
    piece_count INTEGER,
    dim_factor_dom INTEGER DEFAULT 194,
    dim_factor_intl INTEGER DEFAULT 366,
    actual_weight_lbs NUMERIC(10,2),
    actual_weight_kg NUMERIC(10,3),
    chargeable_weight_lbs NUMERIC(10,2),
    chargeable_weight_kg NUMERIC(10,3),
    shipper_comment TEXT,
    observations TEXT,
    remarks TEXT,
    created_by_name VARCHAR(150),
    delivered_by_name VARCHAR(150),
    delivered_by_id_num VARCHAR(50),
    delivered_by_id_doc_url TEXT,
    delivered_by_sig_url TEXT,
    received_by_name VARCHAR(150),
    received_by_id_num VARCHAR(50),
    received_by_id_doc_url TEXT,
    received_by_sig_url TEXT,
    broker_name VARCHAR(150),
    broker_id_num VARCHAR(50),
    broker_id_doc_url TEXT,
    broker_sig_url TEXT,
    receipt_doc_url TEXT,
    dock_signature TEXT,
    supporting_docs TEXT DEFAULT '[]',
    hawb_id UUID,
    print_name VARCHAR(150),
    excel_data BYTEA,
    pdf_data BYTEA,
    correction_of_id UUID,
    correction_number INTEGER DEFAULT 1,
    superseded BOOLEAN DEFAULT FALSE,
    correction_reason TEXT,
    corrected_by_name VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE receipt_piece (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID NOT NULL REFERENCES warehouse_receipt(id) ON DELETE CASCADE,
    hawb_id UUID,
    piece_number INTEGER,
    pieces INTEGER,
    length_in NUMERIC(8,2),
    width_in NUMERIC(8,2),
    height_in NUMERIC(8,2),
    scale_weight_lbs NUMERIC(10,2),
    scale_weight_kg NUMERIC(10,3),
    dim_weight_lbs NUMERIC(10,2),
    dim_weight_kg NUMERIC(10,3),
    chargeable_lbs NUMERIC(10,2),
    chargeable_kg NUMERIC(10,3),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_warehouse_receipt_mawb_id ON warehouse_receipt(mawb_id);
CREATE INDEX idx_warehouse_receipt_airline_id ON warehouse_receipt(airline_id);
CREATE INDEX idx_warehouse_receipt_superseded ON warehouse_receipt(superseded);
CREATE INDEX idx_warehouse_receipt_correction_of ON warehouse_receipt(correction_of_id);
CREATE INDEX idx_receipt_piece_receipt_id ON receipt_piece(receipt_id);