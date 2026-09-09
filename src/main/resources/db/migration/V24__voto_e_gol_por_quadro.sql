-- Um jogador pode marcar gol no 1º E no 2º quadro do mesmo jogo — a trava antiga (jogo_id, usuario_id)
-- impedia isso, sobrando só pro cenário de jogador que marca em um único quadro.
ALTER TABLE gol_registro DROP CONSTRAINT uk_gol_registro_jogo_usuario;
ALTER TABLE gol_registro ADD CONSTRAINT uk_gol_registro_jogo_usuario_quadro UNIQUE (jogo_id, usuario_id, quadro);

-- Melhor da rodada agora é votado separadamente por quadro.
ALTER TABLE voto_melhor_rodada ADD COLUMN quadro quadro_time NOT NULL DEFAULT 'PRIMEIRO';
ALTER TABLE voto_melhor_rodada ALTER COLUMN quadro DROP DEFAULT;

ALTER TABLE voto_melhor_rodada DROP CONSTRAINT uk_voto_jogo_votante;
ALTER TABLE voto_melhor_rodada ADD CONSTRAINT uk_voto_jogo_votante_quadro UNIQUE (jogo_id, votante_id, quadro);
