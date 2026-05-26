-- 기존 constraint 제거 (있으면)
ALTER TABLE p_hub DROP CONSTRAINT IF EXISTS uk_latitude_longitude;

-- Partial unique index 생성
CREATE UNIQUE INDEX IF NOT EXISTS uk_latitude_longitude_active
    ON p_hub (latitude, longitude)
    WHERE deleted_at IS NULL;