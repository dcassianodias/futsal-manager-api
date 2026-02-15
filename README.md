# futsal-manager-api
Aplicativo para gestão de times de futsal

# ⚽ Futsal Manager API

Sistema de gestão para times amadores de futsal.

O objetivo do projeto é centralizar a organização de jogos, controle financeiro e comunicação entre atletas e administradores, eliminando o uso de planilhas e controles informais.

---

## 🎯 Objetivo do Projeto

- Organizar jogos
- Controlar presença de atletas
- Gerenciar mensalidades
- Registrar despesas do time
- Fornecer transparência financeira

---

## 🚀 Tecnologias Utilizadas

- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security (JWT)
- PostgreSQL
- Flyway
- Docker
- Angular (Frontend - PWA)

---

## 🏗 Arquitetura

O projeto segue uma arquitetura MVC organizada:

Controller  
↓  
Service  
↓  
Repository  
↓  
Database  

Preparado desde o início para evolução futura como SaaS (multi-tenant).

---

## 📦 Estrutura do Projeto

src/main/java/com/futsalmanager
- ├── controller
- ├── service
- ├── repository
- ├── entity
- ├── dto
- ├── security
- ├── config
- └── exception


Documentação detalhada disponível na pasta `/docs`.

---

## 🛣 Roadmap

### Fase 1 – MVP
- Autenticação
- Cadastro de time
- Cadastro de atletas
- Gestão de jogos
- Controle de pagamentos

### Fase 2
- Estatísticas básicas
- Upload de imagens
- Relatórios

### Fase 3
- Multi-tenant completo
- Deploy em nuvem
- Monetização

---

## 📌 Status

🚧 Em desenvolvimento

---

## 📄 Documentação

A documentação detalhada pode ser encontrada na pasta `/docs`.

---

Desenvolvido por Danilo Cassiano Dias



