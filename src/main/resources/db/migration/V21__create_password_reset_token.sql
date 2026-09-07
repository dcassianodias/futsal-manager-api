-- Token opaco de recuperação de senha, com expiração e uso único (mesma lógica do
-- código de convite de time: token em texto plano na tabela, não JWT, porque
-- precisa ser revogável/consumível de forma simples).
CREATE TABLE password_reset_token (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id uuid NOT NULL,
    token varchar(255) NOT NULL UNIQUE,
    data_expiracao timestamp NOT NULL,
    usado boolean NOT NULL DEFAULT false,
    data_criacao timestamp NOT NULL DEFAULT now(),

    CONSTRAINT fk_password_reset_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
CREATE INDEX idx_password_reset_token_token ON password_reset_token(token);
CREATE INDEX idx_password_reset_token_usuario_id ON password_reset_token(usuario_id);
