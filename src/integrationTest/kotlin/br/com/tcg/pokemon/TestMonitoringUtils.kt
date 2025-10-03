package br.com.tcg.pokemon

import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import java.time.Duration
import java.time.LocalDateTime

/**
 * Utility class for monitoring test execution performance and metrics
 */
object TestMonitoringUtils {

    private val performanceLogger = LoggerFactory.getLogger("TEST_PERFORMANCE")
    private val testMetrics = mutableMapOf<String, TestMetric>()

    data class TestMetric(
        val testName: String,
        val startTime: LocalDateTime,
        val duration: Duration,
        val status: TestStatus,
        val memoryUsed: Long,
        val additionalInfo: Map<String, Any> = emptyMap()
    )

    enum class TestStatus {
        PASSED, FAILED, SKIPPED
    }

    /**
     * Starts monitoring a test execution
     */
    fun startTest(testName: String): StopWatch {
        val stopWatch = StopWatch(testName)
        stopWatch.start()

        performanceLogger.info("Starting test: $testName at ${LocalDateTime.now()}")

        return stopWatch
    }

    /**
     * Stops monitoring a test execution and records metrics
     */
    fun stopTest(stopWatch: StopWatch, status: TestStatus, additionalInfo: Map<String, Any> = emptyMap()) {
        stopWatch.stop()

        val runtime = Runtime.getRuntime()
        val memoryUsed = runtime.totalMemory() - runtime.freeMemory()

        val metric = TestMetric(
            testName = stopWatch.id,
            startTime = LocalDateTime.now().minusNanos(stopWatch.totalTimeNanos),
            duration = Duration.ofNanos(stopWatch.totalTimeNanos),
            status = status,
            memoryUsed = memoryUsed,
            additionalInfo = additionalInfo
        )

        testMetrics[stopWatch.id] = metric

        performanceLogger.info(
            "Test completed: ${stopWatch.id} - " +
                    "Duration: ${stopWatch.totalTimeMillis}ms - " +
                    "Status: $status - " +
                    "Memory: ${memoryUsed / 1024 / 1024}MB"
        )
    }

    /**
     * Records database operation metrics
     */
    fun recordDatabaseOperation(operation: String, duration: Duration, recordCount: Int = 0) {
        performanceLogger.info(
            "DB Operation: $operation - " +
                    "Duration: ${duration.toMillis()}ms - " +
                    "Records: $recordCount"
        )
    }

    /**
     * Records HTTP request metrics
     */
    fun recordHttpRequest(method: String, endpoint: String, statusCode: Int, duration: Duration) {
        performanceLogger.info(
            "HTTP Request: $method $endpoint - " +
                    "Status: $statusCode - " +
                    "Duration: ${duration.toMillis()}ms"
        )
    }

    /**
     * Gets all recorded test metrics
     */
    fun getTestMetrics(): Map<String, TestMetric> = testMetrics.toMap()

    /**
     * Clears all recorded metrics
     */
    fun clearMetrics() {
        testMetrics.clear()
    }

    /**
     * Generates a performance summary report
     */
    fun generatePerformanceSummary(): String {
        val totalTests = testMetrics.size
        val passedTests = testMetrics.values.count { it.status == TestStatus.PASSED }
        val failedTests = testMetrics.values.count { it.status == TestStatus.FAILED }
        val skippedTests = testMetrics.values.count { it.status == TestStatus.SKIPPED }

        val avgDuration = if (testMetrics.isNotEmpty()) {
            testMetrics.values.map { it.duration.toMillis() }.average()
        } else 0.0

        val maxDuration = testMetrics.values.maxOfOrNull { it.duration.toMillis() } ?: 0L
        val minDuration = testMetrics.values.minOfOrNull { it.duration.toMillis() } ?: 0L

        return """
            |Test Performance Summary
            |=======================
            |Total Tests: $totalTests
            |Passed: $passedTests
            |Failed: $failedTests
            |Skipped: $skippedTests
            |
            |Performance Metrics:
            |Average Duration: ${String.format("%.2f", avgDuration)}ms
            |Max Duration: ${maxDuration}ms
            |Min Duration: ${minDuration}ms
            |
            |Generated at: ${LocalDateTime.now()}
        """.trimMargin()
    }
}

/**
 * Annotation to mark tests for performance monitoring
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MonitorPerformance(
    val threshold: Long = 5000, // milliseconds
    val trackMemory: Boolean = true
)