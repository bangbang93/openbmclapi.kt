---
agent: "agent"
model: "Claude Haiku"
tools: ["codebase", "githubRepo"]
description: "Generate comprehensive tests for Kotlin code"
---

# Write Tests for Kotlin Code

Your task is to generate comprehensive tests for Kotlin code in the OpenBMCLAPI Agent project.

## Before You Start

Ask for these details if not provided:
1. **Code to Test**: Which function, class, or module needs tests? (Provide code snippet or file reference)
2. **Test Scope**: What should be tested? (specific methods, happy path, error cases, all of the above?)
3. **Dependencies**: What are the external dependencies? (database, services, external APIs?)
4. **Test Data**: Any special test data or fixtures needed?

## Test Generation Requirements

Follow project testing standards:

**Test Structure:**
- Test class name: `[SubjectUnderTest]Test` (e.g., `BootstrapServiceTest`)
- Use `@BeforeTest` and `@AfterTest` for setup/cleanup
- Descriptive test method names using backticks: `` `should do something` ``
- Follow Arrange-Act-Assert pattern

**Mocking:**
- Use MockK for mock dependencies: `mockk<Interface>()`
- Use `coEvery` for suspend functions
- Mock only external dependencies, not the code under test
- Configure mocks to simulate different scenarios

**Test Coverage:**
- Test happy path (success case)
- Test error cases and exceptions
- Test edge cases (empty collections, null values, etc.)
- Test integration points with dependencies
- Include at least 3-5 test cases per public method

**Async Testing:**
- Use `runTest` for deterministic coroutine testing
- Use `runBlocking` for testing suspend functions in unit tests
- Test async error handling

**Integration Tests:**
- For HTTP endpoints, use `testApplication` from Ktor
- Test status codes, request/response bodies
- Verify proper error responses

## Test Quality Standards

- **Clarity**: Test names and structure should be self-documenting
- **Independence**: Each test should run independently and in any order
- **Determinism**: No time-dependent or random behavior in tests
- **Isolation**: Mock external dependencies; don't use real databases/services
- **Meaningful Assertions**: Assert on relevant outcomes, not implementation details

## Generation Steps

1. Analyze the code to understand its behavior and dependencies
2. Identify critical paths and edge cases
3. Generate mock configurations for all dependencies
4. Create test cases for:
   - Happy path (normal operation)
   - Error conditions (expected exceptions)
   - Edge cases (boundary conditions)
   - Integration points (interaction with mocked dependencies)
5. Suggest test data builders if needed for complex fixtures
6. Include example assertions and verification steps

## Runningthe Generated Tests

Provide commands to run the tests:
```bash
./gradlew test --tests "TestClassName"
./gradlew test --tests "TestClassName.testMethodName"
```

## Code Style Reminders

- Follow [Testing Guidelines](../instructions/testing.instructions.md)
- Use MockK properly: `coEvery`, `every`, `verify`
- Test method naming: `` `should return result when input is valid` ``
- Use `assertEquals`, `assertTrue`, `assertFailsWith` from Kotlin Test
- Log important test assertions as comments

## Output Format

Provide:
1. Complete test class with all test methods
2. Mock setup code and fixtures
3. Brief explanation of what each test verifies
4. Any helper methods or builders needed
5. Command to run the generated tests

Reference [Testing Guidelines](../instructions/testing.instructions.md) for established patterns.
