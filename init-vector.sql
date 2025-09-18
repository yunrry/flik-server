-- init-vector.sql
-- PostgreSQL 벡터 데이터베이스 초기화 스크립트

-- pgvector extension 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- Spot embeddings 테이블 생성
CREATE TABLE IF NOT EXISTS spot_embeddings (
                                               id BIGSERIAL PRIMARY KEY,
                                               spot_id BIGINT NOT NULL UNIQUE,
                                               location_embedding vector(2),  -- [normalized_lat, normalized_lng]
                                               tag_embedding vector(1536),    -- OpenAI embedding dimension
                                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                               updated_at TIMESTAMP WITH TIME ZONE
);

-- Tags 테이블 (MySQL에서 이미 정의되어 있지만 참고용)
-- CREATE TABLE IF NOT EXISTS tags (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(100) NOT NULL UNIQUE,
--     embedding vector(1536),
--     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
-- );

-- 인덱스 생성 (벡터 유사도 검색 최적화)
CREATE INDEX IF NOT EXISTS idx_spot_embeddings_spot_id
    ON spot_embeddings (spot_id);

CREATE INDEX IF NOT EXISTS idx_spot_embeddings_location_embedding
    ON spot_embeddings USING ivfflat (location_embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_spot_embeddings_tag_embedding
    ON spot_embeddings USING ivfflat (tag_embedding vector_cosine_ops)
    WITH (lists = 100);

-- 복합 인덱스 (위치 + 태그 동시 검색)
CREATE INDEX IF NOT EXISTS idx_spot_embeddings_combined
    ON spot_embeddings (spot_id)
    INCLUDE (location_embedding, tag_embedding);

-- 벡터 검색을 위한 함수들
CREATE OR REPLACE FUNCTION calculate_weighted_similarity(
    location_emb1 vector(2),
    location_emb2 vector(2),
    tag_emb1 vector(1536),
    tag_emb2 vector(1536),
    location_weight FLOAT DEFAULT 0.3,
    tag_weight FLOAT DEFAULT 0.7
) RETURNS FLOAT AS $$
BEGIN
    RETURN (
        location_weight * (1 - (location_emb1 <=> location_emb2)) +
        tag_weight * (1 - (tag_emb1 <=> tag_emb2))
        );
END;
$$ LANGUAGE plpgsql;

-- 사용자 선호도 기반 장소 추천 함수
CREATE OR REPLACE FUNCTION recommend_spots_for_user(
    user_saved_spot_ids BIGINT[],
    preference_location_vector vector(2),
    preference_tag_vector vector(1536),
    location_weight FLOAT DEFAULT 0.3,
    tag_weight FLOAT DEFAULT 0.7,
    result_limit INTEGER DEFAULT 10
) RETURNS TABLE (
        spot_id BIGINT,
        similarity_score FLOAT,
        location_distance FLOAT,
        tag_distance FLOAT
          ) AS $$
BEGIN
    RETURN QUERY
    SELECT
        se.spot_id,
        calculate_weighted_similarity(
                se.location_embedding,
                preference_location_vector,
                se.tag_embedding,
                preference_tag_vector,
                location_weight,
                tag_weight
        ) as similarity_score,
        (se.location_embedding <=> preference_location_vector) as location_distance,
        (se.tag_embedding <=> preference_tag_vector) as tag_distance
    FROM spot_embeddings se
    WHERE se.spot_id = ANY(user_saved_spot_ids)
    ORDER BY similarity_score DESC
    LIMIT result_limit;
END;
$$ LANGUAGE plpgsql;

-- 지역별 유사한 장소 찾기 함수
CREATE OR REPLACE FUNCTION find_similar_spots_in_region(
    target_location_vector vector(2),
    target_tag_vector vector(1536),
    max_location_distance FLOAT DEFAULT 0.1,
    location_weight FLOAT DEFAULT 0.3,
    tag_weight FLOAT DEFAULT 0.7,
    result_limit INTEGER DEFAULT 20
) RETURNS TABLE (
    spot_id BIGINT,
    similarity_score FLOAT,
    location_distance FLOAT,
    tag_distance FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        se.spot_id,
        calculate_weighted_similarity(
                se.location_embedding,
                target_location_vector,
                se.tag_embedding,
                target_tag_vector,
                location_weight,
                tag_weight
        ) as similarity_score,
        (se.location_embedding <=> target_location_vector) as location_distance,
        (se.tag_embedding <=> target_tag_vector) as tag_distance
    FROM spot_embeddings se
    WHERE (se.location_embedding <=> target_location_vector) <= max_location_distance
    ORDER BY similarity_score DESC
    LIMIT result_limit;
END;
$$ LANGUAGE plpgsql;

-- 벡터 통계 정보 확인 뷰
CREATE OR REPLACE VIEW vector_stats AS
SELECT
    COUNT(*) as total_embeddings,
    COUNT(location_embedding) as location_embeddings_count,
    COUNT(tag_embedding) as tag_embeddings_count,
    AVG(vector_dims(location_embedding)) as avg_location_dims,
    AVG(vector_dims(tag_embedding)) as avg_tag_dims
FROM spot_embeddings;

-- 예시 데이터 (테스트용)
-- INSERT INTO spot_embeddings (spot_id, location_embedding, tag_embedding) VALUES
-- (1, '[0.1, 0.2]', '[0.1, 0.2, 0.3, ...]'),  -- 실제로는 1536차원
-- (2, '[0.3, 0.4]', '[0.4, 0.5, 0.6, ...]');

-- 권한 설정
GRANT ALL PRIVILEGES ON TABLE spot_embeddings TO yoon;
GRANT USAGE, SELECT ON SEQUENCE spot_embeddings_id_seq TO yoon;