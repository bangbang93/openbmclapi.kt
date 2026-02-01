---
applyTo: "**/*.kt"
description: "Performance optimization guidelines for OpenBMCLAPI Kotlin Agent"
---

# Performance Optimization Guidelines

## Performance Philosophy

- **Measure First**: Profile before optimizing; avoid premature optimization
- **Focus on Critical Paths**: Prioritize optimization of frequently-executed code
- **Trade-offs**: Balance performance gains against code complexity
- **User Impact**: Consider how optimization affects user experience
- **Document Assumptions**: Document why optimization choices were made

## Memory Management

- **Object Allocation**: Be mindful of object creation in hot paths (loops, frequently-called functions)
- **Collection Sizing**: Initialize collections with appropriate capacity to avoid resizing
- **String Concatenation**: Use `StringBuilder` or string templates, not `+` concatenation in loops
- **Immutable Collections**: Prefer immutable collections which have better memory characteristics
- **Lazy Initialization**: Use `lazy` for expensive initialization that may not be needed
- **Object Pooling**: Consider object pooling for expensive-to-create objects (if significantly impactful)

## Collection Performance

- **Sequence for Multiple Operations**: Use `asSequence()` for chains of operations on large collections
  ```kotlin
  // Good: Single pass with lazy evaluation
  list.asSequence()
      .filter { it.isValid() }
      .map { it.process() }
      .toList()

  // Avoid: Multiple passes
  list.filter { it.isValid() }
      .map { it.process() }
  ```
- **Appropriate Collection Type**: Use `Set` for membership checks, `Map` for key-value lookups
- **Avoid Copying**: Minimize unnecessary collection copies
- **Stream Reuse**: Be aware of stream limitations (can only be consumed once)

## String Operations

- **buildString for Dynamic Strings**:
  ```kotlin
  // Good
  val result = buildString {
      append("prefix")
      append(value)
      append("suffix")
  }

  // Avoid
  val result = "prefix" + value + "suffix"
  ```
- **String Templates**: Prefer string interpolation over concatenation
  ```kotlin
  val message = "Found ${items.size} items"  // Good
  val message = "Found " + items.size + " items"  // Avoid
  ```
- **Regular Expressions**: Compile regex patterns as constants, not recreate in loops
  ```kotlin
  companion object {
      private val EMAIL_REGEX = Regex(""".+@.+\..+""")
  }
  ```

## Coroutine Performance

### Dispatcher Selection
- **Dispatchers.IO**: For I/O operations (network, file system, database)
- **Dispatchers.Default**: For CPU-intensive work (parsing, calculations)
- **Dispatchers.Main**: Only in UI contexts (not typical for this backend project)
- **Avoid Context Switching**: Unnecessary dispatcher switches have overhead

### Coroutine Scope Management
```kotlin
// Good: Structured concurrency
suspend fun processItems(items: List<Item>) = coroutineScope {
    items.map { item ->
        async { processItem(item) }
    }.awaitAll()
}

// Avoid: Launch without coordination
launch { processItem(item1) }
launch { processItem(item2) }
```

### Resource Cleanup
- Ensure resources are cleaned up in finally blocks
- Use proper cancellation to stop unnecessary work
- Monitor for resource leaks in coroutine scopes

## Database and Storage Performance

- **Batch Operations**: Perform batch operations instead of individual operations when possible
- **Connection Pooling**: Ensure connection pools are sized appropriately
- **Caching**: Implement caching for frequently-accessed data
- **Query Optimization**: Structure queries efficiently; avoid N+1 problems
- **Indexes**: Ensure database indexes exist for frequently-queried fields

## HTTP and Network Performance

- **Connection Pooling**: Reuse HTTP connections; don't create new clients for each request
- **Compression**: Enable compression for responses (HTTP Accept-Encoding)
- **Caching Headers**: Set appropriate cache headers for responses
- **Keep-Alive**: Ensure persistent connections are used
- **Timeout Configuration**: Set reasonable timeouts to avoid hanging connections

## Ktor-Specific Performance

