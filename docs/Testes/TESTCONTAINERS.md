# Testes com TestContainers e PostgreSQL

## Visão Geral

Os testes com TestContainers permitem testar a aplicação com um **PostgreSQL real em container Docker**, em vez de usar um banco em memória como H2.

## Configuração

### 1. **Pré-requisitos**

- Docker instalado e em execução
- Docker Desktop ou Docker Engine rodando na máquina

### 2. **Classes Disponíveis**

#### `AbstractTestcontainersTest`
Classe base que configura automaticamente um container PostgreSQL para os testes:

```java
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = AbstractTestcontainersTest.PostgresInitializer.class)
public abstract class AbstractTestcontainersTest {
    // Container PostgreSQL configurado automaticamente
}
```

#### `TimeControllerPostgresIntegrationTest`
Testes de integração do Time Controller com PostgreSQL real.

**Testes inclusos:**
- ✅ Criar time com PostgreSQL
- ✅ Persistência de dados
- ✅ Concorrência com múltiplos times
- ✅ Transações e rollback
- ✅ Atualização e sincronização

#### `UsuarioControllerPostgresIntegrationTest`
Testes de integração do Usuario Controller com PostgreSQL real.

**Testes inclusos:**
- ✅ Persistência de usuários
- ✅ Reativação e histórico
- ✅ Uniqueness de email (constraint do banco)
- ✅ Filtros por time
- ✅ Concorrência

## Como Usar

### Executar testes com PostgreSQL (requer Docker)

```bash
# Executar apenas testes PostgreSQL
mvn test -Dtest=*PostgresIntegrationTest

# Executar teste específico
mvn test -Dtest=TimeControllerPostgresIntegrationTest

# Executar com logs detalhados
mvn test -Dtest=TimeControllerPostgresIntegrationTest -e
```

### Executar testes padrão com H2 (sem Docker)

```bash
# Executar testes unitários e integração H2
mvn test -Dtest=TimeControllerIntegrationTest
mvn test -Dtest=UsuarioControllerIntegrationTest
```

### Executar todos os testes

```bash
# Isso executará:
# - Testes unitários (81)
# - Testes de integração H2 (18)
# - Testes de integração PostgreSQL (10+) - apenas se Docker estiver disponível
mvn test
```

## Diferenças H2 vs PostgreSQL

| Aspecto | H2 | PostgreSQL |
|---------|----|----|
| **Setup** | Automático | Requer Docker |
| **Velocidade** | Rápido | Mais lento (container) |
| **Realismo** | Banco em memória | Banco real |
| **Constraints** | Básicos | Completos |
| **Performance** | Mock | Real |
| **SQL Dialeto** | H2 SQL | PostgreSQL SQL |

## Testes Disponíveis com TestContainers

### TimeControllerPostgresIntegrationTest

1. **create_DeveCriarTime_ComPostgreSQL**
   - Testa criação de time com PostgreSQL real
   - Valida resposta HTTP e persistência

2. **persistencia_DeveManterDadosNoPostgreSQL**
   - Verifica que dados persistem corretamente no banco
   - Testa leitura e validação de dados salvos

3. **concorrencia_DeveGerenciarMultiplosTimes**
   - Cria 5 times concorrentemente
   - Valida que todos são salvos corretamente

4. **transacao_DeveReverterEmCasoDeErro**
   - Testa rollback automático em caso de erro
   - Valida que dados inválidos não persistem

5. **update_DeveAtualizarNoPostgreSQL**
   - Testa atualização de dados
   - Valida persistência de mudanças

### UsuarioControllerPostgresIntegrationTest

1. **create_DevePersistirUsuarioNoPostgreSQL**
   - Testa criação com banco real
   - Valida persistência

2. **reativacao_DeveManterHistoricoNoPostgreSQL**
   - Testa desativação e reativação
   - Valida estado no banco

3. **emailUniqueness_DeveEnforcarRestricaoNoBanco**
   - Testa constraint UNIQUE de email
   - Valida que duplicatas são rejeitadas

4. **findByTime_DeveRetornarApenasUsuariosDoTime**
   - Testa filtros com relações de chave estrangeira
   - Valida integridade referencial

5. **concorrencia_DeveGerenciarMultiplosUsuariosDoMesmoTime**
   - Cria múltiplos usuários do mesmo time
   - Valida concorrência e integridade

## Estrutura dos Testes

```
src/test/java/
├── com/futsalmanager/
│   ├── testcontainers/
│   │   └── AbstractTestcontainersTest.java (Base para PostgreSQL)
│   ├── application/services/
│   │   ├── *ServiceTest.java (81 testes unitários)
│   ├── api/controller/
│   │   ├── *ControllerIntegrationTest.java (H2, 18 testes)
│   │   └── *ControllerPostgresIntegrationTest.java (PostgreSQL, 10+ testes)
```

## Configuração Automática

Os testes com TestContainers usam a classe `PostgresInitializer` que:

1. ✅ Inicia um container PostgreSQL automaticamente
2. ✅ Configura spring.datasource.url com JDBC do container
3. ✅ Define usuário/senha do PostgreSQL
4. ✅ Habilita `ddl-auto: create-drop` para criar/limpar schema a cada teste
5. ✅ Limpa dados após cada teste (rollback transacional)

## Exemplo de Execução

```bash
$ mvn test -Dtest=TimeControllerPostgresIntegrationTest -X

[INFO] --- testcontainers:1.19.7 ---
[INFO] Starting PostgreSQL Container: postgres:16-alpine
[INFO] Container started with ID: 7f8a2c...
[INFO] JDBC URL: jdbc:postgresql://localhost:32768/futsaldb_test
[INFO] 
[INFO] Tests run: 5, Failures: 0, Errors: 0
[INFO] PostgreSQL Container stopped
```

## Troubleshooting

### Docker não está disponível
- Desabilite temporariamente TestContainers
- Use apenas testes H2
- Instale Docker e tente novamente

### Container não inicia
- Verifique se Docker está rodando: `docker ps`
- Verifique logs: `docker logs <container_id>`
- Tente iniciar Docker manualmente

### Testes lentos
- Testcontainers precisa iniciar container (overhead normal)
- Use testes H2 para desenvolvimento rápido
- Use PostgreSQL para CI/CD final

## Benefícios do TestContainers

✅ **Teste com banco real** - Não há surpresas em produção
✅ **Constraints do banco** - Valida UNIQUE, FK, etc
✅ **Performance real** - Testa com dados reais
✅ **SQL dialeto** - PostgreSQL específico
✅ **Isolamento** - Cada teste tem container próprio
✅ **CI/CD ready** - Funciona em pipelines Docker

## Próximos Passos

1. Instale Docker localmente
2. Execute: `mvn test -Dtest=*PostgresIntegrationTest`
3. Valide que os testes passam com PostgreSQL real
4. Configure no seu CI/CD (GitHub Actions, GitLab CI, etc)

