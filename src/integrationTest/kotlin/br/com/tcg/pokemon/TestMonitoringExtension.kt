package br.com.tcg.pokemon

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import java.time.Duration

/**
 * JUnit 5 extension for automatic test performance monitoring
 */
class TestMonitoringExtension : BeforeEachCallback, AfterEachCallback {

    private val logger = LoggerFactory.getLogger(TestMonitoringExtension::class.java)
    private val stopWatches = mutableMapOf<String, StopWatch>()

    override fun beforeEach(context: ExtensionContext) {
        val testName = "${context.testClass.get().simpleName}.${context.testMethod.get().name}"
        val stopWatch = TestMonitoringUtils.startTest(testName)
        stopWatches[context.uniqueId] = stopWatch

        // Log test start with context information
        logger.info("Starting test: $testName")

        // Check for performance monitoring annotation
        val method = context.testMethod.get()
        val monitorAnnotation = method.getAnnotation(MonitorPerformance::class.java)
        if (monitorAnnotation != null) {
            logger.info("Performance monitoring enabled for $testName (threshold: ${monitorAnnotation.threshold}ms)")
        }
    }

    override fun afterEach(context: ExtensionContext) {
        val stopWatch = stopWatches.remove(context.uniqueId)
        if (stopWatch != null) {
            val status = when {
                context.executionException.isPresent -> TestMonitoringUtils.TestStatus.FAILED
                else -> TestMonitoringUtils.TestStatus.PASSED
            }

            val additionalInfo = mutableMapOf<String, Any>()

            // Add exception information if test failed
            context.executionException.ifPresent { exception ->
                additionalInfo["exception"] = exception.javaClass.simpleName
                additionalInfo["message"] = exception.message ?: "No message"
            }

            // Check performance threshold if annotation is present
            val method = context.testMethod.get()
            val monitorAnnotation = method.getAnnotation(MonitorPerformance::class.java)
            if (monitorAnnotation != null) {
                val duration = Duration.ofNanos(stopWatch.totalTimeNanos)
                if (duration.toMillis() > monitorAnnotation.threshold) {
                    logger.warn(
                        "Test ${stopWatch.id} exceeded performance threshold: " +
                                "${duration.toMillis()}ms > ${monitorAnnotation.threshold}ms"
                    )
                    additionalInfo["thresholdExceeded"] = true
                }
            }

            TestMonitoringUtils.stopTest(stopWatch, status, additionalInfo)

            val testName = "${context.testClass.get().simpleName}.${context.testMethod.get().name}"
            logger.info("Completed test: $testName - Status: $status - Duration: ${stopWatch.totalTimeMillis}ms")
        }
    }
}