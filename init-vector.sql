-- PostgreSQL 초기화 스크립트
-- pgvector 확장 설치 및 기본 설정

-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 확장 설치 확인
\echo 'pgvector extension installed successfully'

-- 기본 spot_vectors 테이블 생성 (선택사항)
CREATE TABLE IF NOT EXISTS spot_vectors (
                                            spot_id BIGINT PRIMARY KEY,
                                            location_embedding vector(2),
                                            tag_embedding vector(1536),
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS spot_vectors_tag_embedding_idx
    ON spot_vectors USING ivfflat (tag_embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS spot_vectors_location_embedding_idx
    ON spot_vectors USING ivfflat (location_embedding vector_cosine_ops);

-- 권한 설정
GRANT ALL PRIVILEGES ON TABLE spot_vectors TO yoon;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO yoon;

\echo 'Database initialization completed'