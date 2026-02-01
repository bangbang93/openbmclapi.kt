---
applyTo: "**/*Test.kt,**/*Tests.kt"
description: "Testing standards for OpenBMCLAPI Kotlin Agent"
---

# Testing Guidelines

## Test Structure and Organization

- **Test Class Naming**: Suffix with `Test` (e.g., `BootstrapServiceTest`) or `Tests`
- **Test File Location**: Place tests in `src/test/kotlin` with same package structure as source
- **Test Methods**: Use descriptive names that explain what is being tested, using backticks for spaces: `` `should validate input correctly` ``
- **Test Organization**: Group related tests using nested classes or separate test methods
- **Setup and Cleanup**: Use `@BeforeTest` and `@AfterTest` annotations for initialization and cleanup

## Test Patterns

### Arrange-Act-Assert Pattern
Every test should follow three phases:
1. **Arrange**: Set up test data and dependencies
2. **Act**: Execute the code being tested
3. **Assert**: Verify the results match expectations

```kotlin
@Test
fun `should return error when input is invalid`() {
    // Arrange
    val input = createInvalidTestInput()

    // Act
    val result = serviceUnderTest.process(input)

    // Assert
    assertEquals(expected, result)
}
```

## Unit Testing

- **Test One Thing**: Each test should verify one behavior or outcome
- **Meaningful Assertions**: Use clear assertion messages
- **Mock Dependencies**: Use MockK to mock external dependencies
- **Avoid Test Interdependence**: Each test should be independent and runnable in any order
- **Fast Execution**: Unit tests should run quickly; avoid external I/O when possible

### Mocking with MockK

```kotlin
val mockStorage = mockk<IStorage> {
    coEvery { check() } returns true
    coEvery { init() } returns Unit
    every { isReady() } returns true
}

// For suspend functions, use coEvery
val mockService = mockk<Service> {
    coEvery { doWork() } returns Result.success(Unit)
}
```

## Integration Testing

- **Use testApplication**: Ktor provides `testApplication` for testing HTTP endpoints
- **Test Full Request/Response**: Verify HTTP status codes, headers, and body content
- **Bootstrap Application**: Properly initialize the application in test scope
- **Clean Up Resources**: Ensure resources are cleaned up after tests

```kotlin
@Test
fun `GET /endpoint should return OK`() = testApplication {
    application {
        runBlocking { this@application.appModule() }
    }

    client.get("/endpoint").apply {
        assertEquals(HttpStatusCode.OK, status)
        assertEquals("expected", bodyAsText())
    }
}
```

## Testing Suspend Functions

- **Use runBlocking in Tests**: When testing suspend functions outside of testApplication
- **Use coEvery for MockK**: Mock suspend functions with `coEvery` not `every`
- **await Results**: Call `await()` or `awaitAll()` on async blocks
- **Timeout Protection**: Consider timeouts for suspend function tests to prevent hanging

```kotlin
@Test
fun `suspend function should complete`() = runTest {
    val result = suspendingFunction()
    assertEquals(expected, result)
}
```

## Testing Coroutines

- **Use runTest**: Kotlin Test provides `runTest` for deterministic coroutine testing
- **Advance Time**: Use `advanceTimeBy()` and `runCurrent()` to control time in tests
- **Test Structured Concurrency**: Verify proper job cancellation and scope behavior

## Testing Exception Handling

- **Verify Exceptions are Thrown**: Use `assertFails` or `assertFailsWith`
- **Test Error Messages**: Verify exception messages are meaningful
- **Test Recovery**: For Result types, verify error branches behave correctly

```kotlin
@Test
fun `should throw when operation fails`() {
    assertFailsWith<IllegalArgumentException> {
        riskyOperation()
    }
}
```

## Testing Logging

- **Avoid Testing Logs Directly**: Focus on behavior, not logging
- **Verify Important Actions**: Log messages can be verified if they're important for compliance
- **Use Test Hooks**: If needed, capture logs via appender configuration for assertions

## Test Coverage

- **Aim for Meaningful Coverage**: Coverage percentage is less important than covering critical paths
- **Test Happy Paths and Error Cases**: Include both success and failure scenarios
- **Test Edge Cases**: Empty collections, null values, boundary conditions
- **Test Public APIs**: Focus coverage on public methods; private methods are tested through public ones
- **Use Code Review**: Have peers review tests to ensure they're meaningful

## Running Tests

See [AGENTS.md](../../AGENTS.md) for test execution commands:
- `./gradlew test` - Run all tests
- `./gradlew test --tests "TestClassName"` - Run specific test class
- `./gradlew test --tests "TestClassName.testMethodName"` - Run specific test method

## Common Test Utilities

### Creating Test Fixtures
```kotlin
fun createTestConfig(
    clusterId: String = "test-cluster",
    clusterSecret: String = "test-secret",
): ClusterConfig {
    return ClusterConfig(clusterId, clusterSecret)
}
```

### Test Builders
Use builder patterns for complex test data:
```kotlin
data class TestBuilder(
    var name: String = "default",
    var value: Int = 0,
) {
    fun withName(name: String) = apply { this.name = name }
    fun withValue(value: Int) = apply { this.value = value }
    fun build() = TestData(name, value)
}
```

## Best Practices

- **Use Clear Names**: Test method names should explain what is being tested
- **One Assertion Focus**: While multiple assertions are OK, keep them focused on one behavior
- **Avoid Test Duplication**: Extract common setup into helper functions or `@BeforeTest`
- **Test Readability**: Prioritize readability over brevity; test code is documentation
- **Isolate External Dependencies**: Mock external systems (databases, APIs, file systems)
- **Deterministic Tests**: Avoid time-dependent tests; use fixed dates or mock time
- **Keep Tests Maintainable**: When code changes, update tests promptly

## See Also

- [AGENTS.md - Testing Guidelines](../../AGENTS.md)
- [Kotlin Test Documentation](https://kotlinlang.org/api/latest/kotlin.test/)
- [MockK Documentation](https://mockk.io/)
- [Ktor Testing Documentation](https://ktor.io/docs/testing.html)
