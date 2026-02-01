---
agent: "agent"
model: "Claude Haiku"
tools: ["codebase"]
description: "Refactor Kotlin code for maintainability and performance"
---

# Refactor Kotlin Code

Your task is to help refactor Kotlin code in the OpenBMCLAPI Agent project for better maintainability and performance.

## Before You Start

Ask for these details if not provided:
1. **Code Location**: Which file or method needs refactoring? (provide code snippet or file reference)
2. **Goals**: What's the goal? (improve readability, reduce duplication, improve performance, reduce complexity)
3. **Constraints**: Any constraints? (backward compatibility, specific patterns to follow)
4. **Context**: What does this code do? (brief description of functionality)

## Refactoring Analysis

### Identify Issues

Analyze the code for:
- **Duplication**: Repeated code patterns that could be extracted
- **Complexity**: Deeply nested logic that could be simplified
- **Readability**: Unclear variable names or logic flows
- **Performance**: Inefficient patterns or algorithms
- **Error Handling**: Missing or inconsistent error handling
- **Type Safety**: Unsafe null handling or type operations

### Refactoring Opportunities

Check for improvements from project standards:

**Code Structure:**
- Extract methods to improve readability
- Simplify long functions by breaking into smaller functions
- Use appropriate Kotlin idioms (scope functions, destructuring, etc.)
- Apply design patterns from [AGENTS.md](../../AGENTS.md)

**Performance:**
- Use Sequence for chained collection operations
- Avoid unnecessary object allocation
- Use lazy evaluation
- Reference [Performance Guidelines](../instructions/performance.instructions.md)

**Maintainability:**
- Improve variable and function names
- Extract magic numbers and strings to constants
- Add explanatory comments for non-obvious logic
- Follow [Kotlin Development Standards](../instructions/kotlin.instructions.md)

**Testing:**
- Ensure refactored code is testable
- Update tests to match new structure
- Add tests for newly extracted functions
- Verify no behavior changes

## Refactoring Steps

1. **Analyze Current Code**: Understand the original functionality and intent
2. **Identify Patterns**: Find repeatable patterns and extract them
3. **Apply Kotlin Idioms**: Use Kotlin features for conciseness
4. **Extract Functions**: Break large functions into focused units
5. **Improve Names**: Use clearer, more descriptive names
6. **Remove Duplication**: Consolidate repeated logic
7. **Enhance Error Handling**: Add proper exception handling and logging
8. **Verify Behavior**: Ensure refactored code has same behavior
9. **Update Tests**: Adjust tests for new structure
10. **Performance Check**: Verify no performance regression

## Common Refactoring Patterns

### Extract Method
```kotlin
// Before
fun process(items: List<Item>) {
    for (item in items) {
        if (item.isValid()) {
            // complex validation logic here
            // many lines of validation
            val result = item.validate()
            if (result.isSuccessful) {
                // complex processing logic here
                // many lines of processing
            }
        }
    }
}

// After
fun process(items: List<Item>) {
    for (item in items) {
        processItem(item)
    }
}

private fun processItem(item: Item) {
    if (item.isValid()) {
        val result = item.validate()
        if (result.isSuccessful) {
            performProcessing(item)
        }
    }
}
```

### Use Sequences for Collections
```kotlin
// Before
val results = list.filter { it.isValid() }
    .map { it.process() }
    .filter { it.success }

// After
val results = list.asSequence()
    .filter { it.isValid() }
    .map { it.process() }
    .filter { it.success }
    .toList()
```

### Use Kotlin Scope Functions
```kotlin
// Before
if (item != null) {
    val processed = item.process()
    logger.info { "Processed: ${processed.name}" }
    cache.put(processed.id, processed)
    return processed
}

// After
return item?.let { item ->
    item.process().also { processed ->
        logger.info { "Processed: ${processed.name}" }
        cache.put(processed.id, processed)
    }
}
```

### Simplify Nested Conditionals
```kotlin
// Before
fun validate(user: User): Boolean {
    if (user.isActive) {
        if (user.hasPermission("admin")) {
            if (user.passwordValid) {
                return true
            }
        }
    }
    return false
}

// After
fun validate(user: User): Boolean {
    return user.isActive &&
           user.hasPermission("admin") &&
           user.passwordValid
}
```

## Refactoring Checklist

Before completing refactoring:
- [ ] Original functionality preserved (tests pass)
- [ ] Code is more readable and maintainable
- [ ] Follows project patterns and conventions
- [ ] Performance not degraded
- [ ] All tests still pass
- [ ] Documentation updated if needed
- [ ] No new code smells introduced

## Output Format

Provide:
1. **Before**: Original code with issues highlighted
2. **After**: Refactored code with improvements
3. **Explanation**: Why this refactoring improves the code
4. **Migration**: Steps to update related code/tests
5. **Verification**: How to verify the refactoring is successful

## Performance Impact

Always verify and report:
- [ ] Performance impact (improved, neutral, or degraded)
- [ ] Memory usage impact if applicable
- [ ] Build/compile time impact if applicable
- [ ] Any trade-offs made

## See Also

- [Kotlin Development Standards](../instructions/kotlin.instructions.md)
- [Performance Guidelines](../instructions/performance.instructions.md)
- [Code Review Standards](../instructions/code-review.instructions.md)
- [AGENTS.md - Code Style Guidelines](../../AGENTS.md)