- **Route Optimization**: Keep route handlers lightweight; move heavy work to services
- **Middleware Order**: Place frequent-use middleware early for faster execution
- **Content Negotiation**: Cache serialization configuration when possible
- **Async Handlers**: All Ktor handlers are suspend functions; use appropriately
- **Request Size Limits**: Set reasonable limits to prevent unbounded requests

## Caching Strategies

- **In-Memory Caching**: Use for frequently-accessed data that doesn't change often
- **Cache Invalidation**: Implement proper cache invalidation when data changes
- **TTL Configuration**: Set appropriate time-to-live for cached data
- **Cache Size Limits**: Prevent unbounded cache growth
- **Cache Warming**: Consider pre-loading cache for critical data

## File System Performance

- **Buffered I/O**: Use buffered streams for large file operations
- **Avoid Reading Entire Files**: Stream large files when possible
- **File Listing**: Avoid listing large directories if possible; use filtering
- **Atomic Operations**: Use atomic file operations where needed
- **File Descriptor Limits**: Monitor and ensure sufficient file descriptors

## Logging Performance

- **Lazy Evaluation**: Always use lambdas for log messages
  ```kotlin
  logger.info { "Processing ${items.size} items" }  // Good
  logger.info("Processing " + items.size + " items")  // Avoid: creates string even if log level disabled
  ```
- **Appropriate Log Levels**: Use DEBUG for verbose logging, INFO for important events
- **Conditional Logging**: Check log level before expensive operations
- **Async Logging**: Consider async logging for performance-critical paths

## Profiling and Monitoring

- **JVM Profiling**: Use JVM profiling tools to identify bottlenecks
- **Memory Profiling**: Monitor memory usage for memory leaks
- **GC Monitoring**: Monitor garbage collection impact
- **Metrics Collection**: Collect performance metrics (response times, throughput)
- **Alerting**: Set up alerts for performance degradation

## Common Performance Anti-patterns

- ❌ String concatenation in loops
- ❌ Creating new objects unnecessarily in hot paths
- ❌ N+1 query problems in loops
- ❌ Blocking operations on coroutine threads
- ❌ Creating regex patterns in loops
- ❌ Unnecessary collection copies
- ❌ Missing indexes on frequently-queried fields
- ❌ No connection pooling for external services
- ❌ Creating new HTTP clients for each request
- ❌ Inefficient serialization/deserialization

## Performance Testing

- **Load Testing**: Test under realistic load conditions
- **Stress Testing**: Test beyond normal load to find breaking points
- **Benchmarking**: Benchmark critical operations
- **Regression Testing**: Monitor for performance regressions
- **Real-World Data**: Test with realistic data sizes and characteristics

## Configuration for Performance

- **JVM Tuning**: Configure JVM heap size appropriately for workload
- **Thread Pools**: Configure thread pool sizes for workload
- **Buffer Sizes**: Configure buffer sizes for I/O operations
- **Connection Pool Size**: Configure database/HTTP connection pools appropriately
- **Cache Sizes**: Configure in-memory cache sizes based on available memory

## Documentation

- **Performance Decisions**: Document why performance-critical choices were made
- **Known Limitations**: Document known performance limitations
- **Scaling Guidance**: Document how the system scales with load
- **Configuration Tuning**: Document how to tune configuration for different workloads

## Performance Checklist

- [ ] Profile before optimizing critical paths
- [ ] Use Sequence for chains of collection operations
- [ ] Use buildString for dynamic strings
- [ ] Lazy log evaluation (lambdas)
- [ ] Appropriate dispatcher selection for coroutines
- [ ] Connection pooling for external services
- [ ] Cache configuration optimized
- [ ] No N+1 query problems
- [ ] Proper resource cleanup
- [ ] Monitoring and alerting in place

## See Also

- [Kotlin Sequence Documentation](https://kotlinlang.org/docs/sequences.html)
- [Coroutine Performance Guidelines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Ktor Performance](https://ktor.io/docs/server-performance.html)
- [JVM Tuning Guide](https://docs.oracle.com/en/java/javase/11/tools/java.html)
- [AGENTS.md - Performance Considerations](../../AGENTS.md)
