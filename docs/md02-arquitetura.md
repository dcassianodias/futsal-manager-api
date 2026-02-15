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

- Separação clara entre:

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


