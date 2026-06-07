CREATE TABLE IF NOT EXISTS maintenances (
    id          SERIAL PRIMARY KEY,
    bathroom_id INT NOT NULL REFERENCES bathrooms(id) ON DELETE RESTRICT,
    technician_full_name VARCHAR(100) NOT NULL,
    reported_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    description VARCHAR(255),
    status      VARCHAR(20) DEFAULT 'ABIERTO' CHECK (status IN ('ABIERTO','CERRADO')),
    resolved_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_maintenance_bathroom_id  ON maintenances(bathroom_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_status       ON maintenances(status);
CREATE INDEX IF NOT EXISTS idx_maintenance_scheduled_at ON maintenances(scheduled_at);