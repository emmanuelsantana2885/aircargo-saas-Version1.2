CREATE TABLE IF NOT EXISTS label_template (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    width_inches NUMERIC(5,2) NOT NULL,
    height_inches NUMERIC(5,2) NOT NULL,
    orientation VARCHAR(20) NOT NULL DEFAULT 'HORIZONTAL',
    dpi INTEGER NOT NULL DEFAULT 203,
    config_json TEXT NOT NULL DEFAULT '{"elements":[]}',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_label_template_type ON label_template(type);
