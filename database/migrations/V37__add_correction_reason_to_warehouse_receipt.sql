-- V37: Add correction reason and corrected-by fields for Adjustment Transaction pattern.
-- Each receipt can now carry a free-text reason explaining WHY a correction was made,
-- and WHO authorized it (populated from JWT principal at correction time).

ALTER TABLE warehouse_receipt ADD COLUMN correction_reason VARCHAR(500);
ALTER TABLE warehouse_receipt ADD COLUMN corrected_by_name VARCHAR(150);

CREATE INDEX idx_receipt_correction ON warehouse_receipt(correction_of_id, superseded);
