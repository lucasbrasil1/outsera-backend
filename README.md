## Outsera Backend – Cálculo de Intervalos de Prêmios de Produtores

Aplicação **Spring Boot** que lê um arquivo CSV de filmes e calcula os **menores e maiores intervalos de tempo** entre prêmios consecutivos de produtores, expondo o resultado por meio de um endpoint REST.

---

### 1. Descrição do projeto

Este backend:

- **Carrega** um arquivo CSV de filmes configurado em `application.properties` (`csv.file`).
- **Persiste** os dados em um banco **H2 em memória**.
- **Filtra** apenas os filmes vencedores (`winner = yes`).
- **Agrupa** os filmes por produtor (suportando múltiplos produtores por filme, separados por vírgula e/ou `" and "`).
- **Calcula** todos os intervalos de anos entre prêmios consecutivos de cada produtor.
- **Retorna**:
  - a lista de produtores com o **menor intervalo** entre vitórias; e
  - a lista de produtores com o **maior intervalo** entre vitórias.

O resultado é exposto no endpoint `GET /api/v1/producers/award-intervals`.

---

### 2. Pré-requisitos

- **Java 21** (JDK 21 instalado e configurado no `PATH`)
- **Maven 3.8+**
- **Git** (para clonar o repositório)

---

### 3. Como clonar e instalar

```bash
git clone <URL_DO_REPOSITORIO>
cd backend

# Compilar e rodar testes
mvn install
```

> Observação: se preferir limpar o diretório `target` antes da compilação, utilize `mvn clean install`.

---

### 4. Como rodar a aplicação

Execute o comando abaixo na raiz do projeto:

```bash
mvn spring-boot:run
```

Por padrão, a aplicação sobe em:

- **Base URL**: `http://localhost:8080`
- **Banco de dados**: H2 em memória (/console para acessar o banco)
- **Arquivo CSV**: configurado em `src/main/resources/application.properties` pela propriedade:

```properties
csv.file=Movielist.csv
```

Certifique-se de que o arquivo `Movielist.csv` (com o formato descrito mais abaixo) esteja disponível em `src/main/resources` ou em um local acessível pelo classpath.

---

### 5. Como executar os testes de integracao

Para executar:

```bash
mvn test 
```

Os testes de integração utilizam:

- Teste com perfil **`default`** com configuração em `src/test/resources/application.properties`.
Este teste utiliza o CSV `src/resoucers/Movielist.csv` e garante que os dados de entrada estão de 
acordo com os dados pra proposta:

```properties
csv.file=Movielist.csv
```

- Teste com perfil **`test`** (`@ActiveProfiles("test")`), com configuração em `src/test/resources/application-test.properties`, esse garante o funcionamento da aplicação de inicio ao fim. Com um CSV de fixture específico para testes:

```properties
csv.file=award-intervals-fixture.csv
```

Esse arquivo está em `src/test/resources/award-intervals-fixture.csv` e contém dados controlados para validar precisamente os intervalos mínimos e máximos retornados pelo endpoint.

---

### 6. Documentação do endpoint `GET /producers/award-intervals`

- **Método**: `GET`
- **URL completa**: `http://localhost:8080/api/v1/producers/award-intervals`
- **Type**: `application/json`

#### 6.1. Resposta de sucesso (`200 OK`)

Corpo da resposta: objeto JSON com dois arrays, `min` e `max`, contendo os intervalos mínimos e máximos de tempo entre prêmios consecutivos por produtor.

Cada elemento dos arrays possui:

- **`producer`** (`string`): nome do produtor.
- **`interval`** (`number`): intervalo em anos entre duas vitórias consecutivas.
- **`previousWin`** (`number`): ano da vitória anterior.
- **`followingWin`** (`number`): ano da vitória seguinte.

Exemplo de resposta (com base no CSV de fixture usado nos testes de integração):

```json
{
  "min": [
    {
      "producer": "Prod A",
      "interval": 3,
      "previousWin": 2000,
      "followingWin": 2003
    },
    {
      "producer": "Prod B",
      "interval": 3,
      "previousWin": 2001,
      "followingWin": 2004
    }
  ],
  "max": [
    {
      "producer": "Prod C",
      "interval": 10,
      "previousWin": 1995,
      "followingWin": 2005
    }
  ]
}
```

#### 6.2. Códigos de status possíveis

- **`200 OK`**: cálculo realizado com sucesso.
- **`500 Internal Server Error`**: erro inesperado ao carregar o CSV ou processar os dados (por exemplo, arquivo ausente ou formato inválido).

### 7. Build e verificação local

Para garantir que o projeto **compila sem erros** e que **todos os testes passam**, execute na raiz do projeto:

```bash
mvn clean package
```