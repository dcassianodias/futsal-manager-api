CREATE TABLE usuario_time (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id uuid NOT NULL,
    time_id uuid NOT NULL,
    perfil perfil_usuario NOT NULL,
    ativo boolean NOT NULL DEFAULT true,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_usuario_time_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_time_time    FOREIGN KEY (time_id)    REFERENCES time(id)    ON DELETE CASCADE,
    CONSTRAINT uk_usuario_time_usuario_time UNIQUE (usuario_id, time_id)
);

CREATE INDEX idx_usuario_time_usuario_id ON usuario_time(usuario_id);
CREATE INDEX idx_usuario_time_time_id    ON usuario_time(time_id);

-- Backfill: um vínculo de membership por usuário existente, preservando time/perfil/ativo atuais.
INSERT INTO usuario_time (usuario_id, time_id, perfil, ativo, data_criacao)
SELECT id, time_id, perfil, ativo, data_criacao FROM usuario;

-- As colunas antigas usuario.time_id/usuario.perfil permanecem na tabela (não mapeadas pelo
-- Hibernate a partir desta versão) até uma migration de limpeza futura, depois que todo o
-- código estiver lendo/gravando exclusivamente em usuario_time. Precisam deixar de ser
-- NOT NULL agora: a entidade Usuario não escreve mais nelas, então qualquer INSERT novo
-- violaria a constraint original.
ALTER TABLE usuario ALTER COLUMN time_id DROP NOT NULL;
ALTER TABLE usuario ALTER COLUMN perfil DROP NOT NULL;
