---
applyTo: "**/*.kt"
description: "Kotlin development standards for OpenBMCLAPI"
---

# Kotlin Development Standards

## Naming Conventions

- **Classes and Interfaces**: `PascalCase` (e.g., `BootstrapService`, `IStorage`)
- **Functions and Methods**: `camelCase` (e.g., `getToken()`, `setupCertificates()`)
- **Properties**: `camelCase` (e.g., `clusterId`, `clusterSecret`)
- **Constants**: `SCREAMING_SNAKE_CASE` (e.g., `AGENT_PROTOCOL_VERSION`)
- **Packages**: `lowercase.with.dots` (e.g., `com.bangbang93.openbmclapi.agent`)
- **Use nouns for classes and verbs for methods**: `UserService.getUserById()` not `UserHandler.handleGetUser()`
- **Avoid abbreviations and Hungarian notation**: Use `fileSize` not `fSize` or `iFileSize`

## Type Annotations and Null Safety

- **Public APIs**: Always provide explicit type annotations for function parameters and return types
- **Local Variables**: Type inference is acceptable when the type is obvious from context
- **Nullable Types**: Always use `?` for nullable types explicitly: `var keystorePath: String? = null`
- **Null Safety**: Prefer `Optional<T>` alternatives and Elvis operator `?:` over null checks
- **Type Inference with `var`**: Acceptable for local variables where type is clear from assignment

## Function and Class Structure

- **Suspend Functions**: Use for all async operations: `suspend fun bootstrap()`
- **Private Functions**: Use `camelCase`: `private fun scheduleFileCheck(lastModified: Long)`
- **Extension Functions**: Apply them where they improve readability and reduce code duplication
- **Data Classes**: Use for value objects and DTOs when they represent immutable data
- **Sealed Classes**: Use for type-safe alternatives to when needed
- **Value Objects**: Create thin wrapper types when appropriate for domain modeling

## Coroutines and Async Programming

- **Use `suspend` functions** for all potentially blocking operations
- **Structured Concurrency**: Always use `coroutineScope` for proper scope management
- **Dispatchers**: Use `Dispatchers.IO` for I/O operations, `Dispatchers.Default` for CPU-bound work
- **async/await**: Use `async { }` with `awaitAll()` for parallel operations when needed
- **Flow**: Use Kotlin Flow for reactive streams and asynchronous sequences
- **No GlobalScope**: Never use `GlobalScope` for new code; use appropriate scope builders

## Logging

- **Use KotlinLogging**: Initialize with `private val logger = KotlinLogging.logger {}`
- **Lazy Evaluation**: Always use lambda: `logger.info { "Processing ${files.size} files" }` not string concatenation
- **Log Levels**: Use appropriate levels (debug, info, warn, error)
- **Exception Logging**: Use `logger.error(exception) { "Failed to process" }` to include stack traces
- **Avoid Sensitive Data**: Never log passwords, tokens, or personal information

## Dependency Injection with Koin

- **Service Classes**: Use `@Single` annotation for singleton services
- **Constructor Injection**: Prefer constructor parameters for dependencies
- **Lazy Initialization**: Use `lazy<T>` when injection timing is important
- **Factory Functions**: Create `@Single` functions for complex configuration objects
- **Ktor Integration**: Inject dependencies in Ktor application modules: `val service by inject<MyService>()`

## Error Handling and Exceptions

- **Try-Catch Blocks**: Use for expected exceptions, always log context
- **Resource Management**: Use try-with-resources or ensure cleanup: `try { resource.use { } }`
- **Result Type**: Use `Result<T>` for operations that can fail but shouldn't throw
- **Custom Exceptions**: Create domain-specific exception types when appropriate
- **Exception Re-throwing**: Only re-throw if upstream code needs to handle it
- **Never Swallow Exceptions**: Always log or handle exceptions explicitly

## Collections and Streams

- **Immutable Collections**: Prefer `List.of()`, `Map.of()` for fixed data
- **Sequences**: Use `.asSequence()` for lazy evaluation with multiple operations
- **Streams API**: Use `stream()` and lambdas for collection processing
- **Method References**: Prefer `stream.map(Foo::toBar)` over `stream.map { it.toBar() }`
- **Avoid Mutable Globals**: Use immutable defaults; use `var` only when necessary

## Ktor-Specific Patterns

- **Route Handlers**: Keep handlers focused; move business logic to services
- **Suspend Functions**: All Ktor route handlers are suspend functions by design
- **Application Configuration**: Use `Application.module()` extension function
- **Dependency Injection in Modules**: Use `val service by inject<ServiceType>()` pattern
- **Status Responses**: Use Ktor's HttpStatusCode enums, not raw numbers
- **Content Negotiation**: Configure JSON serialization in application setup

## Best Practices

- **Keep Functions Small**: Extract helper functions to improve readability and testability
- **Single Responsibility**: Each class should have one reason to change
- **DRY Principle**: Don't repeat code; extract common patterns into utility functions or extensions
- **Immutability First**: Make properties `val` and classes `data class` when appropriate
- **Fail Fast**: Validate inputs early and throw meaningful exceptions
- **Documentation**: Write KDoc for public APIs; comments should explain "why," not "what"
- **Kotlin Idioms**: Prefer `when` over chained `if-else`; use destructuring; prefer `let`, `apply`, `run`

## Performance Considerations

- **Lazy Collections**: Use `Sequence` for operations on large collections
- **String Building**: Use `buildString` or string templates, not concatenation
- **Object Creation**: Reuse objects when possible; be mindful of object allocation in hot paths
- **Coroutine Overhead**: Use appropriate scope and dispatcher to avoid unnecessary context switching
- **Memory**: Be mindful of object references in closures and suspending functions

## Common Patterns

### Service Implementation
```kotlin
@Single
class MyService(
    private val dependency: IDependency,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun doWork(): Result<Output> = try {
        logger.info { "Starting work" }
        Result.success(dependency.execute())
    } catch (e: Exception) {
        logger.error(e) { "Work failed" }
        Result.failure(e)
    }
}
```

### Ktor Route Handler
```kotlin
suspend fun Application.setupRoutes() {
    val service by inject<MyService>()

    routing {
        get("/endpoint") {
            try {
                val result = service.doWork()
                call.respond(result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
```

### Configuration Factory
```kotlin
@Single
fun getConfig(env: ApplicationEnvironment): ClusterConfig {
    return ClusterConfig(
        id = System.getenv("CLUSTER_ID") ?: "",
        secret = System.getenv("CLUSTER_SECRET") ?: "",
        port = System.getenv("CLUSTER_PORT")?.toIntOrNull() ?: 4000,
    )
}
```

## See Also

- [AGENTS.md - Project Build Commands](../../AGENTS.md)
- [Testing Guidelines](./testing.instructions.md)
- Official [Kotlin Conventions](https://kotlinlang.org/docs/coding-conventions.html)
