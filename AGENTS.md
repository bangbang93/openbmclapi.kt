# OpenBMCLAPI Kotlin Agent - Development Guide

This document provides essential information for coding agents working on the OpenBMCLAPI Kotlin project.

## Project Overview

OpenBMCLAPI Kotlin Agent is a distributed file distribution network node for accelerating Minecraft resource downloads in China. It's a Kotlin/JVM project using Ktor framework, migrated from the original TypeScript/Node.js version.

**Technology Stack:**
- Language: Kotlin 2.2.20 (JVM target, Java 11+ required)
- Web Framework: Ktor 3.3.1 with Netty server
- Dependency Injection: Koin 4.1.1 with KSP annotations
- Build System: Gradle with Kotlin DSL
- Testing: Kotlin Test with JUnit, MockK for mocking

## Build Commands

### Core Commands
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "ApplicationTest"

# Run a specific test method
./gradlew test --tests "ApplicationTest.主页返回OK"

# Format code with ktfmt
./gradlew ktfmtFormat

# Check code style
./gradlew ktfmtCheck

# Create executable JAR
./gradlew buildFatJar

# Clean build artifacts
./gradlew clean
```

### Development Commands
```bash
# Continuous build (watches for changes)
./gradlew build --continuous

# Run with specific profile
./gradlew run --args="--config=application-dev.yaml"

# Generate dependency report
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates
```

## Code Style Guidelines

### Formatting Rules (.editorconfig)
- **Indentation:** 4 spaces (no tabs)
- **Line Length:** 140 characters for Kotlin files, 120 for others
- **Line Endings:** LF (Unix-style)
- **Final Newline:** Required
- **Trailing Whitespace:** Trimmed

### Import Organization
```kotlin
// 1. Standard library imports
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

// 2. Third-party library imports
import io.ktor.server.application.Application
import org.koin.core.annotation.Single

// 3. Project imports (grouped by package)
import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.service.BootstrapService
```

### Naming Conventions
- **Classes:** PascalCase (`BootstrapService`, `ClusterConfig`)
- **Functions:** camelCase (`getToken()`, `setupCertificates()`)
- **Properties:** camelCase (`clusterId`, `clusterSecret`)
- **Constants:** SCREAMING_SNAKE_CASE (`AGENT_PROTOCOL_VERSION`)
- **Packages:** lowercase with dots (`com.bangbang93.openbmclapi.agent`)

### Type Annotations
```kotlin
// Explicit types for public APIs
fun getConfig(env: ApplicationEnvironment): ClusterConfig

// Type inference for local variables is acceptable
val server = embeddedServer(Netty, env)

// Nullable types explicitly declared
var keystorePath: String? = null

// Collection types with generics
val storageOpts: Map<String, String> = emptyMap()
```

### Function Structure
```kotlin
// Suspend functions for async operations
suspend fun bootstrap() {
    logger.info { "Starting bootstrap process" }
    // Implementation
}

// Private functions use camelCase
private fun scheduleFileCheck(lastModified: Long) {
    // Implementation
}

// Extension functions when appropriate
suspend fun Application.module() {
    // Configuration
}
```

### Error Handling
```kotlin
// Use try-catch for expected exceptions
try {
    val result = riskyOperation()
    logger.info { "Operation succeeded: $result" }
} catch (e: Exception) {
    logger.error(e) { "Operation failed" }
    throw e // Re-throw if needed
}

