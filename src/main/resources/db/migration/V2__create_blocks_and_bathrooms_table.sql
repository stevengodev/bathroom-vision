CREATE TABLE IF NOT EXISTS blocks (
    id         BIGSERIAL  PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    number_of_floors      INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bathrooms (
    id         BIGSERIAL  PRIMARY KEY,
    block_id   BIGINT NOT NULL REFERENCES blocks(id) ON DELETE RESTRICT,
    floor      INT NOT NULL,
    gender     VARCHAR(50) NOT NULL,
    status     VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bathrooms_block_id       ON bathrooms(block_id);