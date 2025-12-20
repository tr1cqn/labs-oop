CREATE TABLE functions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    definition TEXT
);