CREATE TABLE IF NOT EXISTS cleaning_schedules (
    id           SERIAL PRIMARY KEY,
    bathroom_id  INT NOT NULL REFERENCES bathrooms(id) ON DELETE CASCADE,
    user_id      INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date   DATE,
    end_date     DATE,
    frequency    VARCHAR(10) NOT NULL CHECK (frequency IN ('DIARIO','SEMANAL')),
    days_of_week VARCHAR(50),
    start_time   TIME,
    end_time     TIME,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cleaning_bathroom_id     ON cleaning_schedules(bathroom_id);
CREATE INDEX IF NOT EXISTS idx_cleaning_user_id     ON cleaning_schedules(user_id);

CREATE INDEX IF NOT EXISTS idx_cleaning_overlap 
ON cleaning_schedules(id, bathroom_id, start_time, end_time);

CREATE INDEX IF NOT EXISTS idx_cleaning_date_range 
ON cleaning_schedules(start_date, end_date);
