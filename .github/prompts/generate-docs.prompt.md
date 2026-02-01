---
agent: "agent"
model: "Claude Haiku"
tools: ["codebase", "web/fetch"]
description: "Generate documentation for Kotlin code"
---

# Generate Documentation

Your task is to generate comprehensive documentation for Kotlin code in the OpenBMCLAPI Agent project.

## Before You Start

Ask for these details if not provided:
1. **Documentation Type**: What needs documentation? (API docs, configuration guide, architecture overview, deployment guide, etc.)
2. **Scope**: What's the scope? (single class, module, entire system, configuration)
3. **Audience**: Who is the audience? (developers, DevOps, end users, etc.)
4. **Existing Docs**: Are there existing documentation to build upon?

## Documentation Types

### 1. KDoc for Code

Generate KDoc comments for:
- **Classes**: Description, key methods, usage examples
- **Functions**: Parameters, return values, exceptions, examples
- **Properties**: Type, purpose, constraints
- **Interfaces**: Contract description, implementing classes

Format:
```kotlin
/**
 * Brief description of what this does.
 *
 * More detailed description explaining the purpose, behavior,
 * and important details about this component.
 *
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception is thrown
 *
 * @example
 * ```kotlin
 * val result = method(value)
 * ```
 */
```

### 2. README Documentation

Create comprehensive README with:
- **Project Overview**: What this project does
- **Quick Start**: Steps to get running
- **Build Commands**: Key commands from [AGENTS.md](../../AGENTS.md)
- **Project Structure**: Overview of directory organization
- **Configuration**: How to configure
- **Development**: Development workflow
- **Testing**: How to run tests
- **Troubleshooting**: Common issues
- **Contributing**: Contribution guidelines

### 3. API Documentation

Document HTTP endpoints:
- **Endpoint URL**: HTTP method and path
- **Description**: What the endpoint does
- **Parameters**: Query, path, and body parameters
- **Authentication**: Auth requirements
- **Request/Response**: Example payloads with types
- **Status Codes**: Possible HTTP responses
- **Errors**: Error response formats

Example:
```markdown
## GET /api/status

Get the current status of this cluster node.

**Parameters:**
- `include_metrics` (query, optional): Include performance metrics

**Response (200 OK):**
```json
{
  "status": "healthy",
  "clusterId": "node-1",
  "uptime": 3600000
}
```

**Status Codes:**
- 200 OK - Successfully retrieved status
- 503 Service Unavailable - Service not ready
```

### 4. Configuration Guide

Document configuration:
- **All Configuration Options**: List all configurable settings
- **Defaults**: Show default values
- **Environment Variables**: Map configuration to env vars
- **Examples**: Example configurations
- **Security Notes**: Warnings for sensitive settings

Format:
```markdown
## Configuration

### cluster.id
- Type: String
- Required: Yes
- Default: (none)
- Environment Variable: CLUSTER_ID
- Description: Unique identifier for this cluster node
```

### 5. Architecture Documentation

Document system design:
- **Overview**: High-level system description
- **Components**: Major components and responsibilities
- **Interactions**: How components communicate
- **Patterns**: Architectural patterns used
- **Layers**: Request flow through layers (routes → services → storage)

### 6. Troubleshooting Guide

Document common issues:
- **Problem Description**: Clear description of the problem
- **Symptoms**: How to recognize this problem
- **Root Causes**: What typically causes this
- **Solutions**: Steps to resolve
- **Prevention**: How to prevent in future

## Documentation Standards

### Code Examples
- **Working Code**: All examples must be correct and runnable
- **Realistic**: Use realistic scenarios
- **Formatted**: Properly formatted and indented
- **Explained**: Include comments explaining the example

### Accessibility
- **Clear Language**: Use simple, clear language
- **Context**: Provide necessary background
- **Links**: Link to related documentation
- **Searchability**: Use consistent terminology

### Maintenance
- **Accuracy**: Keep documentation current
- **Links**: Update links when files move
- **Examples**: Update examples when code changes
- **Version**: Note which version the docs apply to

## Generation Steps

1. **Analyze Code**: Understand the code's purpose and behavior
2. **Identify Patterns**: Note common patterns and usage
3. **Gather Examples**: Collect real usage examples
4. **Structure Content**: Organize information logically
5. **Write Clearly**: Use clear, accessible language
6. **Include Examples**: Provide working code examples
7. **Link Related Docs**: Reference related documentation
8. **Review**: Check for accuracy and clarity

## Output Formats

Provide documentation in appropriate format:
- **KDoc**: For code documentation (in-code comments)
- **Markdown**: For README, guides, API docs
- **YAML**: For configuration examples
- **Code Blocks**: For examples (Kotlin, JSON, YAML)

## Documentation Checklist

- [ ] Clear and accurate descriptions
- [ ] All parameters/options documented
- [ ] Working code examples included
- [ ] Related documentation linked
- [ ] No broken links
- [ ] Consistent terminology
- [ ] Appropriate detail level for audience
- [ ] Examples are tested and accurate
- [ ] Security notes included where relevant
- [ ] Updated if code changes

## Reference Materials

- [Documentation Standards](../instructions/documentation.instructions.md)
- [Kotlin Documentation Guide](https://kotlinlang.org/docs/documenting-code.html)
- [AGENTS.md - Project Overview](../../AGENTS.md)
- [Project README](../../README.md)

## See Also

- [Code Review Standards](../instructions/code-review.instructions.md)
- [Kotlin Development Standards](../instructions/kotlin.instructions.md)
