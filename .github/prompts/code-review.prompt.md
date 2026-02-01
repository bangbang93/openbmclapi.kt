---
agent: "agent"
model: "Claude Haiku"
tools: ["codebase"]
description: "Prepare code for GitHub pull request review"
---

# Code Review Preparation

Your task is to help prepare Kotlin code for a GitHub pull request review.

## Before You Start

Ask for these details if not provided:
1. **Changes Made**: What is the purpose of these changes? (feature, bug fix, refactoring, etc.)
2. **Code Files**: Which files were modified? (provide file paths)
3. **Related Issues**: Are there related GitHub issues? (provide issue numbers)
4. **Testing**: What testing was performed? (unit tests, integration tests, manual testing)

## Review Preparation Steps

### 1. Code Quality Assessment

Check against [Code Review Standards](../instructions/code-review.instructions.md):
- **Naming**: Do names follow conventions? (PascalCase, camelCase, SCREAMING_SNAKE_CASE)
- **Architecture**: Does code follow project structure and patterns?
- **Error Handling**: Is error handling appropriate with logging?
- **Null Safety**: Are null values handled properly?
- **Immutability**: Are objects immutable where appropriate?

### 2. Style Verification

Verify adherence to [Kotlin Development Standards](../instructions/kotlin.instructions.md):
- Format: 4-space indentation, 140-character line limit
- Imports: Organized correctly (stdlib → third-party → project)
- Type Annotations: Explicit for public APIs
- Logging: Using KotlinLogging with lazy evaluation
- Coroutines: Using suspend functions and proper scoping

### 3. Testing Assessment

Verify [Testing Guidelines](../instructions/testing.instructions.md):
- Are tests included for new functionality?
- Do tests follow Arrange-Act-Assert pattern?
- Are dependencies properly mocked?
- Is error handling tested?
- Is coverage adequate?

### 4. Security Check

Review [Security Best Practices](../instructions/security.instructions.md):
- No hardcoded secrets or credentials
- Input validation present
- Sensitive data not logged
- Dependencies are secure
- SSL/TLS properly configured

### 5. Performance Review

Check [Performance Guidelines](../instructions/performance.instructions.md):
- No obvious performance issues
- Appropriate use of collections/sequences
- Coroutines properly scoped
- Resource cleanup implemented
- Database queries optimized

### 6. Documentation Check

Verify [Documentation Standards](../instructions/documentation.instructions.md):
- Public APIs have KDoc comments
- Complex logic explained
- Configuration documented
- README updated if needed

## Pre-Review Checklist

Before submitting PR, verify:
- [ ] `./gradlew ktfmtCheck` passes (code style)
- [ ] `./gradlew build` completes successfully
- [ ] `./gradlew test` passes with meaningful coverage
- [ ] All tests pass locally
- [ ] No new warnings introduced
- [ ] Code follows all established patterns
- [ ] Tests verify new functionality
- [ ] No hardcoded secrets or credentials
- [ ] Documentation is updated
- [ ] Commit messages are clear

## Build Verification Commands

```bash
# Format code
./gradlew ktfmtFormat

# Check style
./gradlew ktfmtCheck

# Build and test
./gradlew build

# Run specific tests
./gradlew test --tests "TestClassName"

# Build summary
./gradlew buildFatJar
```

## PR Description Template

Provide a comprehensive PR description:

```markdown
## Description
[Brief description of changes]

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Refactoring
- [ ] Documentation update

## Related Issues
Fixes #[issue number]

## How to Test
[Steps to verify the changes]

## Testing Performed
- [ ] Unit tests added/updated
- [ ] Integration tests passed
- [ ] Manual testing performed
- [ ] All tests pass locally

## Checklist
- [ ] Code follows project style guidelines
- [ ] Tests added for new functionality
- [ ] Documentation updated
- [ ] No breaking changes
- [ ] Sensitive data not exposed
- [ ] Build verification passed
```

## Common Issues to Address

### Code Review Comments Pattern

```
[Category] Issue Description

Example:
[Style] This import should be ordered with other project imports
[Security] Consider validating input before using in query
[Performance] Using Sequence here would avoid creating intermediate list
[Testing] Add test case for error condition
```

## Review Categories

- **Architecture**: Structure, design patterns, separation of concerns
- **Security**: Input validation, credential handling, data protection
- **Performance**: Efficiency, memory usage, resource management
- **Style**: Formatting, naming, conventions
- **Testing**: Coverage, quality, meaningful assertions
- **Documentation**: Comments, KDoc, examples

## Next Steps After Review

1. **Address Comments**: Respond to and fix review comments
2. **Push Updates**: Commit changes and push to branch
3. **Request Re-review**: Mark conversation as resolved
4. **Merge**: Once approved, merge to main branch

## Reference Materials

- [Code Review Standards](../instructions/code-review.instructions.md)
- [Kotlin Development Standards](../instructions/kotlin.instructions.md)
- [Testing Guidelines](../instructions/testing.instructions.md)
- [AGENTS.md - Development Workflow](../../AGENTS.md)
