CREATE UNIQUE INDEX uk_jogo_time_adversario_local_datahora
    ON jogo(time_id, adversario, local, data_hora);