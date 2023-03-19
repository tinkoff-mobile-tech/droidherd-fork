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
package com.shazam.fork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Semaphore;

import static com.shazam.fork.injector.ConfigurationInjector.setConfiguration;
import static com.shazam.fork.injector.ForkRunnerInjector.forkRunner;
import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatPeriod;

public final class Fork {
    private static final Logger logger = LoggerFactory.getLogger(Fork.class);
    private static final Semaphore semaphore = new Semaphore(1, true);

    private final File output;
    private final Configuration configuration;

    public Fork(Configuration configuration) {
        this.output = configuration.getOutput();
        this.configuration = configuration;
    }

    public boolean run() {
        if (!semaphore.tryAcquire()) {
            logger.info("Parallel launch of fork detected. Waiting lock to run next task.");
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("Parallel task completed, launch next");
        }

        try {
            setConfiguration(configuration);
            ForkRunnerInterface forkRunner = forkRunner();
            long startOfTestsMs = nanoTime();
            try {
                deleteDirectory(output);
                //noinspection ResultOfMethodCallIgnored
                output.mkdirs();
                return forkRunner.run().isSuccessful;
            } catch (Exception e) {
                logger.error("Error while running Fork", e);
            } finally {
                long duration = millisSinceNanoTime(startOfTestsMs);
                logger.info(formatPeriod(0, duration, "'Total time taken:' H 'hours' m 'minutes' s 'seconds'"));
            }
        } finally {
            semaphore.release();
        }
        return false;
    }
}
