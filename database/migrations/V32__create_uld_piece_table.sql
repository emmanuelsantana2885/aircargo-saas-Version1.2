CREATE TYPE piece_source AS ENUM ('BARCODE', 'MANUAL');

CREATE TABLE uld_piece (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uld_id UUID NOT NULL REFERENCES uld(id) ON DELETE CASCADE,
    mawb_id UUID REFERENCES mawb(id),
    awb_number VARCHAR(20),
    hawb_number VARCHAR(30),
    piece_number INTEGER NOT NULL,
    source piece_source NOT NULL DEFAULT 'MANUAL',
    scanned_by UUID REFERENCES app_user(id),
    scanned_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_uld_piece_uld_id ON uld_piece(uld_id);
CREATE INDEX idx_uld_piece_mawb_id ON uld_piece(mawb_id);
CREATE INDEX idx_uld_piece_awb ON uld_piece(awb_number);
CREATE INDEX idx_uld_piece_uld_mawb ON uld_piece(uld_id, mawb_id);
