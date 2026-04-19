CREATE UNIQUE INDEX IF NOT EXISTS uk_jogo_time_datahora
    ON jogo (time_id, data_hora)
    WHERE status <> 'CANCELADO';

CREATE UNIQUE INDEX IF NOT EXISTS uk_jogo_adversario_datahora
    ON jogo (adversario, data_hora)
    WHERE status <> 'CANCELADO';