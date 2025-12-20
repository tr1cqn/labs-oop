-- Таблица результатов вычислений
CREATE TABLE result (
    id BIGSERIAL PRIMARY KEY,
    result_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    result TEXT NOT NULL
);