// Use Result type for recoverable errors when appropriate
fun parseConfig(input: String): Result<Config> {
    return try {
        Result.success(Json.decodeFromString(input))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Logging
```kotlin
// Use KotlinLogging with structured logging
private val logger = KotlinLogging.logger {}

// Log with lambda for lazy evaluation
logger.info { "Processing ${files.size} files" }
logger.error(exception) { "Failed to process request" }

// Use appropriate log levels
logger.debug { "Debug information" }
logger.info { "General information" }
logger.warn { "Warning message" }
logger.error(exception) { "Error with exception" }
```

### Dependency Injection (Koin)
```kotlin
// Service classes use @Single annotation
@Single
class BootstrapService(
    private val storage: IStorage,
    private val tokenManager: TokenManager,
) {
    // Implementation
}

// Factory functions for configuration
@Single
fun getConfig(env: ApplicationEnvironment): ClusterConfig {
    // Configuration logic
}

// Inject dependencies in Ktor applications
suspend fun Application.module() {
    val bootstrapService by inject<BootstrapService>()
}
```

## Project Structure

```
src/main/kotlin/com/bangbang93/openbmclapi/agent/
├── Application.kt              # Main entry point
├── AppModule.kt               # Koin DI configuration
├── config/                    # Configuration classes
│   ├── Config.kt             # Main configuration
│   ├── Constants.kt          # Application constants
│   └── Version.kt            # Version management
├── model/                     # Data models and DTOs
├── nat/                      # NAT/UPnP support
├── routes/                   # HTTP route handlers
├── service/                  # Business logic services
├── storage/                  # Storage backend implementations
└── util/                     # Utility functions
```

## Testing Guidelines

### Test Structure
```kotlin
class ServiceTest {
    @BeforeTest
    fun setup() {
        // Test setup
    }

    @AfterTest
    fun cleanup() {
        // Test cleanup
    }

    @Test
    fun `should handle normal case`() {
        // Arrange
        val input = createTestInput()

        // Act
        val result = serviceUnderTest.process(input)

        // Assert
        assertEquals(expected, result)
    }
}
```

### Mocking with MockK
```kotlin
val mockStorage = mockk<IStorage> {
    coEvery { check() } returns true
    coEvery { init() } returns Unit
}
```

### Integration Tests
```kotlin
@Test
fun `integration test with test application`() = testApplication {
    application {
        runBlocking { this@application.appModule() }
    }

    client.get("/").apply {
        assertEquals(HttpStatusCode.OK, status)
    }
}
```

## Common Patterns

### Coroutines Usage
```kotlin
// Use appropriate dispatchers
CoroutineScope(Dispatchers.IO).launch {
    // I/O operations
}

// Structured concurrency
suspend fun processFiles() = coroutineScope {
    val jobs = files.map { file ->
        async { processFile(file) }
    }
    jobs.awaitAll()
}
```

### Configuration Management
- Use `application.yaml` for Ktor configuration
- Use `.env` files for environment-specific settings (gitignored)
- Support multiple configuration sources (Ktor config, dotenv, env vars, system props)
- Provide sensible defaults for development

### Storage Abstraction
- Implement `IStorage` interface for new storage backends
- Use factory pattern for storage creation
- Support multiple storage types: file, WebDAV, MinIO, Aliyun OSS

## Development Workflow

1. **Before Making Changes:**
    - Run `./gradlew ktfmtCheck` to verify code style
   - Run `./gradlew test` to ensure all tests pass

2. **During Development:**
   - Use `./gradlew build --continuous` for automatic rebuilds
   - Write tests for new functionality
   - Follow existing patterns and conventions

3. **Before Committing:**
    - Run `./gradlew ktfmtFormat` to format code
   - Run `./gradlew build` to ensure everything compiles
   - Run `./gradlew test` to verify all tests pass

## Key Dependencies

- **Ktor:** Web framework and HTTP client
- **Koin:** Dependency injection with KSP code generation
- **Kotlinx Coroutines:** Async programming
- **Socket.IO:** Cluster communication
- **Logback:** Logging implementation
- **BouncyCastle:** SSL/TLS certificate handling
- **Various Storage:** MinIO, Aliyun OSS, WebDAV clients

## Environment Setup

Required environment variables (see `.env.example`):
- `CLUSTER_ID`: Cluster identifier
- `CLUSTER_SECRET`: Cluster authentication secret
- `CLUSTER_PORT`: Server port (default: 4000)
- `CLUSTER_STORAGE`: Storage backend type (default: "file")

Optional variables:
- `CLUSTER_IP`: Public IP address
- `ENABLE_UPNP`: Enable UPnP NAT traversal
- `SSL_KEY`/`SSL_CERT`: Custom SSL certificates (BYOC mode)
