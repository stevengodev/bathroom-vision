CREATE TABLE "incident_messages" (
  "id" SERIAL PRIMARY KEY,
  "code" varchar NOT NULL,
  "description" varchar NOT NULL,
  "category" VARCHAR NOT NULL CHECK (category IN ('LIMPIEZA', 'MANTENIMIENTO')),
  "created_at" TIMESTAMP DEFAULT NOW(),
  "updated_at" TIMESTAMP DEFAULT NOW()
);

INSERT INTO incident_messages (code, description, category) VALUES
    ('FALTA_DE_JABON',   'Falta jabón en el dispensador', 'LIMPIEZA'),
    ('FALTA_DE_PAPEL',   'Falta papel higiénico', 'LIMPIEZA'),
    ('MAL_OLOR',         'Mal olor en el baño', 'LIMPIEZA'),

    ('INODORO_AVERIADO', 'Inodoro averiado o no funciona', 'MANTENIMIENTO'),
    ('PUERTA_ROTA',      'Puerta del cubículo rota o sin seguro', 'MANTENIMIENTO'),
    ('LAVAMANOS_ROTO',   'Lavamanos roto o sin agua', 'MANTENIMIENTO');

CREATE TABLE incidents (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  bathroom_id BIGINT NOT NULL,
  incident_message_id INT NOT NULL,
  reported_at TIMESTAMP NOT NULL,
  status VARCHAR NOT NULL,
  resolved_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),

  CONSTRAINT fk_incident_user
      FOREIGN KEY (user_id) REFERENCES users(id),

  CONSTRAINT fk_incident_bathroom
      FOREIGN KEY (bathroom_id) REFERENCES bathrooms(id),

  CONSTRAINT fk_incident_message
      FOREIGN KEY (incident_message_id) REFERENCES incident_messages(id)
);

CREATE INDEX IF NOT EXISTS idx_incidents_user_id        ON incidents(user_id);
CREATE INDEX IF NOT EXISTS idx_incidents_status         ON incidents(status);