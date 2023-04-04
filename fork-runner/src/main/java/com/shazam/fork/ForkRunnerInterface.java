package com.shazam.fork;

import com.shazam.fork.aggregator.AggregatedTestResult;

public interface ForkRunnerInterface {

    class Result {
        public final boolean isSuccessful;
        public final AggregatedTestResult aggregatedTestResult;

        public static final AggregatedTestResult EMPTY_AGGREGATED_RESULT =
                AggregatedTestResult.Builder.aggregatedTestResult().build();

        public Result(boolean isSuccessful) {
            this(isSuccessful, EMPTY_AGGREGATED_RESULT);
        }

        public Result(boolean isSuccessful, AggregatedTestResult aggregatedTestResult) {
            this.isSuccessful = isSuccessful;
            this.aggregatedTestResult = aggregatedTestResult;
        }
    }

    Result run();
}
