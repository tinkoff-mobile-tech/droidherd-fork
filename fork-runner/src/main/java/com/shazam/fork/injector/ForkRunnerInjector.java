/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.injector;

import com.shazam.fork.Configuration;
import com.shazam.fork.DroidherdForkRunner;
import com.shazam.fork.ForkRunner;
import com.shazam.fork.ForkRunnerInterface;
import com.shazam.fork.injector.system.DroidherdClientInjector;
import com.shazam.fork.suite.TestsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;
import static com.shazam.fork.injector.aggregator.AggregatorInjector.aggregator;
import static com.shazam.fork.injector.pooling.PoolLoaderInjector.poolLoader;
import static com.shazam.fork.injector.runner.PoolTestRunnerFactoryInjector.poolTestRunnerFactory;
import static com.shazam.fork.injector.runner.ProgressReporterFactoryInjector.progressReporterFactory;
import static com.shazam.fork.injector.suite.TestSuiteLoaderInjector.testSuiteLoader;
import static com.shazam.fork.injector.summary.OutcomeAggregatorInjector.outcomeAggregator;
import static com.shazam.fork.injector.summary.SummaryGeneratorHookInjector.summaryGeneratorHook;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;

public class ForkRunnerInjector {

    private static final Logger logger = LoggerFactory.getLogger(ForkRunnerInjector.class);

    private ForkRunnerInjector() {
    }

    public static ForkRunnerInterface forkRunner() {
        long startNanos = nanoTime();

        Configuration configuration = configuration();
        TestsLoader testsLoader = testSuiteLoader();
        ForkRunnerInterface forkRunner = new ForkRunner(
                poolLoader(),
                testsLoader,
                poolTestRunnerFactory(),
                progressReporterFactory(),
                summaryGeneratorHook(),
                outcomeAggregator(),
                aggregator()
        );
        if (configuration.getDroidherdConfig().isConfigured()) {
            forkRunner = new DroidherdForkRunner(forkRunner, DroidherdClientInjector.clientInstance(), testsLoader);
        }

        logger.debug("Bootstrap of ForkRunner took: {} milliseconds", millisSinceNanoTime(startNanos));

        return forkRunner;
    }
}
