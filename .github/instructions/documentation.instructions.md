---
applyTo: "**/*.md,**/*.kt"
description: "Documentation standards for OpenBMCLAPI Kotlin Agent"
---

# Documentation Standards

## Code Documentation

### KDoc Comments
- **Document Public APIs**: All public classes, functions, and properties should have KDoc
- **Format**: Use proper KDoc format with `/**` and `*/`
- **Description**: Start with a brief one-line description
- **Parameters**: Document all parameters with `@param` tags
- **Return Value**: Document return value with `@return` tag
- **Exceptions**: Document exceptions with `@throws` tag
- **Examples**: Include usage examples for complex functions

```kotlin
/**
 * Processes a batch of items and returns successful results.
 *
 * This function handles item validation and processing in a coroutine scope,
 * ensuring all items are processed in parallel.
 *
 * @param items List of items to process
 * @param batchSize Maximum number of items to process in parallel (default: 10)
 * @return List of successfully processed items
 * @throws IllegalArgumentException if items list is empty
 * @throws ProcessingException if critical processing error occurs
 *
 * @example
 * ```kotlin
 * val results = processor.processBatch(items, batchSize = 5)
 * ```
 */
suspend fun processBatch(items: List<Item>, batchSize: Int = 10): List<ProcessedItem>
```

### Inline Comments
- **Explain "Why" Not "What"**: Comments should explain design decisions, not restate code
- **Complex Logic**: Comments for non-obvious algorithms or business logic
- **Workarounds**: Document workarounds for bugs or limitations
- **Keep Current**: Update comments when code changes

```kotlin
// Good: Explains why
// Use Sequence to avoid creating intermediate lists for large collections
val results = items.asSequence()
    .filter { it.isValid() }
    .map { it.process() }
    .toList()

// Avoid: Restates what code does
// Filter items and map them to processed results
val results = items.asSequence()
    .filter { it.isValid() }
    .map { it.process() }
    .toList()
```

## API Documentation

### REST Endpoints
- **Endpoint Description**: Document what each endpoint does
- **Parameters**: Document path, query, and body parameters
- **Response Codes**: Document expected HTTP status codes
- **Response Format**: Describe response body structure
- **Authentication**: Document authentication requirements
- **Rate Limits**: Document rate limiting if applicable

Example format for Ktor route documentation:
```kotlin
/**
 * GET /api/status - Get cluster node status
 *
 * Returns the current status of this cluster node including health information
 * and configuration details.
 *
 * Query Parameters:
 * - include_stats: boolean (optional) - Include performance statistics
 *
 * Response (200 OK):
 * ```json
 * {
 *   "status": "healthy",
 *   "clusterId": "node-123",
 *   "uptime": 3600000,
 *   "version": "1.0.0"
 * }
 * ```
 *
 * Response (503 Service Unavailable):
 * If the service is not ready or has encountered errors.
 */
```

## Configuration Documentation

### Configuration Files
- **Document All Options**: Document all configurable parameters
- **Include Defaults**: Show default values for each option
- **Provide Examples**: Include example configuration values
- **Mark Required vs Optional**: Clearly indicate which settings are required
- **Security Warnings**: Warn about sensitive configuration

Example:
```yaml
# Cluster Configuration
# All configuration can be provided via environment variables or .env file

cluster:
  # REQUIRED: Unique identifier for this cluster node
  # Environment: CLUSTER_ID
  id: "node-1"

  # REQUIRED: Secret key for cluster authentication
  # Environment: CLUSTER_SECRET
  # WARNING: Never commit this value; use environment variables
  secret: "${CLUSTER_SECRET}"

  # OPTIONAL: Public IP address (auto-detected if not provided)
  # Environment: CLUSTER_IP
  ip: "192.168.1.100"

  # OPTIONAL: Server port (default: 4000)
  # Environment: CLUSTER_PORT
  port: 4000
```

