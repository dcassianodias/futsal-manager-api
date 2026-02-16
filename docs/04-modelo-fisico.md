# 📘 MD04 – Modelo Físico do Banco de Dados
## 🎯 Objetivo

Definir a estrutura física do banco de dados PostgreSQL alinhada ao Modelo de Domínio (MD03).

- Tabelas

- Tipos de dados

- Chaves primárias

- Chaves estrangeiras

- Índices

- Restrições

# 🗄 Banco de Dados: PostgreSQL

Padrões adotados:

- Nome das tabelas em snake_case

- Campos em snake_case

- UUID como chave primária

- Auditoria com data_criacao e data_atualizacao

- Multi-tenant via time_id

- Integridade referencial via FOREIGN KEY  

# 📌 Tabela: time
Descrição: Armazena os dados do time.

| Coluna            | Tipo          | Obrigatório | Observação           |
| ----------------- | ------------- | ----------- | -------------------- |
| id                | UUID          | Sim         | Chave primária       |
| nome              | VARCHAR(100)  | Sim         | Nome do time         |
| valor_mensalidade | NUMERIC(10,2) | Sim         | Valor mensal fixo    |
| ativo             | BOOLEAN       | Sim         | Indica se está ativo |
| data_criacao      | TIMESTAMP     | Sim         | Auditoria            |
| data_atualizacao  | TIMESTAMP     | Não         | Auditoria            |

Índice recomendado:

- idx_time_nome (nome)

# 📌 Tabela: usuario
Descrição: Representa atleta ou administrador.

| Coluna           | Tipo           | Obrigatório | Observação          |
| ---------------- | -------------- | ----------- | ------------------- |
| id               | UUID           | Sim         | Chave primária      |
| time_id          | UUID           | Sim         | FK para time        |
| nome             | VARCHAR(150)   | Sim         | Nome completo       |
| email            | VARCHAR(150)   | Sim         | Deve ser único      |
| senha            | VARCHAR(255)   | Sim         | Senha criptografada |
| perfil           | perfil_usuario | Sim         | ADMIN ou ATLETA    |
| ativo            | BOOLEAN        | Sim         | Status              |
| data_criacao     | TIMESTAMP      | Sim         | Auditoria           |
| data_atualizacao | TIMESTAMP      | Não         | Auditoria           |

Relacionamentos:

- FK: usuario.time_id → time.id (ON DELETE CASCADE)

Restrições:

- UNIQUE (time_id, email)

Índice recomendado:

- idx_usuario_time_id (time_id)

# Tabela Jogo
Descrição: Registra os jogos organizados pelo time.

| Coluna           | Tipo         | Obrigatório | Observação              |
| ---------------- | ------------ | ----------- | ----------------------- |
| id               | UUID         | Sim         | Chave primária          |
| time_id          | UUID         | Sim         | FK para time            |
| adversario       | VARCHAR(150) | Sim         | Nome do adversário      |
| local            | VARCHAR(150) | Sim         | Local da partida        |
| data_hora        | TIMESTAMP    | Sim         | Data e hora             |
| status           | status_jogo  | Sim         | AGENDADO, FINALIZADO... |
| observacoes      | TEXT         | Não         | Campo opcional          |
| data_criacao     | TIMESTAMP    | Sim         | Auditoria               |
| data_atualizacao | TIMESTAMP    | Não         | Auditoria               |

Relacionamento:

- FK: jogo.time_id → time.id (ON DELETE CASCADE)

Índice recomendado:

- idx_jogo_time_id (time_id)

# 🔷 Tipo Enumerado: perfil_usuario

Valores:

- ADMIN

- ATLETA

# 🔷 Tipo Enumerado: tipo_pagamento

Valores possíveis:

- MENSALIDADE

- EVENTO

# 🔷 Tipo Enumerado: status_pagamento

Valores:

- PAGO

- PENDENTE

# 🔷 Tipo Enumerado: status_jogo

Valores:

- AGENDADO

- FINALIZADO

- CANCELADO

# Tabela: pagamento
Descrição: Registra pagamentos realizados pelos atletas.

| Coluna           | Tipo             | Obrigatório | Observação                   |
| ---------------- | ---------------- | ----------- | ---------------------------- |
| id               | UUID             | Sim         | Chave primária               |
| time_id          | UUID             | Sim         | FK para time                 |
| usuario_id       | UUID             | Sim         | FK para usuario              |
| evento_id        | UUID             | Não(Condicional)         | FK opcional para evento        |
| mes_referencia   | DATE             | Não(Condicional)         | Competência (ex: 2026-02-01) |
| valor            | NUMERIC(10,2)    | Sim         | Valor da cobrança            |
| tipo             | tipo_pagamento   | Sim         | MENSALIDADE ou EVENTO        |
| status           | status_pagamento | Sim         | PAGO ou PENDENTE             |
| data_criacao     | TIMESTAMP        | Sim         | Auditoria                    |
| data_atualizacao | TIMESTAMP        | Não         | Auditoria                    |

