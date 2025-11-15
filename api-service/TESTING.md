# Testing Guide for Sentiment API Service

This guide explains how to run tests in IntelliJ IDEA and from the command line.

## Prerequisites

- Java 17 or higher
- IntelliJ IDEA (Community or Ultimate Edition)
- Lombok plugin installed in IntelliJ

## IntelliJ IDEA Setup

### 1. Install Lombok Plugin

1. Open IntelliJ IDEA
2. Go to **File → Settings** (Windows/Linux) or **IntelliJ IDEA → Preferences** (Mac)
3. Navigate to **Plugins**
4. Search for "Lombok"
5. Click **Install** and restart IntelliJ

### 2. Enable Annotation Processing

1. Go to **File → Settings** (Windows/Linux) or **IntelliJ IDEA → Preferences** (Mac)
2. Navigate to **Build, Execution, Deployment → Compiler → Annotation Processors**
3. Check **"Enable annotation processing"**
4. Set **"Obtain processors from project classpath"**
5. Click **Apply** and **OK**

### 3. Import Project

1. Open IntelliJ IDEA
2. Select **File → Open**
3. Navigate to `sentiment-project/api-service`
4. Select the `build.gradle.kts` file or the project folder
5. Click **OK**
6. When prompted, select **"Open as Project"**
7. IntelliJ will automatically import the Gradle project

### 4. Sync Gradle Dependencies

1. Click the Gradle icon on the right sidebar (or **View → Tool Windows → Gradle**)
2. Click the refresh icon to sync dependencies
3. Wait for dependencies to download

## Running Tests in IntelliJ

### Method 1: Using the Green Play Button (Recommended)

1. Open `SentimentControllerTest.java` in the editor
2. You'll see green play buttons (▶) next to:
   - The class name (runs all tests in the class)
   - Each nested class (runs all tests in that group)
   - Each individual test method
3. Click the green play button next to what you want to run
4. Select **"Run 'TestName'"** or **"Debug 'TestName'"**

### Method 2: Using Run Configurations

Pre-configured run configurations are available:

1. Click the run configuration dropdown at the top right (next to the play button)
2. Select one of:
   - **"All Tests"** - Runs all tests using Gradle
   - **"SentimentControllerTest"** - Runs all SentimentController tests

### Method 3: Right-Click in Project Explorer

1. In the Project view, navigate to `src/test/java/com/sentiment/api/controller`
2. Right-click on `SentimentControllerTest.java`
3. Select **"Run 'SentimentControllerTest'"**

### Method 4: Keyboard Shortcuts

- **Run test at cursor**: `Ctrl+Shift+F10` (Windows/Linux) or `Ctrl+Shift+R` (Mac)
- **Re-run last test**: `Shift+F10` (Windows/Linux) or `Ctrl+R` (Mac)
- **Debug test at cursor**: `Ctrl+Shift+F9` (Windows/Linux) or `Ctrl+Shift+D` (Mac)

## Running Tests from Command Line

### Run All Tests

```bash
cd api-service
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "SentimentControllerTest"
```

### Run Specific Test Method

```bash
./gradlew test --tests "SentimentControllerTest.health_ShouldReturnOk"
```

### Run Tests with Coverage Report

```bash
./gradlew test jacocoTestReport
```

View the coverage report at: `build/reports/jacoco/test/html/index.html`

### Run Tests in Continuous Mode

```bash
./gradlew test --continuous
```

Tests will automatically re-run when files change.

## Test Structure

```
api-service/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/sentiment/api/
│   │           ├── controller/
│   │           │   └── SentimentController.java
│   │           └── ...
│   └── test/
│       ├── java/
│       │   └── com/sentiment/api/
│       │       └── controller/
│       │           └── SentimentControllerTest.java (35+ tests)
│       └── resources/
│           └── application-test.yml
```

## Test Coverage

The test suite includes:

