package com.shazam.fork;

import com.shazam.fork.aggregator.AggregatedTestResult;
import com.shazam.fork.injector.ConfigurationInjector;
import com.shazam.fork.suite.TestsLoader;
import com.shazam.fork.summary.ResultStatus;
import com.shazam.fork.summary.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.testops.droidherd.DroidherdClient;
import ru.tinkoff.testops.droidherd.DroidherdClientMetricCollector;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class DroidherdForkRunner implements ForkRunnerInterface {
    private final Logger logger = LoggerFactory.getLogger(DroidherdForkRunner.class);

    private final ForkRunnerInterface runner;
    private final TestsLoader testsLoader;
    private final DroidherdClient droidherdClient;

    public DroidherdForkRunner(ForkRunnerInterface runner, DroidherdClient droidherdClient, TestsLoader testsLoader) {
        this.runner = runner;
        this.droidherdClient = droidherdClient;
        this.testsLoader = testsLoader;
    }

    @Override
    public Result run() {
        // use stdout due to gradle can run with any logging level
        System.out.println("Fork configuration for run: " + ConfigurationInjector.configuration());

        long startTime = System.currentTimeMillis();
        droidherdClient.run(
                testsLoader.loadTestSuite().size());
        long prepareEmulatorsSpent = System.currentTimeMillis() - startTime;
        logger.info("Prepare {} emulators took {} seconds",
                droidherdClient.connectedEmulatorsCount(),
                Duration.ofMillis(prepareEmulatorsSpent).toSeconds());

        startTime = System.currentTimeMillis();

        try {
            Result result = runner.run();
            long totalTimeSpent = System.currentTimeMillis() - startTime;
            try {
                droidherdClient.postMetrics(
                        generateMetrics(result.aggregatedTestResult, totalTimeSpent));
            } catch (Exception e) {
                logger.error("Failed to post metrics", e);
            }
            return result;
        } finally {
            droidherdClient.releaseEmulators();
        }
    }

    private DroidherdClientMetricCollector generateMetrics(AggregatedTestResult testResult, long totalTimeSpent) {
        DroidherdClientMetricCollector metricCollector = new DroidherdClientMetricCollector();
        List<TestResult> testCaseResults = testResult.getPoolTestResults()
                .stream()
                .flatMap(result -> result.getTestResults().stream())
                .collect(Collectors.toUnmodifiableList());

        Map<ResultStatus, List<TestResult>> testsByStatus = testCaseResults.stream()
                .collect(groupingBy(TestResult::getResultStatus));

        int passed = getTestsCountByStatus(testsByStatus, ResultStatus.PASS);
        metricCollector.add(DroidherdClientMetricCollector.Key.PassedTestCases, passed);

        int failed = getTestsCountByStatus(testsByStatus, ResultStatus.FAIL);
        metricCollector.add(DroidherdClientMetricCollector.Key.FailedTestCases, failed);

        int error = getTestsCountByStatus(testsByStatus, ResultStatus.ERROR);
        metricCollector.add(DroidherdClientMetricCollector.Key.ErrorTestCases, error);

        metricCollector.add(DroidherdClientMetricCollector.Key.TotalTestCases, passed + failed + error);

        double testingTime = testCaseResults.stream().mapToDouble(TestResult::getTimeTaken).sum();
        metricCollector.add(DroidherdClientMetricCollector.Key.TestsRunDurationMs, testingTime * 1000);

        metricCollector.add(DroidherdClientMetricCollector.Key.TotalRunDurationMs, totalTimeSpent);

        return metricCollector;
    }

    private int getTestsCountByStatus(Map<ResultStatus, List<TestResult>> testsByStatus, ResultStatus status) {
        return testsByStatus.getOrDefault(status, Collections.emptyList()).size();
    }
}
