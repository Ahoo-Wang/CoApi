# CoApi Testing

Load this reference when writing or reviewing tests for CoApi clients, registrars, factory beans, or
Spring Boot auto-configuration.

## Contents

- [Repository Conventions](#repository-conventions)
- [Definition Parsing Unit Test](#definition-parsing-unit-test)
- [HTTP Client Factory Precedence Test](#http-client-factory-precedence-test)
- [ApplicationContextRunner Test](#applicationcontextrunner-test)
- [Spring Boot Integration Test](#spring-boot-integration-test)
- [MockK Client Test](#mockk-client-test)
- [Coverage Checklist](#coverage-checklist)

## Repository Conventions

- Test framework: JUnit 5.
- Mocking: MockK.
- Assertions: `me.ahoo.test.asserts.assert`.
- Do not use AssertJ `assertThat`.
- Useful commands:
  - `./gradlew :spring:test`
  - `./gradlew :spring-boot-starter:test`
  - `./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"`

Import:

```kotlin
import me.ahoo.test.asserts.assert
```

## Definition Parsing Unit Test

Use `MockEnvironment` for annotation and property resolution.

```kotlin
class CoApiDefinitionTest {
    @Test
    fun toCoApiDefinitionIfServiceApi() {
        val coApiDefinition = MockServiceApi::class.java.toCoApiDefinition(MockEnvironment())

        coApiDefinition.loadBalanced.assert().isTrue()
        coApiDefinition.baseUrl.assert().isEqualTo("http://order-service")
    }
}

@CoApi(serviceId = "order-service")
interface MockServiceApi
```

## HTTP Client Factory Precedence Test

When testing final base URL selection, assert that per-client configuration wins over the parsed
definition URL.

```kotlin
@Test
fun `getBaseUrl should return URL from properties when available`() {
    val mockApplicationContext = mockk<ApplicationContext>()
    val mockClientProperties = mockk<ClientProperties>()

    every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
    every { mockClientProperties.getBaseUri("testClient") } returns "http://properties-url:9090"

    val factoryBean = TestHttpClientFactoryBean(mockDefinition)
    factoryBean.setApplicationContext(mockApplicationContext)

    factoryBean.getBaseUrl().assert().isEqualTo("http://properties-url:9090")
}
```

## ApplicationContextRunner Test

Use `ApplicationContextRunner` for auto-configuration and bean registration assertions.

```kotlin
class CoApiContextTest {
    @Test
    fun `should create reactive CoApi bean`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .run { context ->
                context.assert()
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
            }
    }
}

@EnableCoApi(clients = [GitHubApiClient::class])
class EnableCoApiConfiguration
```

## Spring Boot Integration Test

Use this style when the test should start the application context and exercise a real client bean.

```kotlin
@SpringBootTest
class ConsumerServerTest {
    @Autowired
    private lateinit var gitHubApiClient: GitHubApiClient

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "CoApi")
            .doOnNext { println(it) }
            .blockLast()
    }
}
```

## MockK Client Test

Mock generated client interfaces directly when the unit under test only depends on the contract.

```kotlin
@Test
fun `should mock CoApi client`() {
    val mockClient = mockk<GitHubApiClient>()
    every { mockClient.getIssue("owner", "repo") } returns Flux.just(Issue("url"))

    mockClient.getIssue("owner", "repo")
        .blockFirst()
        .assert()
        .isEqualTo(Issue("url"))
}
```

## Coverage Checklist

For behavior changes, cover the smallest relevant layer:

- Annotation parsing or property precedence: unit test around `CoApiDefinition`.
- Registrar behavior: `ApplicationContextRunner`.
- Reactive vs sync mode selection: focused tests for `ClientMode` or adapter factory registration.
- Starter auto-configuration: `spring-boot-starter` tests.
- Example behavior: example module tests only when the public workflow changes.
