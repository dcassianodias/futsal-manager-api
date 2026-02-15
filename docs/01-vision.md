# Futsal Manager – Architecture Document

## 1. Architectural Overview

O sistema será composto por três camadas principais:

Frontend (Angular PWA)  
↓  
Backend (Spring Boot API)  
↓  
Database (PostgreSQL)

A comunicação entre frontend e backend será feita via API REST utilizando JSON.

---

## 2. Stack Tecnológica Oficial

### Backend
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security (JWT)
- Flyway
- PostgreSQL
- Docker

### Frontend
- Angular 17+
- Angular Material
- PWA (Progressive Web App)

---

## 3. Arquitetura do Backend

O projeto seguirá uma arquitetura MVC organizada e desacoplada.

### Estrutura de pacotes:

com.futsalmanager
- ├── controller
- ├── service
- ├── repository
- ├── entity
- ├── dto
- ├── security
- ├── config
- └── exception


### Responsabilidades:

- Controller → Camada HTTP (entrada/saída)
- Service → Regras de negócio
- Repository → Acesso a dados
- Entity → Mapeamento JPA
- DTO → Objetos de transporte
- Security → Configuração de autenticação/autorização

Regras importantes:
- Controller não contém regra de negócio
- Entity nunca é retornada diretamente
- Service centraliza regras
- Repository não contém lógica

---

## 4. Estratégia Multi-Tenant (Preparação para SaaS)

Desde a versão inicial, todas as entidades principais possuirão o campo:

team_id

Isso permitirá que, no futuro, múltiplos times utilizem o mesmo sistema de forma isolada.

Modelo adotado:

Single Database  
Single Schema  
Isolamento por coluna (team_id)

Todas as consultas deverão filtrar obrigatoriamente pelo team_id do usuário autenticado.

---

## 5. Segurança

A autenticação será baseada em:

- Login com e-mail e senha
- Geração de token JWT
- Envio do token no header Authorization

Papéis (roles):

- ADMIN
- ATHLETE

Regras:

- ADMIN pode gerenciar jogos, pagamentos e despesas
- ATHLETE pode visualizar informações e confirmar presença

---

## 6. Banco de Dados

Banco escolhido: PostgreSQL

Motivos:
- Robustez
- Suporte a evolução futura
- Compatibilidade com multi-tenant
- Melhor estrutura para crescimento do produto

Migrações serão controladas via Flyway.

---

## 7. Estratégia de Deploy (Futuro)

Backend:
- Container Docker
- Deploy em plataforma cloud (Render, Railway ou VPS)

Frontend:
- Deploy como aplicação estática (Vercel ou similar)

Banco:
- PostgreSQL gerenciado

---

## 8. Escalabilidade Futura

Possíveis evoluções:

- Push notifications
- Integração com gateway de pagamento
- Estatísticas avançadas
- Plano de assinatura
- Separação em microsserviços (se necessário)

---

## 9. Princípios Arquiteturais

- Código limpo
- Baixo acoplamento
- Alta coesão
- Separação clara de responsabilidades
- Evolução incremental

---

Document Version: 1.0  
Date: 2026-02-15
