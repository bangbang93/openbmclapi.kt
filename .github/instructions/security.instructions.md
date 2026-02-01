---
applyTo: "**/*.kt"
description: "Security best practices for OpenBMCLAPI Kotlin Agent"
---

# Security Best Practices

## Sensitive Data Management

- **Never Commit Secrets**: Keep API keys, tokens, and passwords out of version control
- **Use Environment Variables**: Store sensitive configuration in `.env` files (gitignored) or secure vaults
- **Configuration Files**: Mark `application.yaml` and similar config files with sensible defaults only
- **Secrets in CI/CD**: Use GitHub Secrets or similar for CI/CD pipelines
- **Credential Rotation**: Implement mechanisms to rotate credentials without code changes

## Credential Handling

- **No Hardcoded Values**: Avoid any hardcoded passwords, API keys, or tokens
- **Logging Consideration**: Never log credentials, tokens, or sensitive user data
- **Password Validation**: Validate passwords/credentials securely without exposing details
- **Credential Storage**: Use `.env.example` as template; document required credentials
- **Secret Scope**: Limit credential scope to minimum necessary permissions

## Input Validation

- **Validate Early**: Check all external input (HTTP requests, file uploads, user input) immediately
- **Type Validation**: Verify data types and formats match expectations
- **Size Limits**: Enforce reasonable limits on string lengths and file sizes
- **Format Validation**: Use regex or validators for specific formats (URLs, emails, etc.)
- **Reject Invalid Input**: Return clear error messages without revealing system details

## SSL/TLS Security

- **Certificate Management**:
  - Use BYOC (Bring Your Own Certificate) mode for custom certificates
  - Store certificate files securely, not in version control
  - Keep certificate paths in environment configuration
  - Validate certificate expiration dates
- **Strong Ciphers**: Ensure Ktor configuration uses modern, strong TLS ciphersuites
- **Certificate Validation**: Validate certificates in client connections
- **HTTPS Enforcement**: Enforce HTTPS/TLS for all cluster communication

## Secure Communication

- **Cluster Authentication**: Verify cluster credentials (ID and secret) on every connection
- **Token Validation**: Implement secure token generation and validation
- **Message Integrity**: Consider message signing or HMAC for cluster communication
- **Rate Limiting**: Implement rate limiting to prevent abuse
- **CORS Configuration**: Configure CORS appropriately; don't allow `*` in production

## Dependency Security

- **Regular Updates**: Keep Kotlin, Ktor, Koin, and all dependencies up to date
- **Security Advisories**: Monitor for security updates to dependencies
- **Dependency Auditing**: Use tools like OWASP Dependency Check (via Gradle plugins if available)
- **Transitive Dependencies**: Be aware of transitive dependencies; understand your full dependency tree
- **Remove Unused Dependencies**: Keep dependency list minimal to reduce attack surface

## Data Protection

- **Minimal Data Collection**: Only collect and store necessary data
- **Data Encryption**: Encrypt sensitive data at rest if stored
- **Access Control**: Implement proper authentication and authorization
- **Data Deletion**: Implement secure deletion of sensitive data when no longer needed
- **Audit Logging**: Log access to sensitive data for audit trails

## File System Security

- **Path Validation**: Validate all file paths; prevent directory traversal attacks
- **File Permissions**: Ensure storage directories have appropriate permissions
- **Secure Temporary Files**: Use secure temporary file creation with proper cleanup
- **File Size Limits**: Enforce limits on uploaded file sizes
- **File Type Validation**: Validate file types match expectations

## Network Security

- **Port Configuration**: Use non-standard ports when appropriate; document port usage
- **Firewall Rules**: Document required firewall rules for cluster nodes
- **NAT/UPnP Caution**: When using UPnP for NAT traversal, verify no unintended exposure
- **IP Whitelisting**: Consider whitelisting authorized cluster nodes if possible
- **Connection Timeouts**: Implement reasonable timeouts to prevent resource exhaustion

## Error Messages and Logging

- **Generic Error Messages**: Return generic errors to users; log detailed errors server-side
- **Stack Trace Hiding**: Don't expose stack traces to users in error responses
- **Sensitive Data in Logs**: Never log passwords, tokens, API keys, or personal information
- **Structured Logging**: Use structured logs for security events (auth failures, unusual access)
- **Log Retention**: Implement appropriate log retention policies

## Access Control

- **Principle of Least Privilege**: Grant minimal necessary permissions to services and users
- **Role-Based Access**: Implement role-based access control when applicable
- **Authentication Verification**: Verify cluster authentication on every request
- **Authorization Checks**: Perform authorization checks for sensitive operations
- **Session Management**: If implementing sessions, use secure practices (secure cookies, etc.)

## Code Security Practices

- **Type Safety**: Use Kotlin's type system to prevent null-pointer exceptions
- **Resource Management**: Always clean up resources (files, connections, streams)
- **Avoid Reflection**: Limit use of reflection; it's harder to audit for security
- **Serialization Safety**: Validate serialized data; be cautious with untrusted input
- **Cryptographic Functions**: Use established libraries (BouncyCastle, JDK built-ins)

## Security Testing

- **Input Fuzzing**: Test with malformed, oversized, or malicious input
- **Boundary Testing**: Test edge cases and boundary conditions
- **Authentication Testing**: Verify authentication is properly enforced
- **Authorization Testing**: Verify only authorized parties can access resources
- **Dependency Testing**: Scan dependencies for known vulnerabilities

## Incident Response

- **Error Monitoring**: Monitor for unusual errors that might indicate attacks
- **Rate Limiting Alerts**: Alert when rate limits are triggered repeatedly
- **Authentication Failures**: Log and monitor authentication failures
- **Resource Exhaustion**: Monitor for unusual resource usage patterns
- **Incident Plan**: Document incident response procedures

## OWASP Top 10 Considerations

- **A01 Broken Access Control**: Implement proper authorization checks
- **A02 Cryptographic Failures**: Use secure encryption and SSL/TLS
- **A03 Injection**: Use parameterized queries and proper input validation
- **A04 Insecure Design**: Follow secure design principles from the start
- **A05 Security Misconfiguration**: Use secure defaults; document configuration
- **A06 Vulnerable Components**: Keep dependencies updated
- **A07 Auth Failures**: Implement secure authentication mechanisms
- **A08 Software/Data Integrity**: Verify code integrity; use secure CI/CD
- **A09 Logging/Monitoring Failures**: Implement comprehensive security logging
- **A10 Server-Side Request Forgery**: Validate external requests; implement proper validation

## Security Checklist

- [ ] No hardcoded secrets in code
- [ ] All input is validated and sanitized
- [ ] SSL/TLS properly configured
- [ ] Sensitive data not logged
- [ ] Dependencies are current and secure
- [ ] Authentication/authorization properly implemented
- [ ] File system access properly validated
- [ ] Error messages don't expose sensitive information
- [ ] Security headers configured (if applicable)
- [ ] Rate limiting/DDoS protection implemented

## Resources and References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Kotlin Security](https://kotlinlang.org/docs/reference/null-safety.html)
- [Ktor Security](https://ktor.io/docs/security.html)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [CWE Top 25](https://cwe.mitre.org/top25/)

## See Also

- [Kotlin Development Standards](./kotlin.instructions.md)
- [Code Review Standards](./code-review.instructions.md)
- [AGENTS.md - Environment Setup](../../AGENTS.md)
