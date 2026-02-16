# 📘 MD02 – Arquitetura do Sistema
## 🎯 Objetivo

Definir a arquitetura técnica do sistema Futsal Manager, estabelecendo:

- Camadas da aplicação

- Responsabilidades

- Padrões adotados

- Diretrizes estruturais

# 🏗 Estilo Arquitetural

O sistema seguirá o padrão:

Arquitetura em Camadas (Layered Architecture)

Separação clara entre:

- Camada de Apresentação

- Camada de Aplicação

- Camada de Domínio

- Camada de Infraestrutura

Essa separação reduz acoplamento, melhora testabilidade e facilita evolução.

# 🧱 Estrutura de Camadas
## 1️⃣ Camada de Apresentação (Controller)

Responsável por:

- Receber requisições HTTP

- Validar dados de entrada

- Retornar respostas (DTO)

- Mapear erros para códigos HTTP adequados

Não deve conter regra de negócio.

Exemplo futuro:

- AtletaController

- JogoController 

- PagamentoController 

- DespesaController 

## 2️⃣ Camada de Aplicação (Service)

Responsável por:

- Implementar regras de negócio

- Orquestrar entidades

- Garantir integridade transacional

- Aplicar validações de domínio

Aqui ficam as decisões do sistema.

Exemplo:

- CadastroAtletaService

- RegistrarPagamentoService

- CriarJogoService

## 3️⃣ Camada de Domínio

Contém:

- Entidades

- Enumerações

- Regras essenciais do negócio

- Contratos (interfaces)

Representa o coração do sistema.

Exemplo:

- Atleta
- Time
- Jogo
- Pagamento
- Despesa
- TipoPagamento

## 4️⃣ Camada de Infraestrutura

Responsável por:

- Persistência (JPA / Hibernate)

- Repositórios

- Integrações externas

- Configurações técnicas

- Segurança

Exemplo:

- AtletaRepositorio
  
- JogoRepositorio

- ConfiguracaoBanco

- ConfiguracaoSeguranca

# 🗂 Estrutura de Pacotes (Planejada)
```text
com.futsalmanager

├── apresentacao
│   ├── controller
│   └── dto
│
├── aplicacao
│   └── service
│
├── dominio
│   ├── entidade
│   └── enumeracao
│
└── infraestrutura
    ├── repositorio
    └── configuracao
```

# 🔐 Segurança (Planejamento Inicial)

- Autenticação via JWT

- Perfis:

  - ADMIN

  - ATLETA

- Controle de acesso baseado em perfil

- Filtro obrigatório por time_id

# 💾 Banco de Dados

- PostgreSQL

- UUID como chave primária

- Controle de auditoria

- Integridade referencial via chave estrangeira

Migração versionada com Flyway (planejado).

# 📡 API

- RESTful

- JSON

- Padrão de resposta consistente

- Tratamento global de exceções

# ⚙️ Tecnologias Definidas

- Java 21

- Spring Boot

- Spring Data JPA

- Spring Security

- PostgreSQL

- Flyway

- Maven

# 📈 Estratégia de Evolução

Fase 1 – MVP

- Cadastro de time

- Cadastro de atletas

- Gestão de jogos

- Controle de pagamentos

- Controle de despesas

Fase 2

- Estatísticas

- Relatórios

- Upload de imagens

Fase 3

- Multi-tenant completo

- Deploy em nuvem

- Monetização

# 📌 Diretrizes Arquiteturais Importantes

- Nunca expor entidade diretamente na API

- Utilizar DTO para entrada e saída

- Regra de negócio apenas na camada de aplicação

- Sempre validar o time_id do usuário autenticado

- Código 100% em português

Versão do Documento: 1.1
Data: 2026-02-15
