CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ENUMS
CREATE TYPE perfil_usuario AS ENUM ('ADMIN', 'ATLETA');
CREATE TYPE tipo_pagamento AS ENUM ('MENSALIDADE', 'EVENTO');
CREATE TYPE status_pagamento AS ENUM ('PAGO', 'PENDENTE');
CREATE TYPE status_jogo AS ENUM ('AGENDADO', 'FINALIZADO', 'CANCELADO');
CREATE TYPE tipo_despesa AS ENUM ('ALUGUEL_QUADRA', 'UNIFORME', 'EVENTO', 'OUTROS');

-- TABELAS
CREATE TABLE time (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome varchar(150) NOT NULL,
    valor_mensalidade numeric(10, 2) NOT NULL,
    ativo boolean NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp
);

CREATE TABLE usuario (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    time_id uuid NOT NULL,
    nome varchar(150) NOT NULL,
    email varchar(150) NOT NULL,
    senha varchar(255) NOT NULL,
    perfil perfil_usuario NOT NULL,
    ativo boolean NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_usuario_time
     FOREIGN KEY (time_id) REFERENCES time(id) ON DELETE CASCADE,
    CONSTRAINT uk_usuario_time_email
     UNIQUE (time_id, email)
);

CREATE TABLE jogo (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    time_id uuid NOT NULL,
    adversario varchar(150) NOT NULL,
    local varchar(100) NOT NULL,
    data_hora timestamp NOT NULL,
    status status_jogo NOT NULL,
    observacoes text,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_jogo_time
      FOREIGN KEY (time_id) REFERENCES time(id) ON DELETE CASCADE
);

CREATE TABLE evento (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    time_id uuid NOT NULL,
    nome varchar(150) NOT NULL,
    descricao text NOT NULL,
    valor_sugerido numeric(10, 2),
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    ativo boolean NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_evento_time
        FOREIGN KEY (time_id) REFERENCES time(id) ON DELETE CASCADE
);

CREATE TABLE despesa (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    time_id uuid NOT NULL,
    descricao varchar(255) NOT NULL,
    valor numeric(10, 2) NOT NULL,
    mes_referencia date NOT NULL,
    tipo tipo_despesa NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_despesa_time
     FOREIGN KEY (time_id) REFERENCES time(id) ON DELETE CASCADE
);

CREATE TABLE pagamento (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    time_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    evento_id uuid,
    mes_referencia date,
    valor numeric(10, 2) NOT NULL,
    tipo tipo_pagamento NOT NULL,
    status status_pagamento NOT NULL,
    data_criacao timestamp NOT NULL DEFAULT now(),
    data_atualizacao timestamp,

    CONSTRAINT fk_pagamento_time
       FOREIGN KEY (time_id) REFERENCES time(id) ON DELETE CASCADE,
    CONSTRAINT fk_pagamento_usuario
       FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_pagamento_evento
       FOREIGN KEY (evento_id) REFERENCES evento(id) ON DELETE SET NULL,

    CONSTRAINT ck_pagamento_mensalidade_ou_evento CHECK (
       (tipo = 'MENSALIDADE' AND mes_referencia IS NOT NULL AND evento_id IS NULL)
           OR
       (tipo = 'EVENTO' AND evento_id IS NOT NULL AND mes_referencia IS NULL)
       )
);

-- ÍNDICES
CREATE INDEX idx_time_nome ON time(nome);

CREATE INDEX idx_usuario_time_id ON usuario(time_id);
CREATE INDEX idx_jogo_time_id ON jogo(time_id);
CREATE INDEX idx_evento_time_id ON evento(time_id);

CREATE INDEX idx_despesa_time_id ON despesa(time_id);
CREATE INDEX idx_despesa_mes_referencia ON despesa(mes_referencia);

CREATE INDEX idx_pagamento_time_id ON pagamento(time_id);
CREATE INDEX idx_pagamento_usuario_id ON pagamento(usuario_id);
CREATE INDEX idx_pagamento_evento_id ON pagamento(evento_id);
CREATE INDEX idx_pagamento_mes_referencia ON pagamento(mes_referencia);

-- UNIQUE parciais
CREATE UNIQUE INDEX uk_pagamento_mensalidade
    ON pagamento(time_id, usuario_id, mes_referencia)
    WHERE tipo = 'MENSALIDADE';

CREATE UNIQUE INDEX uk_pagamento_evento
    ON pagamento(time_id, usuario_id, evento_id)
    WHERE tipo = 'EVENTO';