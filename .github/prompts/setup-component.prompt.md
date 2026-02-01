---
agent: "agent"
model: "Claude Haiku"
tools: ["githubRepo", "codebase"]
description: "Generate a new Kotlin service class with dependency injection"
---

# Generate Kotlin Service Component

Your task is to generate a new Kotlin service class for the OpenBMCLAPI Agent project.

## Before You Start

Ask the user for these details if not provided:
1. **Service Name**: What should this service be called? (e.g., `TokenManager`, `FileProcessor`)
2. **Responsibilities**: What should this service do? (Describe 2-3 main responsibilities)
3. **Dependencies**: What other services or components does it need? (List them)
4. **Public Methods**: What public methods should it expose? (List method names and descriptions)
5. **Async Operations**: Are any operations async (use suspend functions)?

## Requirements for Generated Service

Follow these standards from the project:

**Structure:**
- Use `@Single` annotation for Koin dependency injection
- Constructor-inject all dependencies
- Place in appropriate `service/` subdirectory based on functionality
- Follow the project's established service patterns

**Code Quality:**
- Use Kotlin idioms (null-coalescing, scope functions, etc.)
- Use suspend functions for async operations
- Proper error handling with try-catch and logging
- Use KotlinLogging for logging
- Follow [Kotlin Development Standards](../instructions/kotlin.instructions.md)

**Documentation:**
- Add KDoc comments for all public methods
- Explain complex logic with inline comments
- Include usage examples if non-obvious

**Testing:**
- Suggest test class structure with MockK mocks for dependencies
- Include example test cases following Arrange-Act-Assert pattern
- Suggest how to test error cases

## Generation Steps

1. Validate all required information is provided
2. Generate the service class with:
   - Proper Koin `@Single` annotation
   - Constructor dependency injection
   - Comprehensive error handling and logging
   - KDoc documentation
   - Kotlin idioms throughout
3. Suggest test class structure with example tests
4. Provide implementation guidance for specific business logic
5. Suggest where to register/use the service in the application

## Code Style Reminders

- Follow [AGENTS.md Code Style Guidelines](../../AGENTS.md)
- Use KotlinLogging: `private val logger = KotlinLogging.logger {}`
- Lazy log evaluation: `logger.info { "Processing" }` not concatenation
- Suspend functions for async: `suspend fun doWork(): Result<T>`
- Proper null-safety with `?:` and `.let { }`
- Type annotations on public APIs

## Output Format

Provide:
1. Complete service class with proper formatting
2. Associated test class structure
3. Integration guidance (where to use this service)
4. Any configuration needed for this service

Reference [project patterns](../../AGENTS.md) for established service implementations.
