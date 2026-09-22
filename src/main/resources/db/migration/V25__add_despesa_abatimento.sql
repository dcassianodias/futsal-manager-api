ALTER TYPE status_despesa ADD VALUE 'PARCIAL';

ALTER TABLE despesa ADD COLUMN valor_pago numeric(10, 2) NOT NULL DEFAULT 0;

CREATE TABLE despesa_abatimento (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    despesa_id uuid NOT NULL,
    valor numeric(10, 2) NOT NULL,
    data_pagamento date NOT NULL,
    observacao varchar(255),
    registrado_por_id uuid,
    data_criacao timestamp NOT NULL DEFAULT now(),

    CONSTRAINT fk_despesa_abatimento_despesa
        FOREIGN KEY (despesa_id) REFERENCES despesa(id) ON DELETE CASCADE,
    CONSTRAINT fk_despesa_abatimento_usuario
        FOREIGN KEY (registrado_por_id) REFERENCES usuario(id)
);

CREATE INDEX idx_despesa_abatimento_despesa_id ON despesa_abatimento(despesa_id);