- **Health Endpoint Tests** (2 tests)
  - Status check
  - CORS validation

- **Analyze Endpoint Tests** (11 tests)
  - Valid positive/negative sentiment
  - Validation (blank, null, empty, too long)
  - Boundary testing (1000 chars)
  - Special characters
  - JSON validation

- **History Endpoint Tests** (4 tests)
  - Empty list handling
  - Multiple results
  - Maximum 10 entries

- **Test Cache Endpoint Tests** (4 tests)
  - With/without parameters
  - Empty text handling

- **CORS Configuration Tests** (2 tests)
  - Preflight requests
  - All origins allowed

- **Error Handling Tests** (4 tests)
  - 404, 405, 415 status codes
  - Wrong HTTP methods

## Troubleshooting

### Tests not showing in IntelliJ

1. **Invalidate caches**: File → Invalidate Caches → Invalidate and Restart
2. **Reimport Gradle**: Right-click `build.gradle.kts` → Reimport Gradle Project
3. **Check JDK**: File → Project Structure → Project → Ensure JDK 17 is selected
4. **Rebuild**: Build → Rebuild Project

### Lombok not working

1. Ensure Lombok plugin is installed
2. Enable annotation processing (see setup instructions above)
3. Verify `lombok` is in dependencies:
   ```bash
   ./gradlew dependencies | grep lombok
   ```

### Tests fail with "Cannot find symbol" errors

This usually means Lombok annotation processing isn't enabled:
1. Go to Settings → Annotation Processors
2. Enable annotation processing
3. Rebuild project

### H2 Database errors

The tests use H2 in-memory database. If you see H2 errors:
1. Check that `h2` dependency is present in `build.gradle.kts`
2. Verify `application-test.yml` has correct H2 configuration
3. Clean and rebuild: `./gradlew clean build`

### Spring context fails to load

If you see errors like "ApplicationContext failure threshold exceeded" or Redis/Cache configuration errors:

1. **Clean Gradle cache and rebuild**:
   ```bash
   ./gradlew clean
   ./gradlew build --refresh-dependencies
   ```

2. **In IntelliJ, invalidate caches and restart**:
   ```
   File → Invalidate Caches → Check "Clear file system cache" → Invalidate and Restart
   ```

3. **Delete IntelliJ's out directory**:
   ```bash
   rm -rf api-service/out
   rm -rf api-service/build
   ```

4. **Reimport Gradle project**:
   ```
   Right-click on api-service/build.gradle.kts → Reload Gradle Project
   ```

5. **Check test configuration**:
   - Verify `@WebMvcTest` excludes Redis and Cache auto-configuration
   - Verify `application-test.yml` has `cache.type: none`
   - Ensure `@ActiveProfiles("test")` is present

6. **If still failing, check for conflicting dependencies**:
   ```bash
   ./gradlew dependencies --configuration testRuntimeClasspath
   ```

## CI/CD Integration

Tests automatically run in GitHub Actions on every push:

```yaml
- name: Run tests
  run: ./gradlew test
```

Coverage reports are generated and can be viewed in the CI pipeline artifacts.

## Best Practices

1. **Run tests before committing**: Ensure all tests pass locally
2. **Write tests for new features**: Maintain test coverage above 80%
3. **Use descriptive test names**: Follow pattern `methodUnderTest_condition_expectedBehavior`
4. **Organize with @Nested**: Group related tests together
5. **Mock external dependencies**: Use @MockBean for service layer
6. **Test edge cases**: Include boundary values, null, empty, and invalid inputs

## Next Steps

After setting up tests for the controller layer, consider:

1. **Service Layer Tests** - Test `SentimentService` and `MLServiceClient`
2. **Repository Tests** - Test `AnalysisRepository` with @DataJpaTest
3. **Integration Tests** - Test full stack with @SpringBootTest
4. **Performance Tests** - Add load testing for critical endpoints
5. **Contract Tests** - Verify API contracts with ML service

## Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
