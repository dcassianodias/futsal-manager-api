-- usuario.time_id e usuario.perfil ficaram órfãs desde a V13 (migração pro modelo
-- multi-time via usuario_time): a entidade Usuario não mapeia mais esses campos e
-- nenhuma query no código lê ou escreve neles. Removendo pra não confundir quem olhar
-- o banco direto achando que é a fonte de verdade do perfil (que é usuario_time.perfil).
ALTER TABLE usuario DROP COLUMN time_id;
ALTER TABLE usuario DROP COLUMN perfil;
