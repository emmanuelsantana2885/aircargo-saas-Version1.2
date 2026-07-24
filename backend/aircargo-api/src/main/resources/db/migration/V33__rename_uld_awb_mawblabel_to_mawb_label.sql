-- Safely rename the Java field from mawblabel to mawbLabel.
-- The DB column was already created as mawb_label in V1, so this is a no-op
-- for databases that were initialized with the current schema.
-- Wrapped in DO block to handle both cases gracefully.
DO $$ BEGIN
    ALTER TABLE uld_awb RENAME COLUMN mawblabel TO mawb_label;
EXCEPTION WHEN undefined_column THEN
    -- Column already named mawb_label, nothing to do
END $$;
