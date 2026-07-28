CREATE TABLE IF NOT EXISTS dua_record (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    mawb_id UUID NOT NULL REFERENCES mawb(id) ON DELETE CASCADE,
    dua_number VARCHAR(50) NOT NULL,
    document_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    dua_date DATE,
    notes TEXT,
    customs_broker VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dua_record_mawb ON dua_record(mawb_id);
CREATE INDEX IF NOT EXISTS idx_dua_record_status ON dua_record(status);
