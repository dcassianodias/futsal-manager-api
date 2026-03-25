DROP INDEX IF EXISTS uk_jogo_time_adversario_local_datahora;

CREATE UNIQUE INDEX uk_jogo_ativo
    ON jogo (time_id, adversario, local, data_hora)
    WHERE status <> 'CANCELADO';