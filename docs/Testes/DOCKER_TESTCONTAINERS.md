# 🐳 Guia de Configuração: Docker + TestContainers

## 📋 Sumário Rápido

1. **Instalar Docker**
2. **Configurar TestContainers** 
3. **Executar testes com PostgreSQL**

---

## 1️⃣ Instalação do Docker

### Windows

#### Opção A: Docker Desktop (Recomendado)
1. Baixe [Docker Desktop para Windows](https://www.docker.com/products/docker-desktop)
2. Execute o instalador
3. Siga as instruções na tela
4. Reinicie o computador
5. Verifique a instalação:
   ```bash
   docker --version
   docker run hello-world
   ```

#### Opção B: Windows Subsystem for Linux (WSL 2)
1. Habilite WSL 2
2. Instale Docker via WSL 2
3. Configure Docker Desktop para usar WSL 2

### macOS

1. Baixe [Docker Desktop para Mac](https://www.docker.com/products/docker-desktop)
2. Abra o instalador `.dmg`
3. Arraste Docker para Applications
4. Inicie Docker Desktop
5. Verifique:
   ```bash
   docker --version
   ```

### Linux

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER

# Verify
docker --version
```

---

## 2️⃣ Verificar Docker

```bash
# Verificar se Docker está rodando
docker ps

# Baixar imagem PostgreSQL (opcional, TestContainers faz automaticamente)
docker pull postgres:16-alpine

# Testar com hello-world
docker run hello-world
```

**Output esperado:**
```
CONTAINER ID   IMAGE                 COMMAND             STATUS
abc123...      hello-world           "/hello"            Exited
```

---

## 3️⃣ Configurar TestContainers

### Pré-requisitos
- ✅ Docker instalado
- ✅ Docker daemon rodando
- ✅ Maven com acesso à internet

### Dependências (já adicionadas ao pom.xml)
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.7</version>
    <scope>test</scope>
</dependency>
```

### Classe Base (já criada)
```java
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = AbstractTestcontainersTest.PostgresInitializer.class)
public abstract class AbstractTestcontainersTest {
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("futsaldb_test")
            .withUsername("futsaluser")
            .withPassword("futsalpass");
    // ...
}
```

---

## 4️⃣ Executar Testes com PostgreSQL

### Executar Testes PostgreSQL
```bash
cd C:\Projetos\futsal-manager

# Executar todos os testes PostgreSQL
mvn test -Dtest=*PostgresIntegrationTest

# Executar teste específico
mvn test -Dtest=TimeControllerPostgresIntegrationTest

# Com saída detalhada
mvn test -Dtest=TimeControllerPostgresIntegrationTest -e
```

### Monitorar Container em Tempo Real
Em outro terminal:
```bash
# Ver containers rodando
docker ps

# Ver logs do container
docker logs <container_id>

# Parar container manualmente (se necessário)
docker stop <container_id>
```

---

## 5️⃣ Troubleshooting

### Problema: "Docker daemon is not running"

**Solução:**
```bash
# Windows
# 1. Abra Docker Desktop
# 2. Aguarde inicialização completa

# macOS
# 1. Abra Applications > Docker
# 2. Aguarde a ícone aparecer na barra superior

# Linux
sudo systemctl start docker
```

### Problema: "Permission denied while trying to connect to Docker daemon"

**Solução (Linux):**
```bash
sudo usermod -aG docker $USER
newgrp docker
# Faça logout e login novamente
```

### Problema: "Timeout waiting for container startup"

**Possíveis causas:**
- Docker não tem espaço em disco
- Imagem PostgreSQL não baixou completamente
- Firewall bloqueando Docker

**Solução:**
```bash
# Limpar containers antigos
docker system prune -a

# Baixar imagem manualmente
docker pull postgres:16-alpine

# Verificar espaço em disco
df -h  # Linux/Mac
dir C:  # Windows
```

### Problema: Testes rodando lentamente

**Esperado:** Testes PostgreSQL são ~2-3x mais lentos que H2
- Iniciar container: ~10 segundos
- Executar testes: ~30-60 segundos
- Parar container: ~5 segundos

**Otimização:**
```bash
# Usar H2 para desenvolvimento rápido
mvn test -Dtest=!*Postgres*

# Usar PostgreSQL apenas em CI/CD
```

---

## 6️⃣ Executar Ambos H2 e PostgreSQL

### Todos os testes (H2 + PostgreSQL)
```bash
mvn test
```

**Output esperado:**
```
[INFO] Tests run: 81, ... (Unit Tests)
[INFO] Tests run: 18, ... (Integration H2)
[INFO] Tests run: 10, ... (Integration PostgreSQL)
[INFO] Tests run: 1,  ... (Application Context)
[INFO] Tests run: 110, Failures: 0, Errors: 0 ✅
```

### Apenas H2 (rápido, sem Docker)
```bash
mvn test -Dtest=!*Postgres*
```

**Output esperado:**
```
[INFO] Tests run: 99, Failures: 0, Errors: 0 ✅
```

### Apenas PostgreSQL (requer Docker)
```bash
mvn test -Dtest=*Postgres*
```

**Output esperado:**
```
[INFO] Tests run: 10, Failures: 0, Errors: 0 ✅
```

---

## 7️⃣ Integração com CI/CD

### GitHub Actions com TestContainers

```yaml
name: Tests with Docker

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      docker:
        image: docker:dind
        options: >-
          --privileged
          --health-cmd "docker ps"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      
      - name: Run all tests (H2 + PostgreSQL)
        run: mvn clean test
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports/
```

### GitLab CI com TestContainers

```yaml
test:
  image: maven:3.8-openjdk-21
  services:
    - docker:dind
  script:
    - mvn clean test
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
```

---

## 8️⃣ Boas Práticas

### ✅ FAÇA
- ✅ Use H2 para desenvolvimento rápido
- ✅ Use PostgreSQL em CI/CD para validação final
- ✅ Mantenha testes independentes
- ✅ Use @Transactional para cleanup automático
- ✅ Monitore containers durante execução

### ❌ NÃO FAÇA
- ❌ Não instale Docker em máquinas compartilhadas sem avisar
- ❌ Não deixe containers rodando indefinidamente
- ❌ Não dependa de ordem específica de execução
- ❌ Não use dados externos em testes
- ❌ Não ignore avisos de TestContainers

---

## 9️⃣ Verificação Final

```bash
# Checklist de verificação

# 1. Docker instalado?
docker --version
# esperado: Docker version X.X.X

# 2. Docker rodando?
docker ps
# esperado: lista de containers (pode estar vazia)

# 3. Testes H2 passam?
mvn test -Dtest=!*Postgres*
# esperado: 99 tests passed

# 4. Testes PostgreSQL passam? (se Docker está rodando)
mvn test -Dtest=*Postgres*
# esperado: 10+ tests passed

# ✅ Pronto para usar TestContainers!
```

---

## 🔗 Links Úteis

- [Docker Official](https://www.docker.com/)
- [TestContainers Official](https://www.testcontainers.org/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Spring Boot + TestContainers](https://spring.io/blog/2023/06/19/integration-testing-improvements-in-spring-boot-3-1-0/)

---

## 📞 Suporte

Se encontrar problemas:

1. **Limpe cache e reconstrua:**
   ```bash
   mvn clean
   docker system prune -a
   mvn test
   ```

2. **Verifique logs:**
   ```bash
   mvn test -Dtest=*Postgres* -X 2>&1 | grep -i error
   ```

3. **Consulte docs:**
   - [Troubleshooting TestContainers](https://www.testcontainers.org/support/)
   - [Docker Troubleshooting](https://docs.docker.com/config/daemon/troubleshoot/)

---

**Status: ✅ Pronto para usar TestContainers com PostgreSQL!**

