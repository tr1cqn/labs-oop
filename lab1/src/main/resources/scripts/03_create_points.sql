-- Таблица точек функций
CREATE TABLE points (
    id BIGSERIAL PRIMARY KEY,
    func_id BIGINT NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL
);

