ALTER TABLE warehouse_receipt ADD COLUMN correction_of_id uuid REFERENCES warehouse_receipt(id);
ALTER TABLE warehouse_receipt ADD COLUMN correction_number integer DEFAULT 1;
ALTER TABLE warehouse_receipt ADD COLUMN superseded boolean DEFAULT false;

CREATE INDEX idx_warehouse_receipt_correction_of ON warehouse_receipt(correction_of_id);