## README Documentation

### Project README Structure
1. **Project Description**: Brief overview of the project
2. **Quick Start**: Steps to get running locally
3. **Build Commands**: Common build and test commands
4. **Project Structure**: Overview of directory organization
5. **Configuration**: How to configure the application
6. **Development**: Development workflow and guidelines
7. **Testing**: How to run tests
8. **Troubleshooting**: Common issues and solutions
9. **Contributing**: Contribution guidelines
10. **License**: License information

## AGENTS.md Documentation

- **Keep Current**: Update AGENTS.md when development patterns change
- **Build Commands**: Ensure all build commands are accurate and tested
- **Code Style**: Update code style guidelines when standards change
- **Dependencies**: Keep technology stack section up to date
- **Examples**: Provide working code examples for important patterns

## Environment Documentation

### .env.example File
- **Template File**: Create `.env.example` with all configurable environment variables
- **Include Descriptions**: Add comments explaining each variable
- **Show Defaults**: Indicate default values where applicable
- **Mark Required**: Clearly mark required vs optional variables
- **Security Notes**: Add warnings for sensitive variables

Example:
```bash
# OpenBMCLAPI Cluster Configuration
# Copy this file to .env and fill in required values

# REQUIRED - Unique cluster node identifier
CLUSTER_ID=my-cluster-node

# REQUIRED - Secret key for cluster authentication
# Never commit this value to version control
CLUSTER_SECRET=your-secret-key-here

# OPTIONAL - Public IP address (auto-detected if not provided)
CLUSTER_IP=192.168.1.100

# OPTIONAL - Server port (default: 4000)
CLUSTER_PORT=4000
```

## Change Documentation

### Commit Messages
- **Clear Summary**: First line summarizes the change (50 characters or less)
- **Detailed Description**: Additional details after blank line
- **References Issues**: Link to related issues: "Fixes #123"
- **Breaking Changes**: Clearly note breaking changes

Example:
```
Add cluster health check endpoint

Implement GET /api/health endpoint that returns cluster node status
and health metrics. Include uptime, version information, and
performance statistics.

Fixes #456
```

### Pull Request Descriptions
- **What**: Describe what was changed and why
- **How**: Explain how the change works
- **Testing**: Describe testing performed
- **Checklist**: Include checklist of review requirements
- **Screenshots/Examples**: Include if applicable

## Documentation Tools

- **KDoc Generator**: Gradle task to generate HTML documentation
- **Markdown Linting**: Consider markdown linting for consistency
- **Automated Deployment**: Document how to deploy generated documentation

## Maintenance

### Documentation Review
- Review documentation in code reviews
- Update documentation when code changes
- Remove outdated documentation
- Fix broken links and references

### Version Documentation
- Tag releases in git with version numbers
- Maintain changelog documenting changes
- Document breaking changes prominently
- Archive old documentation if needed

## Best Practices

- **DRY (Don't Repeat Yourself)**: Reference documented information rather than duplicating
- **Keep Current**: Outdated documentation is worse than no documentation
- **Clarity Over Cleverness**: Write clearly; assume reader is unfamiliar with code
- **Audience Awareness**: Write for both new developers and experienced team members
- **Searchability**: Use consistent terminology to help with searching
- **Linking**: Link related documentation together
- **Examples**: Provide working examples whenever possible

## Documentation Checklist

Before submitting PR:
- [ ] Public APIs have KDoc comments
- [ ] Complex logic has explanatory comments
- [ ] Configuration options are documented
- [ ] README is up to date
- [ ] Examples are working and accurate
- [ ] Links are not broken
- [ ] No outdated information remains
- [ ] Security-sensitive information not exposed

## See Also

- [AGENTS.md - Project Overview](../../AGENTS.md)
- [Kotlin Documentation Guide](https://kotlinlang.org/docs/documenting-code.html)
- [Ktor Documentation](https://ktor.io/docs/)
