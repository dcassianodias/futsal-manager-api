CREATE TYPE resultado_jogo AS ENUM ('VITORIA', 'DERROTA', 'EMPATE');

ALTER TABLE jogo
    ADD COLUMN gols_time       INTEGER DEFAULT 0,
    ADD COLUMN gols_adversario INTEGER DEFAULT 0,
    ADD COLUMN resultado       resultado_jogo;

ALTER TABLE usuario
    ADD COLUMN gols INTEGER NOT NULL DEFAULT 0;