Relacionamentos:

- FK: pagamento.time_id → time.id (ON DELETE CASCADE)

- FK: pagamento.usuario_id → usuario.id (ON DELETE CASCADE)

- FK: pagamento.evento_id → evento.id (ON DELETE SET NULL)

Restrições adicionais:

- Para MENSALIDADE:

  - UNIQUE PARCIAL via índice (WHERE tipo = 'MENSALIDADE')

- Para EVENTO:

  - UNIQUE PARCIAL via índice (WHERE tipo = 'EVENTO')
  
Índices recomendados:

- idx_pagamento_time_id (time_id)

- idx_pagamento_usuario_id (usuario_id)

- idx_pagamento_evento_id (evento_id)

- idx_pagamento_mes_referencia (mes_referencia)

Regra:

mes_referencia obrigatório apenas se tipo = MENSALIDADE

evento_id obrigatório apenas se tipo = EVENTO


# Tabela: despesa
Descrição: Armazena despesas do time.

| Coluna           | Tipo          | Obrigatório | Observação     |
| ---------------- | ------------- | ----------- | -------------- |
| id               | UUID          | Sim         | Chave primária |
| time_id          | UUID          | Sim         | FK para time   |
| descricao        | VARCHAR(255)  | Sim         | Descrição      |
| valor            | NUMERIC(10,2) | Sim         | Valor          |
| mes_referencia   | DATE          | Sim         | Competência    |
| data_criacao     | TIMESTAMP     | Sim         | Auditoria      |
| data_atualizacao | TIMESTAMP     | Não         | Auditoria      |
| tipo             | TIPO_DESPESA  | SIM         | ALUGUEL_QUADRA, UNIFORME, EVENTO, OUTROS      |


Relacionamento:

- FK: despesa.time_id → time.id (ON DELETE CASCADE)

Índice recomendado:

- idx_despesa_time_id (time_id)

# Tabela: evento
Descrição: Armazena despesas do time.

| Coluna           | Tipo          | Obrigatório | Observação     |
| ---------------- | ------------- | ----------- | -------------- |
| id               | UUID PK       | Sim         | PK para evento |
| time_id          | UUID FK       | Sim         | FK para time   |
| nome             | VARCHAR(255)  | Sim         | FK para time   |
| descricao        | VARCHAR(255)  | Sim         | Descrição      |
| valor_sugerido   | NUMERIC(10,2) | Não         | Valor          |
| data_inicio      | DATE          | Sim         | Auditoria      |
| data_fim         | DATE          | Sim         | Auditoria      |
| ativo            | BOOLEAN       | Sim         | Auditoria      |
| data_criacao     | TIMESTAMP     | Sim         | Auditoria      |
| data_atualizacao | TIMESTAMP     | Não         | Auditoria      |

Relacionamentos:

- FK: evento.time_id → time.id (ON DELETE CASCADE)

# Diagrama de Relacionamentos

- Um Time possui vários Usuarios

- Um Time possui vários Jogos

- Um Time possui várias Despesas

- Um Usuário possui vários Pagamentos

- Um Evento pode possuir vários Pagamentos

# 📊 Diagrama Entidade-Relacionamento (ER)

erDiagram

    TIME {
        UUID id PK
        VARCHAR nome
        NUMERIC valor_mensalidade
        BOOLEAN ativo
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }

    USUARIO {
        UUID id PK
        UUID time_id FK
        VARCHAR nome
        VARCHAR email
        VARCHAR senha
        ENUM perfil_usuario
        BOOLEAN ativo
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }

    JOGO {
        UUID id PK
        UUID time_id FK
        VARCHAR adversario
        VARCHAR local
        TIMESTAMP data_hora
        ENUM status_jogo
        TEXT observacoes
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }

    PAGAMENTO {
        UUID id PK
        UUID time_id FK
        UUID usuario_id FK
        UUID evento_id FK
        DATE mes_referencia
        NUMERIC valor
        ENUM tipo_pagamento
        ENUM status_pagamento
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }

    DESPESA {
        UUID id PK
        UUID time_id FK
        VARCHAR descricao
        NUMERIC valor
        DATE mes_referencia
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }

    EVENTO {
    UUID id PK
    UUID time_id FK
    VARCHAR nome
    VARCHAR descricao
    NUMERIC valor_sugerido
    DATE data_inicio
    DATE data_fim
    BOOLEAN ativo
    TIMESTAMP data_criacao
    TIMESTAMP data_atualizacao
    }


    TIME ||--o{ USUARIO : possui
    TIME ||--o{ JOGO : organiza
    TIME ||--o{ PAGAMENTO : registra
    TIME ||--o{ DESPESA : possui
    TIME ||--o{ EVENTO : possui

    USUARIO ||--o{ PAGAMENTO : realiza
    EVENTO ||--o{ PAGAMENTO : referencia



