-- Retroactively mark older receipts as superseded.
-- For each MAWB, only the most recent general receipt (hawbId IS NULL) stays active.
-- For each (MAWB, HAWB) pair, only the most recent receipt stays active.

-- 1. General receipts: supersede all but the latest per MAWB
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY mawb_id ORDER BY created_at DESC, id DESC) AS rn
    FROM warehouse_receipt
    WHERE hawb_id IS NULL
)
UPDATE warehouse_receipt SET superseded = true
FROM ranked
WHERE warehouse_receipt.id = ranked.id
  AND ranked.rn > 1
  AND warehouse_receipt.superseded = false;

-- 2. HAWB receipts: supersede all but the latest per (mawb_id, hawb_id)
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY mawb_id, hawb_id ORDER BY created_at DESC, id DESC) AS rn
    FROM warehouse_receipt
    WHERE hawb_id IS NOT NULL
)
UPDATE warehouse_receipt SET superseded = true
FROM ranked
WHERE warehouse_receipt.id = ranked.id
  AND ranked.rn > 1
  AND warehouse_receipt.superseded = false;

-- 3. Ensure all non-superseded receipts have correction_number >= 1
UPDATE warehouse_receipt
SET correction_number = 1
WHERE superseded = false
  AND (correction_number IS NULL OR correction_number < 1);
