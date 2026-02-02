---
applyTo: "**/*.kt"
description: "Code review standards for OpenBMCLAPI Kotlin Agent"
---

# Code Review Standards

## Review Goals

- **Correctness**: Ensure code functions as intended and handles edge cases
- **Maintainability**: Code should be understandable by other developers
- **Performance**: Identify potential performance bottlenecks or unnecessary allocations
- **Security**: Catch security issues, unsafe patterns, and credential handling problems
- **Consistency**: Verify adherence to project standards and established patterns
- **Test Coverage**: Ensure tests verify the new functionality

## Architecture and Design Review

- **Single Responsibility**: Does each class/function have one reason to change?
- **Separation of Concerns**: Are routes, services, and storage properly separated?
- **Dependency Injection**: Are dependencies properly injected via Koin?
- **Error Handling**: Is error handling appropriate and logged?
- **Null Safety**: Does the code handle null values safely?
- **Immutability**: Are objects immutable where appropriate?

## Code Quality Checks

### Kotlin Idioms
- Code uses Kotlin idioms appropriately (destructuring, scoping functions, scope functions)
- Prefer `when` over chained `if-else` statements
- Use `require()` and `check()` for preconditions
- Extension functions are used appropriately, not overused

### Naming and Readability
- Names are descriptive and follow established conventions (PascalCase, camelCase, SCREAMING_SNAKE_CASE)
- No abbreviations or Hungarian notation in names
- Function/variable names clearly indicate purpose
- Comments explain "why" not "what"; code should be self-documenting

### Function and Class Design
- Functions are small and focused (ideally <30 lines)
- Parameter lists are reasonable in length (<5 parameters)
- Return types are explicit for public APIs
- Complex logic is extracted into helper functions

## Coroutine and Async Review

- All async operations use `suspend` functions, not callbacks
- Proper use of structured concurrency with `coroutineScope`
- Correct dispatcher selection (IO vs Default vs Main)
- No `GlobalScope` usage; proper scope management
- Proper cancellation and cleanup of resources

## Error Handling Review

- Exceptions are caught and logged appropriately
- Meaningful error messages that help debugging
- No silent failures or empty catch blocks
- Result types used appropriately for recoverable errors
- Validation happens early in functions

## Security Review

- Input validation for external data
- No credentials or tokens in logs
- Sensitive configuration via environment variables
- Proper SSL/TLS certificate handling
- No hardcoded secrets or API keys
- Secure deserialization practices

## Testing Review

- Meaningful tests for new functionality
- Tests follow Arrange-Act-Assert pattern
- Mock dependencies appropriately
- Edge cases and error conditions covered
- Test names clearly describe what is tested
- Integration tests for HTTP endpoints when needed

## Performance Review

- No obvious performance issues or N+1 queries
- Appropriate use of lazy collections (Sequence)
- Reasonable object allocation in hot paths
- Resource cleanup and garbage collection
- Consider memory implications of operations

## Documentation Review

- Public APIs have KDoc comments
- Complex algorithms explained with comments
- Non-obvious decisions documented
- Examples provided for non-obvious functionality
- Configuration options documented

## Common Issues to Watch For

### Anti-patterns to Flag
- Swallowing exceptions without logging
- Using `!!` (not-null assertion) without justification
- Mutable global state or singletons
- Circular dependencies between modules
- Business logic in route handlers
- Lack of type safety with generic `Any` types

### Performance Anti-patterns
- String concatenation in loops
- Creating objects in tight loops unnecessarily
- Blocking operations on event threads
- Unbounded resource creation
- Inefficient collection operations

### Security Anti-patterns
- Storing credentials in configuration files
- Logging sensitive data
- Inadequate input validation
- Trusting all user input
- Weak SSL/TLS configuration

## Review Comments Best Practices

### Positive Comments
- ✅ Acknowledge good solutions and clever implementations
- ✅ Suggest improvements politely and constructively
- ✅ Provide context when requesting changes
- ✅ Offer alternative approaches when needed
- ✅ Celebrate code quality and adherence to standards

### Negative Comments
- ❌ Don't use vague language; be specific
- ❌ Avoid personal criticism; focus on code
- ❌ Don't approve changes you don't understand
- ❌ Don't request changes purely for personal preference
- ❌ Avoid bikeshedding; focus on significant issues

### Comment Format
```
[Category] Message

Example: [Performance] Consider using Sequence here for lazy evaluation
         [Security] Validate input before using in SQL query
         [Style] This follows our pattern; nice work!
```

## Review Checklist

Before approving a PR:
- [ ] Code compiles and all tests pass
- [ ] Follows Kotlin idioms and project conventions
- [ ] Error handling is appropriate and logged
- [ ] Tests verify new functionality
- [ ] No obvious performance issues
- [ ] No security concerns identified
- [ ] Documentation is updated if needed
- [ ] Code is maintainable and understandable

## Build Verification Requirements

Reviewers should verify:
- [ ] `./gradlew spotlessCheck` passes (code style compliance)
- [ ] `./gradlew build` completes successfully
- [ ] `./gradlew test` passes with reasonable coverage
- [ ] No new warnings introduced
- [ ] Dependencies are appropriate and up-to-date

## GitHub-Specific Review Guidelines

### Approving Changes
- Use "Approve" when changes meet standards
- Include constructive feedback even on approval
- Suggest improvements for future iterations

### Requesting Changes
- Use "Request Changes" only for blocking issues
- Be specific about what needs to change
- Provide guidance on how to fix issues
- Be available for discussion and clarification

### Comments
- Use inline comments for specific lines
- Use general comments for broader feedback
- Reference project standards when applicable
- Link to relevant documentation (AGENTS.md, instructions)

## Review Priorities

1. **Critical**: Security issues, data loss, test failures
2. **High**: Correctness, major performance issues, architectural concerns
3. **Medium**: Code style, maintainability, documentation
4. **Low**: Minor improvements, suggestions for future work

## See Also

- [Kotlin Development Standards](./kotlin.instructions.md)
- [Testing Guidelines](./testing.instructions.md)
- [Security Practices](./security.instructions.md)
- [AGENTS.md - Development Workflow](../../AGENTS.md)
