---
agent: "agent"
model: "Claude Haiku"
tools: ["codebase", "githubRepo"]
description: "Debug issues in Kotlin code"
---

# Debug Kotlin Issues

Your task is to help debug issues in the OpenBMCLAPI Agent Kotlin project.

## Before You Start

Ask for these details if not provided:
1. **Issue Description**: What's the problem? (error message, unexpected behavior, etc.)
2. **Error/Output**: Any error messages, logs, or stack traces?
3. **Code Location**: Where is the issue? (provide file path and line numbers if known)
4. **Reproduction Steps**: Steps to reproduce the issue
5. **Recent Changes**: Any recent code changes that might be related?
6. **Environment**: Running environment (local, Docker, CI/CD, etc.)

## Debugging Process

### 1. Understand the Problem

- **Gather Information**: Collect all relevant error messages, logs, stack traces
- **Identify Symptoms**: What exactly is going wrong?
- **Scope the Issue**: Is it in specific code, a dependency, configuration?
- **Environment Factors**: Could it be environment-specific?

### 2. Analyze Error Messages

- **Read Stack Trace**: Follow the call stack to find root cause
- **Identify Exception Type**: What exception is being thrown?
- **Check Root Cause**: Look for the "Caused by" section
- **Examine Log Context**: What was happening before the error?

### 3. Isolate the Problem

- **Reproduce Locally**: Try to reproduce the issue in a simple test
- **Minimal Example**: Create smallest possible reproduction
- **Check Dependencies**: Verify all dependencies are correctly configured
- **Review Recent Changes**: Look at recent git commits for cause

### 4. Common Issues to Check

#### Build Issues
- [ ] All dependencies installed: `./gradlew dependencies`
- [ ] Gradle clean build: `./gradlew clean build`
- [ ] JDK version correct: Java 11+
- [ ] Kotlin version: 2.2.20+

#### Runtime Issues
- [ ] Configuration loaded correctly: Check environment variables
- [ ] Dependencies injected properly: Verify Koin setup
- [ ] Null pointer exceptions: Check null safety
- [ ] Resource leaks: Verify cleanup in try/finally

#### Test Issues
- [ ] Test isolation: Tests don't interfere with each other
- [ ] Mock configuration: Mocks set up correctly
- [ ] Test data: Fixtures created correctly
- [ ] Async handling: Suspend functions awaited properly

#### Performance Issues
- [ ] Database queries: Check for N+1 problems
- [ ] Memory leaks: Monitor heap usage
- [ ] Resource exhaustion: Check for unbounded growth
- [ ] Blocking operations: Verify no blocking on coroutine threads

### 5. Debugging Tools and Techniques

#### Using Logs
```kotlin
// Add detailed logging
logger.debug { "About to process: $item" }
val result = processItem(item)
logger.debug { "Result: $result" }

// Check log levels
logger.info { "Important milestone reached" }
logger.warn { "Unusual but recoverable condition" }
logger.error(exception) { "Error occurred" }
```

#### Using Breakpoints (IDE)
- Set breakpoint at suspected location
- Run with debugger: `./gradlew run --debug-jvm`
- Inspect variables at breakpoint
- Step through code execution

#### Using Print Statements (Temporary)
```kotlin
println("DEBUG: Variable value = $value")
// Or better with logging:
logger.debug { "Variable value = $value" }
```

#### Testing in Isolation
```kotlin
@Test
fun `reproduce the issue`() {
    // Simplified reproduction of the issue
    val input = createProblematicInput()
    val result = functionUnderTest(input)
    // Assert on unexpected behavior
}
```

### 6. Investigation Checklist

For the issue, check:
- [ ] Error message fully understood
- [ ] Stack trace analyzed
- [ ] Issue reproduced locally
- [ ] Problem isolated to specific code
- [ ] Dependencies verified
- [ ] Configuration correct
- [ ] Recent changes identified
- [ ] Logs examined for context

### 7. Common Solutions

#### NullPointerException
- Check null-safety in Kotlin code
- Use safe operators: `?.` and `?:`
- Use `Objects.requireNonNull()` for validation

#### Timeout Issues
- Increase timeout configuration
- Check for deadlocks or infinite loops
- Verify async operations complete
- Check resource availability

#### Dependency Injection Issues
- Verify Koin module registered
- Check `@Single` annotation present
- Verify constructor parameters
- Check dependency order

#### Serialization Issues
- Verify JSON structure matches class
- Check optional fields nullability
- Verify custom serializers if used
- Check data type mismatches

#### Resource Leaks
- Verify files/connections closed
- Use try-with-resources
- Check coroutine cleanup
- Monitor resource usage

## Debug Output Example

Create minimal reproducible example:

```kotlin
fun main() {
    // Setup
    val config = createTestConfig()
    val service = MyService(config)

    // Reproduce issue
    try {
        val result = service.problematicOperation()
        println("Result: $result")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}
```

## Viewing Logs

View application logs:
```bash
# Run application
./gradlew run

# View build output logs
./gradlew build --info

# View test logs
./gradlew test --debug

# Check application logs (if file logging configured)
tail -f logs/application.log
```

## Getting Help

If unable to resolve:
1. **Create Minimal Reproduction**: Provide smallest example that reproduces issue
2. **Collect Artifacts**: Provide logs, error messages, stack traces
3. **Document Environment**: OS, JDK version, Gradle version
4. **Check Dependencies**: Ensure all dependencies properly installed
5. **Review Recent Changes**: Git diff to see what changed

## Output Format

When reporting findings:
1. **Issue Summary**: What is the problem?
2. **Root Cause**: What's causing it?
3. **Affected Code**: Which code is problematic?
4. **Reproduction Steps**: How to reproduce?
5. **Solution**: How to fix it?
6. **Verification**: How to verify fix works?
7. **Prevention**: How to prevent in future?

## Documentation

- **Document Solution**: Add to troubleshooting guide if common issue
- **Update Code**: Fix the issue with proper error handling
- **Add Tests**: Add test to prevent regression
- **Update Comments**: Document why this workaround exists if applicable

## Build and Test Commands

```bash
# Full clean build and test
./gradlew clean build

# Run with debug logging
./gradlew run --info

# Run specific test
./gradlew test --tests "TestClassName"

# Check for dependency issues
./gradlew dependencies
./gradlew dependencyTree

# Run formatter check
./gradlew ktfmtCheck
```

## See Also

- [Kotlin Development Standards](../instructions/kotlin.instructions.md)
- [Testing Guidelines](../instructions/testing.instructions.md)
- [Ktor Documentation](https://ktor.io/docs/)
- [AGENTS.md - Build Commands](../../AGENTS.md)
