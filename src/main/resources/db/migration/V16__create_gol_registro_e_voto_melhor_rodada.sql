CREATE TABLE gol_registro (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    jogo_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    quantidade integer NOT NULL DEFAULT 1,
    data_criacao timestamp NOT NULL DEFAULT now(),

    CONSTRAINT fk_gol_registro_jogo FOREIGN KEY (jogo_id) REFERENCES jogo(id) ON DELETE CASCADE,
    CONSTRAINT fk_gol_registro_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_gol_registro_jogo_usuario UNIQUE (jogo_id, usuario_id)
);
CREATE INDEX idx_gol_registro_jogo_id ON gol_registro(jogo_id);
CREATE INDEX idx_gol_registro_usuario_id ON gol_registro(usuario_id);

CREATE TABLE voto_melhor_rodada (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    jogo_id uuid NOT NULL,
    votante_id uuid NOT NULL,
    votado_id uuid NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),

    CONSTRAINT fk_voto_jogo FOREIGN KEY (jogo_id) REFERENCES jogo(id) ON DELETE CASCADE,
    CONSTRAINT fk_voto_votante FOREIGN KEY (votante_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_voto_votado FOREIGN KEY (votado_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_voto_jogo_votante UNIQUE (jogo_id, votante_id),
    CONSTRAINT ck_voto_nao_self CHECK (votante_id <> votado_id)
);
CREATE INDEX idx_voto_jogo_id ON voto_melhor_rodada(jogo_id);
