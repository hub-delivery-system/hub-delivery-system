-- 기존 constraint 제거 (있으면)
ALTER TABLE p_hub DROP CONSTRAINT IF EXISTS uk_latitude_longitude;

-- Partial unique index 생성
CREATE UNIQUE INDEX IF NOT EXISTS uk_latitude_longitude_active
    ON p_hub (latitude, longitude)
    WHERE deleted_at IS NULL;


-- 1. 기존 데이터 삭제 (재실행할 경우)
DELETE FROM p_central_hub
WHERE hub_id IN (
    SELECT id FROM p_hub
    WHERE hub_name IN ('경기남부 중앙허브', '대전 중앙허브', '대구 중앙허브')
);

DELETE FROM p_hub
WHERE hub_name IN ('경기남부 중앙허브', '대전 중앙허브', '대구 중앙허브');

-- 2. 허브 생성 (경기남부, 대전, 대구)
INSERT INTO p_hub (id, hub_name, address, latitude, longitude, created_at, updated_at, created_by, updated_by, deleted_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001'::uuid,
     '경기남부 중앙허브', '경기도 수원시 영통구', 37.2707000, 127.0106000, NOW(), NOW(), 'SYSTEM', 'SYSTEM', NULL),
    ('550e8400-e29b-41d4-a716-446655440002'::uuid,
     '대전 중앙허브', '대전광역시 서구 둔산동', 36.3504000, 127.3845000, NOW(), NOW(), 'SYSTEM', 'SYSTEM', NULL),
    ('550e8400-e29b-41d4-a716-446655440003'::uuid,
     '대구 중앙허브', '대구광역시 수성구 범어동', 35.8748000, 128.6046000, NOW(), NOW(), 'SYSTEM', 'SYSTEM', NULL);

-- 3. 중앙허브 생성 (위에서 생성한 Hub와 연결)
INSERT INTO p_central_hub (id, hub_id, created_at, updated_at, created_by, updated_by, deleted_at)
SELECT
    gen_random_uuid(),
    id,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    NULL
FROM p_hub
WHERE hub_name IN ('경기남부 중앙허브', '대전 중앙허브', '대구 중앙허브');

-- 4. 확인
SELECT id, hub_name, address, latitude, longitude FROM p_hub WHERE hub_name LIKE '%중앙허브%';
SELECT id, hub_id FROM p_central_hub;