# Requisitos de Backend — Futsal Manager

Documento gerado com base no frontend Angular atual. Lista o que cada funcionalidade precisa do backend para operar corretamente.

---

## 1. Página Inicial (Landing Page)

### O que exibe
- Próximos jogos (todos os times, status AGENDADO, ordenados por data)
- Eventos ativos (todos os times)

### Endpoints necessários

| Endpoint | Status | Observação |
|---|---|---|
| `GET /api/jogo/v1` | Necessário | Retorna todos os jogos sem filtro de time |
| `GET /api/evento/v1` | Necessário | Retorna todos os eventos sem filtro de time |
| `GET /api/time/v1` | Já existe | Usado para mapear `timeId` → nome do time nos jogos |

### Campos esperados nas respostas
**Jogo**: `id`, `timeId`, `adversario`, `local`, `dataHora`, `statusJogo` (AGENDADO/FINALIZADO/CANCELADO), `observacoes?`

**Evento**: `id`, `timeId`, `nome`, `descricao?`, `valorSugerido?`, `dataInicio`, `dataFim`, `ativo`

---

## 2. Autenticação / Seleção de Time

### Funcionamento atual (sem auth real)
O frontend usa `TeamStateService` com `localStorage`. O usuário escolhe um time na tela de login, e esse time fica salvo na sessão.

### O que precisará ser implementado para auth real

| Endpoint | Descrição |
|---|---|
| `POST /api/auth/login` | Recebe `{ email, senha }`, retorna `{ token, time }` |
| `GET /api/auth/me` | Retorna dados do usuário autenticado + timeId |
| `POST /api/auth/logout` | Invalida o token (opcional se usar JWT stateless) |

### Modelo sugerido para o token
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "time": {
    "id": "uuid",
    "nome": "Nome do Time",
    "valorMensalidade": 80.00
  }
}
```

### Perfis de usuário necessários
- `ADMIN` — acessa o painel de gestão (jogos, jogadores, pagamentos, despesas, eventos)
- `ATLETA` — acessa apenas visualização pública do time (futuramente)

---

## 3. Times

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/time/v1` | Já existe |
| `GET /api/time/v1/{id}` | Já existe |
| `POST /api/time/v1` | Já existe |
| `PATCH /api/time/v1/{id}` | Já existe |
| `PATCH /api/time/v1/{id}/desativar` | Já existe |
| `PATCH /api/time/v1/{id}/ativar` | Já existe |

### Observação
O campo `codigo` (adicionado na V10) pode ser NULL para registros antigos. A busca por time deve usar o `id` (UUID), não o `codigo`.

---

## 4. Jogos

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/jogo/v1/time/{timeId}` | Já existe |
| `POST /api/jogo/v1` | Já existe |
| `PATCH /api/jogo/v1/{id}/finalizar` | Já existe |
| `PATCH /api/jogo/v1/{id}/cancelar` | Já existe |

### Endpoints necessários
| Endpoint | Prioridade | Observação |
|---|---|---|
| `GET /api/jogo/v1` | Alta | Listagem global para landing page |
| `PATCH /api/jogo/v1/{id}` | Média | Edição de jogo (adversário, local, data) |
| `DELETE /api/jogo/v1/{id}` | Baixa | Exclusão de jogo cancelado |

### Modelo do corpo para criação
```json
{
  "timeId": "uuid",
  "adversario": "Time Adversário",
  "local": "Ginásio Municipal",
  "dataHora": "2025-06-15T19:00:00",
  "observacoes": "Jogo válido pelo campeonato"
}
```

---

## 5. Jogadores (Usuários)

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/usuario/v1/time/{timeId}` | Já existe |
| `POST /api/usuario/v1` | Já existe |
| `PATCH /api/usuario/v1/{id}/desativar` | Já existe |
| `PATCH /api/usuario/v1/{id}/reativar` | Já existe |

### Endpoints necessários
| Endpoint | Prioridade | Observação |
|---|---|---|
| `PATCH /api/usuario/v1/{id}` | Média | Edição de dados do jogador |
| `GET /api/usuario/v1/{id}` | Baixa | Perfil individual do atleta |

### Observação sobre senha
Atualmente o frontend envia senha no cadastro. Quando auth real for implementada, o fluxo de criação de atleta deve ser separado do cadastro de usuário com login.

