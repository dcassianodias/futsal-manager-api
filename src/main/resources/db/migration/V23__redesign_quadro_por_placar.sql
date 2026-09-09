-- V22 tratou "quadro" como se fosse necessário agendar um jogo por quadro (modelo errado, corrigido em seguida
-- na mesma sessão de trabalho). O confronto é sempre um único jogo — a diferenciação de quadro entra apenas
-- no placar e nos artilheiros ao finalizar. Esta migration desfaz o esquema do V22 e aplica o correto.

ALTER TABLE jogo DROP COLUMN IF EXISTS quadro;

DROP INDEX IF EXISTS uk_jogo_time_datahora_quadro;
DROP INDEX IF EXISTS uk_jogo_adversario_datahora_quadro;

CREATE UNIQUE INDEX IF NOT EXISTS uk_jogo_ativo
    ON jogo (time_id, adversario, local, data_hora)
    WHERE status <> 'CANCELADO';

CREATE UNIQUE INDEX IF NOT EXISTS uk_jogo_time_datahora
    ON jogo (time_id, data_hora)
    WHERE status <> 'CANCELADO';

CREATE UNIQUE INDEX IF NOT EXISTS uk_jogo_adversario_datahora
    ON jogo (adversario, data_hora)
    WHERE status <> 'CANCELADO';

ALTER TABLE jogo
    ADD COLUMN IF NOT EXISTS gols_time_quadro1       INTEGER,
    ADD COLUMN IF NOT EXISTS gols_adversario_quadro1 INTEGER,
    ADD COLUMN IF NOT EXISTS gols_time_quadro2       INTEGER,
    ADD COLUMN IF NOT EXISTS gols_adversario_quadro2 INTEGER;

ALTER TABLE gol_registro
    ADD COLUMN IF NOT EXISTS quadro quadro_time NOT NULL DEFAULT 'PRIMEIRO';

ALTER TABLE gol_registro
    ALTER COLUMN quadro DROP DEFAULT;
