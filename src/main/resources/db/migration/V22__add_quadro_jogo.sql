CREATE TYPE quadro_time AS ENUM ('PRIMEIRO', 'SEGUNDO');

ALTER TABLE jogo
    ADD COLUMN quadro quadro_time NOT NULL DEFAULT 'PRIMEIRO';

DROP INDEX IF EXISTS uk_jogo_time_datahora;
DROP INDEX IF EXISTS uk_jogo_adversario_datahora;
DROP INDEX IF EXISTS uk_jogo_ativo;

CREATE UNIQUE INDEX uk_jogo_time_datahora_quadro
    ON jogo (time_id, data_hora, quadro)
    WHERE status <> 'CANCELADO';

CREATE UNIQUE INDEX uk_jogo_adversario_datahora_quadro
    ON jogo (adversario, data_hora, quadro)
    WHERE status <> 'CANCELADO';
