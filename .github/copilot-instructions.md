---
description: "OpenBMCLAPI Kotlin Agent - Repository-wide coding standards"
---

# OpenBMCLAPI Kotlin Agent - Copilot Instructions

This document provides project-wide standards for developing the OpenBMCLAPI Kotlin Agent, a distributed Minecraft resource distribution network node built with Kotlin and Ktor.

## Project Overview

- **Language**: Kotlin 2.2.20 (JVM 11+)
- **Framework**: Ktor 3.3.1 with Netty server
- **Dependency Injection**: Koin 4.1.1
- **Build System**: Gradle with Kotlin DSL
- **Testing**: Kotlin Test with JUnit and MockK

## Core Development Principles

1. **Follow Established Patterns**: Use patterns documented in [AGENTS.md](../../AGENTS.md) as the foundation for all development decisions
2. **Kotlin-First**: Leverage Kotlin idioms and the standard library over Java approaches
3. **Coroutine-Based**: Use structured concurrency and suspend functions for all async operations
4. **Type Safety**: Maximize compile-time safety through proper type annotations and null-safety
5. **Clean Architecture**: Maintain clear separation of concerns between routes, services, models, and storage layers

## Code Organization

Follow the project structure defined in AGENTS.md:
- `config/`: Configuration classes and constants
- `model/`: Data models and DTOs
- `routes/`: HTTP endpoint handlers
- `service/`: Business logic and core functionality
- `storage/`: Storage backend implementations
- `nat/`: NAT/UPnP support
- `util/`: Utility functions and extensions

## Code Style

- **Formatting**: 4-space indentation, 140-character line limit for Kotlin, LF line endings
- **Naming**: PascalCase for classes, camelCase for functions/properties, SCREAMING_SNAKE_CASE for constants
- **Imports**: Organize as: stdlib → third-party → project imports
- **Type Annotations**: Always explicit for public APIs, can infer for local variables
- **Logging**: Use KotlinLogging with lazy evaluation (`logger.info { "message" }`)

## Dependency Injection (Koin)

- Use `@Single` annotation for singleton services
- Prefer constructor injection over property injection
- Create factory functions for complex configuration objects
- Always inject in Ktor modules using `inject<T>()`

## Error Handling

- Use try-catch for expected exceptions with proper logging
- Use `Result<T>` type for recoverable errors when appropriate
- Always log exceptions with context
- Re-throw only when necessary for upstream handling
- Avoid silent failures or generic catch blocks

## Testing Requirements

- Write tests for all public functions and significant business logic
- Use Kotlin Test with JUnit for test classes
- Use MockK for mocking dependencies
- Follow Arrange-Act-Assert pattern
- Use integration tests with `testApplication` for HTTP endpoints
- Aim for meaningful test coverage, not 100% coverage

## Build Verification

Before committing changes:
1. Run `./gradlew ktlintFormat` to format code
2. Run `./gradlew ktlintCheck` to verify style
3. Run `./gradlew build` to compile and run tests
4. Ensure all tests pass

## Performance Considerations

- Use appropriate Dispatchers for I/O vs. CPU-bound work
- Implement structured concurrency with `coroutineScope`
- Profile before optimizing; avoid premature optimization
- Consider memory implications of collection operations
- Use sequences for lazy evaluation of collections when appropriate

## Security Best Practices

- Always validate input from external sources
- Use HTTPS/TLS for cluster communication
- Handle SSL certificates securely (use BYOC mode or proper secret management)
- Store sensitive configuration in environment variables
- Never log sensitive information (credentials, tokens)
- Keep dependencies up to date; monitor for security updates

## Documentation

- Document public APIs with KDoc comments
- Include examples for non-obvious functionality
- Update AGENTS.md when modifying development patterns
- Keep code comments focused on "why," not "what"
- Document configuration options and environment variables

## For More Details

Refer to:
- [Kotlin Development Standards](./instructions/kotlin.instructions.md)
- [Testing Guidelines](./instructions/testing.instructions.md)
- [Code Review Standards](./instructions/code-review.instructions.md)
- [Security Practices](./instructions/security.instructions.md)
- [Performance Guidelines](./instructions/performance.instructions.md)
- [Project AGENTS.md](../../AGENTS.md) for build commands and patterns
