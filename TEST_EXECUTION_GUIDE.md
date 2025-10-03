# Test Execution and CI/CD Integration Guide

## Overview

This document provides comprehensive guidance on test execution, monitoring, and CI/CD integration for the Pokemon
Trading Card Game API project.

## Test Structure

### Test Types

1. **Unit Tests** (`src/test/kotlin`)
    - Fast, isolated tests for individual components
    - Mock external dependencies
    - Target coverage: 80%

2. **Integration Tests** (`src/integrationTest/kotlin`)
    - End-to-end testing with real database
    - TestContainers for PostgreSQL
    - Target coverage: 85%

### Test Profiles

- `test` - Unit test profile with H2 in-memory database
- `integration-test` - Integration test profile with TestContainers PostgreSQL

## Gradle Tasks

### Basic Test Execution

```bash
# Run unit tests only
./gradlew test

# Run integration tests only
./gradlew integrationTest

# Run all tests
./gradlew allTests

# Run fast tests (unit tests only)
./gradlew fastTests

# Run tests in CI mode
./gradlew ciTests
```

### Coverage Reports

```bash
# Generate unit test coverage report
./gradlew jacocoTestReport

# Generate integration test coverage report
./gradlew jacocoIntegrationTestReport

# Generate combined coverage report
./gradlew jacocoFullReport

# Verify coverage thresholds
./gradlew jacocoTestCoverageVerification
```

### Test Reporting

```bash
# Generate test performance report
./gradlew testPerformanceReport

# Generate comprehensive test summary
./gradlew testSummaryReport
```

## Test Configuration

### Environment Variables

```bash
# Enable parallel execution
export JUNIT_JUPITER_EXECUTION_PARALLEL_ENABLED=true

# TestContainers reuse
export TESTCONTAINERS_REUSE_ENABLE=true

# JVM options for tests
export GRADLE_OPTS="-Xmx4g -XX:+UseG1GC"
```

### System Properties

```bash
# Run tests with specific profile
./gradlew test -Dspring.profiles.active=test

# Enable debug logging
./gradlew integrationTest -Dlogging.level.br.com.tcg.pokemon=DEBUG

# Set performance monitoring threshold
./gradlew integrationTest -Dtest.performance.threshold=5000
```

## Performance Monitoring

### Automatic Monitoring

Tests are automatically monitored using the `TestMonitoringExtension`. Key metrics include:

- Test execution duration
- Memory usage
- Database operation performance
- HTTP request timing

### Manual Monitoring

Use the `@MonitorPerformance` annotation for specific tests:

```kotlin
@MonitorPerformance(threshold = 3000, trackMemory = true)
@Test
fun `should complete within performance threshold`() {
    // Test implementation
}
```

### Performance Reports

Performance data is logged to:

- Console output during test execution
- `build/logs/test-performance.log`
- `build/reports/test-performance.txt`

## CI/CD Integration

### GitHub Actions Workflow

The project includes a comprehensive CI/CD pipeline (`.github/workflows/ci.yml`) with:

1. **Test Job**
    - Runs unit and integration tests
    - Generates coverage reports
    - Uploads test artifacts

2. **Build Job**
    - Builds application JAR
    - Creates Docker image
    - Uploads build artifacts

3. **Quality Job**
    - SonarQube analysis
    - Coverage verification
    - Code quality checks

### Pipeline Triggers

- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual workflow dispatch

### Required Secrets

Configure these secrets in your GitHub repository:

```bash
SONAR_TOKEN=your_sonar_token
CODECOV_TOKEN=your_codecov_token  # Optional
```

## Test Data Management

### TestContainers Configuration

Integration tests use TestContainers with PostgreSQL:

```yaml
# src/integrationTest/resources/application-integration-test.yml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
```

### Test Data Builders

Use the provided test data builders for consistent test data:

```kotlin
val serie = SerieTestDataBuilder()
    .withCode("TEST01")
    .withName("Test Serie")
    .withReleaseYear(2024)
    .buildEntity()
```

### Database Cleanup

- Automatic cleanup after each test method
- `@Transactional` with rollback for data isolation
- Container reuse for performance optimization

## Troubleshooting

### Common Issues

1. **TestContainers fails to start**
   ```bash
   # Ensure Docker is running
   docker --version
   
   # Check available memory
   docker system info
   ```

2. **Tests run slowly**
   ```bash
   # Increase JVM memory
   export GRADLE_OPTS="-Xmx4g"
   
   # Enable container reuse
   export TESTCONTAINERS_REUSE_ENABLE=true
   ```

3. **Coverage verification fails**
   ```bash
   # Check current coverage
   ./gradlew jacocoTestReport
   
   # View HTML report
   open build/reports/jacoco/test/html/index.html
   ```

### Performance Optimization

1. **Parallel Execution**
    - Unit tests run in parallel by default
    - Integration tests run sequentially to avoid resource conflicts

2. **Container Reuse**
    - Enable TestContainers reuse for faster test startup
    - Containers are shared across test runs

3. **Memory Management**
    - Optimized JVM settings for test execution
    - G1GC for better garbage collection performance

## Monitoring and Alerts

### Test Metrics

The following metrics are automatically collected:

- Test execution time
- Memory usage per test
- Database operation duration
- HTTP request/response timing
- Test success/failure rates

### Performance Thresholds

Default performance thresholds:

- Unit tests: < 1000ms
- Integration tests: < 5000ms
- Database operations: < 2000ms
- HTTP requests: < 3000ms

### Alerting

Configure alerts for:

- Test failure rate > 5%
- Coverage drop > 2%
- Performance degradation > 20%
- Build time increase > 30%

## Best Practices

### Test Organization

1. **Naming Conventions**
    - Use descriptive test names in English
    - Follow `should_expectedBehavior_when_stateUnderTest` pattern

2. **Test Structure**
    - Follow AAA pattern (Arrange, Act, Assert)
    - One assertion per test method
    - Use test data builders for setup

3. **Performance**
    - Keep unit tests fast (< 1s)
    - Use `@MonitorPerformance` for critical tests
    - Monitor resource usage

### CI/CD Best Practices

1. **Pipeline Optimization**
    - Cache Gradle dependencies
    - Run tests in parallel where possible
    - Fail fast on critical errors

2. **Quality Gates**
    - Minimum 80% code coverage
    - Zero critical security vulnerabilities
    - All tests must pass

3. **Artifact Management**
    - Upload test reports for failed builds
    - Preserve coverage reports
    - Archive performance metrics

## Support

For questions or issues with test execution:

1. Check the troubleshooting section above
2. Review test logs in `build/logs/`
3. Examine coverage reports in `build/reports/jacoco/`
4. Contact the development team for assistance

## References

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)