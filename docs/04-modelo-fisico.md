# Tabela Time
Descrição: Armazena os dados do time.

| Coluna           | Tipo         | Obrigatório | Observação           |
| ---------------- | ------------ | ----------- | -------------------- |
| id               | UUID         | Sim         | Chave primária       |
| nome             | VARCHAR(100) | Sim         | Nome do time         |
| data_criacao     | TIMESTAMP    | Sim         | Data de criação      |
| data_atualizacao | TIMESTAMP    | Não         | Data de atualização  |
| ativo            | BOOLEAN      | Sim         | Indica se está ativo |

# Tabela Atleta
Descrição: Armazena os atletas vinculados a um time.

| Coluna           | Tipo         | Obrigatório | Observação        |
| ---------------- | ------------ | ----------- | ----------------- |
| id               | UUID         | Sim         | Chave primária    |
| nome             | VARCHAR(150) | Sim         | Nome completo     |
| telefone         | VARCHAR(20)  | Não         | Contato           |
| posicao          | VARCHAR(50)  | Não         | Posição em quadra |
| data_entrada     | DATE         | Sim         | Data de entrada   |
| ativo            | BOOLEAN      | Sim         | Status            |
| time_id          | UUID         | Sim         | FK para time      |
| data_criacao     | TIMESTAMP    | Sim         | Auditoria         |
| data_atualizacao | TIMESTAMP    | Não         | Auditoria         |

Relacionamento:

- FK: atleta.time_id → time.id

- Regra de exclusão: ON DELETE CASCADE

Índice recomendado:

- idx_atleta_time_id (time_id)

# Tabela Jogo
Descrição: Registra os jogos organizados pelo time.

| Coluna           | Tipo          | Obrigatório | Observação       |
| ---------------- | ------------- | ----------- | ---------------- |
| id               | UUID          | Sim         | Chave primária   |
| data_jogo        | TIMESTAMP     | Sim         | Data e horário   |
| local            | VARCHAR(150)  | Sim         | Local da partida |
| valor_quadra     | NUMERIC(10,2) | Sim         | Valor total      |
| confirmado       | BOOLEAN       | Sim         | Status           |
| time_id          | UUID          | Sim         | FK para time     |
| data_criacao     | TIMESTAMP     | Sim         | Auditoria        |
| data_atualizacao | TIMESTAMP     | Não         | Auditoria        |

Relacionamento:

- FK: jogo.time_id → time.id

- Regra de exclusão: ON DELETE CASCADE

Índice recomendado:

- idx_jogo_time_id (time_id)

# Tipo Enumerado: tipo_pagamento

Valores possíveis:

- MENSALIDADE

- JOGO

# Tabela: pagamento
Descrição: Registra pagamentos realizados pelos atletas.

| Coluna           | Tipo          | Obrigatório | Observação            |
| ---------------- | ------------- | ----------- | --------------------- |
| id               | UUID          | Sim         | Chave primária        |
| atleta_id        | UUID          | Sim         | FK para atleta        |
| jogo_id          | UUID          | Não         | FK opcional para jogo |
| valor            | NUMERIC(10,2) | Sim         | Valor pago            |
| data_pagamento   | TIMESTAMP     | Sim         | Data do pagamento     |
| tipo_pagamento   | ENUM          | Sim         | MENSALIDADE ou JOGO   |
| data_criacao     | TIMESTAMP     | Sim         | Auditoria             |
| data_atualizacao | TIMESTAMP     | Não         | Auditoria             |

Relacionamentos:

- FK: pagamento.atleta_id → atleta.id (ON DELETE CASCADE)

- FK: pagamento.jogo_id → jogo.id (ON DELETE SET NULL)

Índices recomendados:

- idx_pagamento_atleta_id (atleta_id)

- idx_pagamento_jogo_id (jogo_id)

# Tabela: despesa
Descrição: Armazena despesas do time.

| Coluna           | Tipo          | Obrigatório | Observação           |
| ---------------- | ------------- | ----------- | -------------------- |
| id               | UUID          | Sim         | Chave primária       |
| descricao        | VARCHAR(255)  | Sim         | Descrição da despesa |
| valor            | NUMERIC(10,2) | Sim         | Valor                |
| data_despesa     | TIMESTAMP     | Sim         | Data da despesa      |
| time_id          | UUID          | Sim         | FK para time         |
| data_criacao     | TIMESTAMP     | Sim         | Auditoria            |
| data_atualizacao | TIMESTAMP     | Não         | Auditoria            |

Relacionamento:

- FK: despesa.time_id → time.id (ON DELETE CASCADE)

Índice recomendado:

- idx_despesa_time_id (time_id)

# Diagrama de Relacionamentos

- Um Time possui vários Atletas

- Um Time possui vários Jogos

- Um Time possui várias Despesas

- Um Atleta possui vários Pagamentos

- Um Jogo pode possuir vários Pagamentos
