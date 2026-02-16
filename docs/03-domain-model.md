# Futsal Manager – Modelo de Domínio

## 1. Visão Geral do Domínio

O sistema será estruturado em torno do conceito principal de Time.

Todas as entidades operacionais estarão vinculadas a um Time através do campo:

time_id

Essa decisão permite isolamento de dados e futura evolução para modelo SaaS multi-tenant.

---

## 2. Entidades Principais

### 2.1 Time

Representa um time de futsal.

Campos:

- id
- nome
- data_criacao
- ativo

Regras:
- Um Time possui vários Usuários
- Um Time possui vários Jogos
- Um Time possui vários Pagamentos
- Um Time possui várias Despesas

---

### 2.2 Usuario

Representa atleta ou administrador.

Campos:

- id
- time_id
- nome
- email
- senha
- perfil (ADMIN | ATLETA)
- ativo
- data_criacao

Regras:
- Email deve ser único
- Usuário pertence a apenas um Time
- O perfil define permissões no sistema

---

### 2.3 Jogo

Representa um jogo agendado.

Campos:

- id
- time_id
- adversario
- local
- data_hora
- status (AGENDADO | FINALIZADO | CANCELADO)
- observacoes

Regras:
- Apenas ADMIN pode criar ou alterar jogos
- Um Jogo pertence a um Time
- Controle de presença poderá ser adicionado futuramente

---

### 2.4 Pagamento

Representa contribuição financeira de um atleta.

Campos:

- id
- time_id
- usuario_id
- mes_referencia (YYYY-MM)
- valor
- tipo (MENSALIDADE | EVENTO)
- status (PAGO | PENDENTE)
- data_criacao
- eventoId

Regras:
- Um Pagamento pertence a um Usuário
- ADMIN pode registrar pagamento manualmente
- Não deve existir mais de uma MENSALIDADE por mês por usuário

---

### 2.5 Despesa

Representa despesas do time.

Campos:

- id
- time_id
- descricao
- valor
- mes_referencia
- tipo (ALUGUEL_QUADRA | UNIFORME | EVENTO | OUTROS)
- data_criacao

Regras:
- Apenas ADMIN pode registrar despesas
- Despesas impactam a necessidade de arrecadação do mês

---

### 2.6 Evento

Evento:

- id: UUID
- time_id: UUID
- nome: String
- descricao: String
- valor_sugerido: BigDecimal
- data_inicio: LocalDate
- data_fim: LocalDate
- ativo: Boolean
- createdAt: LocalDateTime
  
---

## 3. Relacionamentos

Time (1) → (N) Usuario  
Time (1) → (N) Jogo  
Time (1) → (N) Pagamento  
Time (1) → (N) Despesa  
Time (1) -> (N) Evento

Evento (1) -> (N) Pagamento (tipo EVENTO)

Usuario (1) → (N) Pagamento  

---

## 4. Regras de Negócio Fundamentais

1. Todo usuário deve pertencer a um Time.
2. Toda consulta deve ser filtrada por time_id.
3. Apenas ADMIN pode:
   - Criar jogos
   - Registrar despesas
   - Gerenciar pagamentos
4. ATLETA pode:
   - Visualizar jogos
   - Visualizar pagamentos
   - Confirmar presença (futuro)

---

## 5. Estratégia Inicial de Identificadores

Para o MVP:

- “Estratégia de Identificadores” → UUID como padrão desde o MVP

---

## 6. Evoluções Futuras do Domínio

Possíveis novas entidades:

- Presenca (controle de presença por jogo)
- Estatistica
- Aviso
- PlanoAssinatura (para SaaS)
- Notificacao

---

## 7. Diretrizes Técnicas Importantes

- Nunca expor Entity diretamente na API.
- Utilizar DTO para entrada e saída.
- Validar regras de negócio na camada Service.
- Sempre validar time_id do usuário autenticado antes de qualquer operação.

---

Versão do Documento: 1.1  
Data: 2026-02-15
