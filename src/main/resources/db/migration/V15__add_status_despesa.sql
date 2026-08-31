CREATE TYPE status_despesa AS ENUM ('PENDENTE', 'PAGO');

ALTER TABLE despesa ADD COLUMN status status_despesa NOT NULL DEFAULT 'PENDENTE';
