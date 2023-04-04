package ru.tinkoff.testops.droidherd;

import ru.tinkoff.testops.droidherd.api.DroidherdClientMetric;

import java.util.ArrayList;
import java.util.List;

public class DroidherdClientMetricCollector {
    public enum Key {
        TotalTestCases("total_test_cases"),
        PassedTestCases("passed_test_cases"),
        FailedTestCases("failed_test_cases"),
        ErrorTestCases("error_test_cases"),
        PendingEmulatorsDurationMs("pending_emulators_duration_ms"),
        TestsRunDurationMs("tests_run_duration_ms"),
        TotalRunDurationMs("total_run_duration_ms")
        ;
        private final String alias;

        Key(String alias) {
            this.alias = alias;
        }
    }
    private final List<DroidherdClientMetric> metrics = new ArrayList<>();

    public void add(Key key, double value) {
        metrics.add(new DroidherdClientMetric(key.alias, value));
    }

    public List<DroidherdClientMetric> getAll() {
        return metrics;
    }
}