---

## 6. Pagamentos

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/pagamento/v1/time/{timeId}` | Já existe |
| `GET /api/pagamento/v1/pendentes/time/{timeId}` | Já existe |
| `POST /api/pagamento/v1` | Já existe |
| `POST /api/pagamento/v1/gerar-mensalidades` | Já existe |
| `PATCH /api/pagamento/v1/{id}/pagar` | Já existe |
| `PATCH /api/pagamento/v1/{id}/cancelar` | Já existe |

### Endpoints necessários
| Endpoint | Prioridade | Observação |
|---|---|---|
| `PATCH /api/pagamento/v1/{id}` | Baixa | Edição de valor/tipo de um pagamento |

### Modelo para gerar mensalidades
```json
{
  "timeId": "uuid",
  "mesReferencia": "2025-06-01"
}
```
Deve gerar um pagamento PENDENTE para cada usuário ativo do time no mês informado.

---

## 7. Despesas

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/despesa/v1/time/{timeId}` | Já existe |
| `POST /api/despesa/v1` | Já existe |
| `DELETE /api/despesa/v1/{id}` | Já existe |

### Endpoints necessários
| Endpoint | Prioridade | Observação |
|---|---|---|
| `PATCH /api/despesa/v1/{id}` | Média | Edição de valor/descrição/tipo |

### Tipos de despesa esperados pelo frontend
`ALUGUEL_QUADRA`, `UNIFORME`, `EVENTO`, `OUTROS`

---

## 8. Eventos

### Endpoints existentes
| Endpoint | Status |
|---|---|
| `GET /api/evento/v1/time/{timeId}` | Já existe |
| `POST /api/evento/v1` | Já existe |
| `PATCH /api/evento/v1/{id}/ativar` | Já existe |
| `PATCH /api/evento/v1/{id}/desativar` | Já existe |

### Endpoints necessários
| Endpoint | Prioridade | Observação |
|---|---|---|
| `GET /api/evento/v1` | Alta | Listagem global para landing page |
| `PATCH /api/evento/v1/{id}` | Média | Edição de nome/descrição/datas/valor |

---

## 9. Página Pública do Time (`/time/:id`)

Exibe dados de um time específico para visitantes (sem login).

### Endpoints utilizados
| Endpoint | Status |
|---|---|
| `GET /api/time/v1/{id}` | Já existe |
| `GET /api/jogo/v1/time/{timeId}` | Já existe |
| `GET /api/despesa/v1/time/{timeId}` | Já existe |
| `GET /api/evento/v1/time/{timeId}` | Já existe |

Todos retornam dados públicos. Quando auth for implementada, considerar quais campos ficam visíveis sem autenticação.

---

## 10. Dashboard

Consolida métricas do time autenticado.

### Endpoints utilizados
| Endpoint |
|---|
| `GET /api/jogo/v1/time/{timeId}` |
| `GET /api/usuario/v1/time/{timeId}` |
| `GET /api/pagamento/v1/pendentes/time/{timeId}` |
| `GET /api/despesa/v1/time/{timeId}` |

### Endpoint adicional desejável
| Endpoint | Observação |
|---|---|
| `GET /api/dashboard/v1/time/{timeId}` | Consolidado server-side com totais, próximo jogo, inadimplentes, despesas do mês — evita 4 chamadas separadas |

---

## 11. Considerações para Multi-tenancy / SaaS

Para escalar a plataforma como SaaS:

1. **Isolamento por time**: Todos os endpoints que retornam dados sensíveis devem validar que o `timeId` do token JWT corresponde ao `timeId` da requisição.

2. **Planos**: Sugestão de tabela `plano` com campos `nome`, `maxJogadores`, `maxJogos`, `preco`. FK `planoId` na tabela `time`.

3. **Onboarding**: Endpoint `POST /api/time/v1/cadastrar` com criação do time + usuário admin em uma transação.

4. **Auditoria**: Considerar tabela `audit_log` para registrar criação/edição/exclusão de registros sensíveis.

5. **Notificações**: Futuro endpoint `POST /api/notificacao/v1` para notificar atletas sobre jogos agendados e mensalidades vencendo.
