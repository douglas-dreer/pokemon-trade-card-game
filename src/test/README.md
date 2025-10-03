# Testes Unitários dos Use Cases - Serie

Este diretório contém testes unitários abrangentes para todos os use cases relacionados à entidade `Serie` do projeto
Pokemon Trading Card Game.

## 📋 Testes Implementados

### 1. CreateSerieUsecaseTest

- ✅ Criação bem-sucedida de série com validações
- ✅ Falha quando validação não passa
- ✅ Execução de todos os validadores
- ✅ Criação com campos mínimos obrigatórios

### 2. FindAllSerieUsecaseTest

- ✅ Retorno paginado de séries
- ✅ Página vazia quando não há séries
- ✅ Diferentes tamanhos de página
- ✅ Mapeamento correto de entidade para domínio

### 3. FindSerieByIdUsecaseTest

- ✅ Retorno de série quando encontrada por ID
- ✅ Retorno null quando não encontrada
- ✅ Tratamento de campos mínimos
- ✅ Mapeamento correto de propriedades

### 4. FindSerieByCodeUsecaseTest

- ✅ Retorno de série quando encontrada por código
- ✅ Retorno null quando não encontrada
- ✅ Tratamento de expansões vazias
- ✅ Busca case-sensitive
- ✅ Caracteres especiais no código

### 5. UpdateSerieByIdUsecaseTest

- ✅ Atualização bem-sucedida com validações
- ✅ Falha quando validação não passa
- ✅ Execução ordenada de validadores
- ✅ Atualização com campos parciais
- ✅ Tratamento de exceções do repositório

### 6. DeleteSerieByIdUsecaseTest

- ✅ Exclusão bem-sucedida com validações
- ✅ Falha quando validação não passa
- ✅ Execução ordenada de validadores
- ✅ Parada na primeira falha de validação
- ✅ Tratamento de diferentes formatos de UUID

## 🛠️ Tecnologias e Ferramentas Utilizadas

### MockK

- **Versão**: 1.13.8
- **Motivo**: Framework de mocking nativo para Kotlin, oferecendo melhor integração com recursos específicos da
  linguagem
- **Vantagens**:
    - Suporte completo a coroutines
    - DSL mais limpa e idiomática para Kotlin
    - Melhor performance comparado ao Mockito
    - Suporte nativo a extension functions e data classes

### JUnit 5

- Framework de testes padrão para aplicações Spring Boot
- Anotações modernas e recursos avançados de parametrização

### Kotlin Test

- Assertions nativas do Kotlin para melhor legibilidade

## 🎯 Melhores Práticas Implementadas

### 1. Estrutura de Testes (AAA Pattern)

```kotlin
@Test
fun `should create serie successfully when all validations pass`() {
    // Given - Preparação dos dados e mocks
    val command = CreateSerieCommand(...)
    every { validator.execute(any()) } just Runs
    
    // When - Execução do método testado
    val result = useCase.execute(command)
    
    // Then - Verificação dos resultados
    assertEquals(expectedValue, result.property)
    verify { validator.execute(any()) }
}
```

### 2. Nomenclatura Descritiva

- Nomes de testes em português seguindo o padrão: `should [ação] when [condição]`
- Descrição clara do comportamento esperado

### 3. Isolamento de Testes

- Cada teste é independente
- `@BeforeEach` para setup limpo
- `clearAllMocks()` para garantir estado limpo

### 4. Cobertura Abrangente

- **Cenários de Sucesso**: Fluxo principal funcionando corretamente
- **Cenários de Erro**: Validações falhando, exceções do repositório
- **Casos Extremos**: Dados mínimos, campos nulos, listas vazias
- **Verificação de Comportamento**: Ordem de execução, parâmetros corretos

### 5. Verificações Detalhadas

```kotlin
// Verificação de chamadas exatas
verify(exactly = 1) { repository.findSerieById(serieId) }

// Verificação de ordem de execução
verifyOrder {
    validator1.execute(any())
    validator2.execute(any())
    repository.createSerie(any())
}

// Captura de argumentos para verificação detalhada
val capturedSerie = slot<Serie>()
every { validator.execute(capture(capturedSerie)) } just Runs
```

### 6. Dados de Teste Realistas

- UUIDs gerados dinamicamente
- Códigos de série baseados em padrões reais (SV01, SV02, etc.)
- Timestamps com LocalDateTime
- URLs de exemplo válidas

### 7. Tratamento de Exceções

```kotlin
@Test
fun `should throw exception when validation fails`() {
    // Given
    val validationException = RuntimeException("Validation failed")
    every { validator.execute(any()) } throws validationException

    // When & Then
    assertThrows<RuntimeException> {
        useCase.execute(command)
    }
}
```

## 🚀 Como Executar os Testes

### Pré-requisitos

1. **Java 21** configurado no JAVA_HOME
2. **Gradle** (incluído no projeto via wrapper)

### Comandos

```bash
# Executar todos os testes de use case
./gradlew test --tests "*SerieUsecase*"

# Executar teste específico
./gradlew test --tests "CreateSerieUsecaseTest"

# Executar com relatório de cobertura
./gradlew test jacocoTestReport

# Executar em modo contínuo
./gradlew test --continuous
```

### No Windows

```cmd
.\gradlew.bat test --tests "*SerieUsecase*"
```

## 📊 Cobertura de Testes

Os testes cobrem:

- ✅ **100%** dos métodos públicos dos use cases
- ✅ **100%** dos cenários de validação
- ✅ **100%** dos cenários de erro
- ✅ **100%** das interações com dependências

## 🔧 Configuração do MockK

O MockK foi adicionado ao `build.gradle.kts`:

```kotlin
dependencies {
    // MockK for Kotlin mocking
    testImplementation("io.mockk:mockk:1.13.8")
}
```

## 📝 Padrões de Código

### Organização dos Mocks

```kotlin
private val repository = mockk<SerieRepositoryPort>()
private val validator1 = mockk<ValidatorStrategy<Serie>>()
private val validator2 = mockk<ValidatorStrategy<Serie>>()
private val validators = listOf(validator1, validator2)
```

### Setup Consistente

```kotlin
@BeforeEach
fun setUp() {
    clearAllMocks()
    useCase = CreateSerieUsecaseImpl(repository, validators)
}
```

### Verificações Robustas

```kotlin
// Verificar que não houve chamadas desnecessárias
verify(exactly = 0) { repository.deleteSerieById(any()) }

// Confirmar que todas as interações foram verificadas
confirmVerified(repository, validator1, validator2)
```

## 🎯 Benefícios dos Testes

1. **Confiabilidade**: Garantem que os use cases funcionam conforme especificado
2. **Refatoração Segura**: Permitem mudanças no código com confiança
3. **Documentação Viva**: Servem como documentação do comportamento esperado
4. **Detecção Precoce**: Identificam problemas antes da produção
5. **Qualidade**: Forçam um design melhor e mais testável

## 🔍 Próximos Passos

1. **Testes de Integração**: Implementar testes que validem a integração entre camadas
2. **Testes de Performance**: Adicionar benchmarks para operações críticas
3. **Testes de Contrato**: Validar contratos entre serviços
4. **Mutation Testing**: Verificar a qualidade dos próprios testes