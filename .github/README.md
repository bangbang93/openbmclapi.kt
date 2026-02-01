# GitHub Copilot Setup - Quick Reference

## Configuration Overview

This directory contains comprehensive GitHub Copilot configuration for the OpenBMCLAPI Kotlin Agent project.

## Directory Structure

```
.github/
├── copilot-instructions.md       # Main repository-wide standards
├── instructions/                 # Detailed coding standards
│   ├── kotlin.instructions.md
│   ├── testing.instructions.md
│   ├── code-review.instructions.md
│   ├── security.instructions.md
│   ├── performance.instructions.md
│   └── documentation.instructions.md
├── prompts/                      # Reusable prompts for common tasks
│   ├── setup-component.prompt.md       # Generate new services
│   ├── write-tests.prompt.md           # Generate tests
│   ├── code-review.prompt.md           # Prepare for code review
│   ├── refactor-code.prompt.md         # Refactor code
│   ├── generate-docs.prompt.md         # Generate documentation
│   └── debug-issue.prompt.md           # Debug issues
├── agents/                       # Specialized chat modes
│   ├── architect.agent.md        # Architecture planning mode
│   ├── reviewer.agent.md         # Code review expert mode
│   └── debugger.agent.md         # Debugging expert mode
└── workflows/
    └── copilot-setup-steps.yml   # GitHub Actions workflow
```

## Using Copilot with This Project

### 1. Follow Main Standards
Start by reading [copilot-instructions.md](copilot-instructions.md) for project-wide standards.

### 2. Use Specialized Instructions

**For writing Kotlin code:**
- Reference [kotlin.instructions.md](instructions/kotlin.instructions.md)
- Covers naming, type safety, error handling, logging, DI

**For writing tests:**
- Reference [testing.instructions.md](instructions/testing.instructions.md)
- Covers test structure, mocking, patterns, coverage

**For security:**
- Reference [security.instructions.md](instructions/security.instructions.md)
- Covers credential handling, input validation, SSL/TLS

**For performance:**
- Reference [performance.instructions.md](instructions/performance.instructions.md)
- Covers memory, collections, coroutines, profiling

**For code reviews:**
- Reference [code-review.instructions.md](instructions/code-review.instructions.md)
- Covers review standards and best practices

**For documentation:**
- Reference [documentation.instructions.md](instructions/documentation.instructions.md)
- Covers KDoc, README, API docs, configuration

### 3. Use Reusable Prompts

**To generate a new service:**
Reference [setup-component.prompt.md](prompts/setup-component.prompt.md)
- Ask Copilot to generate a Kotlin service class
- Includes dependency injection, error handling, logging

**To write tests:**
Reference [write-tests.prompt.md](prompts/write-tests.prompt.md)
- Ask Copilot to generate comprehensive tests
- Includes mocking, error cases, integration tests

**To prepare for code review:**
Reference [code-review.prompt.md](prompts/code-review.prompt.md)
- Ask Copilot to help prepare code for review
- Includes verification checklist, build commands

**To refactor code:**
Reference [refactor-code.prompt.md](prompts/refactor-code.prompt.md)
- Ask Copilot to suggest refactoring improvements
- Includes common patterns and anti-patterns

**To generate documentation:**
Reference [generate-docs.prompt.md](prompts/generate-docs.prompt.md)
- Ask Copilot to generate KDoc, README, API docs
- Includes examples and best practices

**To debug issues:**
Reference [debug-issue.prompt.md](prompts/debug-issue.prompt.md)
- Ask Copilot to help debug problems
- Includes methodology, common issues, tools

### 4. Use Specialized Chat Modes

**For architecture planning:**
Use [architect.agent.md](agents/architect.agent.md)
- Plan new features and refactoring at architectural level
- Generate implementation plans with detailed steps

**For code review:**
Use [reviewer.agent.md](agents/reviewer.agent.md)
- Get expert-level code review
- Check against standards and best practices

**For debugging:**
Use [debugger.agent.md](agents/debugger.agent.md)
- Debug issues systematically
- Follow methodology to find root causes

## Quick Start

### Setting Up Copilot in VS Code

1. Install GitHub Copilot extension from VS Code marketplace
2. Sign in with your GitHub account
3. These configuration files will be automatically recognized
4. Start using Copilot with `Ctrl+I` (inline chat) or `Ctrl+Shift+I`

### Example Workflows

**Creating a new service:**
1. Reference [Kotlin Development Standards](instructions/kotlin.instructions.md)
2. Use [setup-component.prompt.md](prompts/setup-component.prompt.md)
3. Ask Copilot: "Generate a new Kotlin service for [purpose]"

**Writing tests for existing code:**
1. Reference [Testing Guidelines](instructions/testing.instructions.md)
2. Use [write-tests.prompt.md](prompts/write-tests.prompt.md)
3. Ask Copilot: "Write comprehensive tests for [code]"

**Preparing a PR:**
1. Reference [Code Review Standards](instructions/code-review.instructions.md)
2. Use [code-review.prompt.md](prompts/code-review.prompt.md)
3. Ask Copilot: "Help me prepare this PR for review"

**Planning a new feature:**
1. Use architect mode with [architect.agent.md](agents/architect.agent.md)
2. Ask Copilot: "Plan architecture for [feature]"
3. Get detailed implementation plan

## Key Resources

- [AGENTS.md](../../AGENTS.md) - Project development guide with build commands
- [Project Structure](instructions/kotlin.instructions.md#project-structure) - Directory organization
- [Build Commands](../../AGENTS.md#build-commands) - How to build and test

## Code Style Quick Reference

- **Indentation**: 4 spaces
- **Line Length**: 140 characters (Kotlin)
- **Naming**: PascalCase (classes), camelCase (functions/properties), SCREAMING_SNAKE_CASE (constants)
- **Type Annotations**: Explicit for public APIs
- **Logging**: Use KotlinLogging with lambdas
- **Error Handling**: Try-catch with logging
- **Testing**: Arrange-Act-Assert pattern

## Build Commands

```bash
# Build and test
./gradlew build

# Format code
./gradlew ktfmtFormat

# Check style
./gradlew ktfmtCheck

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests "TestClassName"
```

## Common Copilot Requests

- "Generate a new Kotlin service with dependency injection"
- "Write comprehensive tests for this code"
- "Help me refactor this for better performance"
- "Check this code for security issues"
- "Generate KDoc comments for this class"
- "Plan the architecture for this feature"
- "Debug this issue following the methodology"

## Standards Reference

All standards reference these core principles:
1. **Kotlin-First**: Leverage Kotlin idioms
2. **Type Safe**: Maximize compile-time safety
3. **Async-Ready**: Use suspend functions and coroutines
4. **Well-Tested**: Comprehensive test coverage
5. **Well-Documented**: Clear documentation and comments
6. **Secure by Default**: Security best practices
7. **Performance-Conscious**: Efficient implementations

## Need Help?

- Review [copilot-instructions.md](copilot-instructions.md) for overview
- Check specific instruction file for your task
- Use relevant prompt for guided generation
- Try specialized agent mode for complex tasks

## Contributing

When updating these files:
1. Keep standards current with actual project practices
2. Sync with AGENTS.md when changing patterns
3. Update examples to match latest code style
4. Maintain consistency across all files
5. Test commands to ensure they still work

---

Last Updated: 2026-02-01
Project: OpenBMCLAPI Kotlin Agent
Framework: Ktor 3.3.1 | Language: Kotlin 2.2.20
