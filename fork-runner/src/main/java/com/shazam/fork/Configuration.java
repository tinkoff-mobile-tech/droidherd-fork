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

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.google.common.base.Supplier;
import com.shazam.fork.system.adb.AdbInterface;
import com.shazam.fork.system.axmlparser.ApplicationInfo;
import com.shazam.fork.system.axmlparser.ApplicationInfoFactory;
import com.shazam.fork.system.axmlparser.InstrumentationInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.testops.droidherd.DroidherdConfig;
import ru.tinkoff.testops.droidherd.auth.AuthProvider;
import ru.tinkoff.testops.droidherd.auth.AuthProviderCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.system.axmlparser.InstrumentationInfoFactory.parseFromFile;

public class Configuration implements ForkConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final File androidSdk;
    private final File applicationApk;
    private final File instrumentationApk;
    private final String applicationPackage;
    private final String instrumentationPackage;
    private final String testRunnerClass;
    private final File output;
    private final String title;
    private final String subtitle;
    private final Pattern testClassPattern;
    private final String testPackage;
    private final long testOutputTimeout;
    private final IRemoteAndroidTestRunner.TestSize testSize;
    private final Collection<String> excludedSerials;
    private final int totalAllowedRetryQuota;
    private final int retryPerTestCaseQuota;
    private final boolean isCoverageEnabled;
    private final Map<String, String> instrumentationArgs;
    private final PoolingStrategy poolingStrategy;
    private final boolean autoGrantPermissions;
    private final String excludedAnnotation;
    private final ApplicationInfo applicationInfo;
    private final AdbInterface.Type adbUsageType;
    private final DroidherdConfig droidherdConfig;

    private Configuration(Builder builder, DroidherdConfig droidherdConfig) {
        instrumentationArgs = builder.instrumentationArgs;
        adbUsageType = builder.adbUsageType;
        androidSdk = builder.androidSdk;
        applicationApk = builder.applicationApk;
        instrumentationApk = builder.instrumentationApk;
        applicationPackage = builder.applicationPackage;
        instrumentationPackage = builder.instrumentationPackage;
        testRunnerClass = builder.testRunnerClass;
        output = builder.output;
        title = builder.title;
        subtitle = builder.subtitle;
        testClassPattern = Pattern.compile(builder.testClassRegex);
        testPackage = builder.testPackage;
        testOutputTimeout = builder.testOutputTimeout;
        testSize = builder.testSize;
        excludedSerials = builder.excludedSerials;
        totalAllowedRetryQuota = builder.totalAllowedRetryQuota;
        retryPerTestCaseQuota = builder.retryPerTestCaseQuota;
        isCoverageEnabled = builder.isCoverageEnabled;
        poolingStrategy = builder.poolingStrategy;
        autoGrantPermissions = builder.autoGrantPermissions;
        this.excludedAnnotation = builder.excludedAnnotation;
        this.applicationInfo = builder.applicationInfo;
        this.droidherdConfig = droidherdConfig;
    }

    @Override
    @Nonnull
    public File getAndroidSdk() {
        return androidSdk;
    }

    @Override
    @Nonnull
    public File getApplicationApk() {
        return applicationApk;
    }

    @Override
    @Nonnull
    public File getInstrumentationApk() {
        return instrumentationApk;
    }

    @Override
    @Nonnull
    public String getApplicationPackage() {
        return applicationPackage;
    }

    @Override
    @Nonnull
    public String getInstrumentationPackage() {
        return instrumentationPackage;
    }

    @Override
    @Nonnull
    public String getTestRunnerClass() {
        return testRunnerClass;
    }

    @Override
    @Nonnull
    public File getOutput() {
        return output;
    }

    @Override
    @Nonnull
    public String getTitle() {
        return title;
    }

    @Override
    @Nonnull
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    @Nonnull
    public Pattern getTestClassPattern() {
        return testClassPattern;
    }

    @Override
    @Nonnull
    public String getTestPackage() {
        return testPackage;
    }

    @Override
    public long getTestOutputTimeout() {
        return testOutputTimeout;
    }

    @Override
    @Nullable
    public IRemoteAndroidTestRunner.TestSize getTestSize() {
        return testSize;
    }

    @Override
    @Nonnull
    public Collection<String> getExcludedSerials() {
        return excludedSerials;
    }

    @Override
    public int getTotalAllowedRetryQuota() {
        return totalAllowedRetryQuota;
    }

    @Override
    public int getRetryPerTestCaseQuota() {
        return retryPerTestCaseQuota;
    }

    @Override
    public boolean isCoverageEnabled() {
        return isCoverageEnabled;
    }

    @Override
    public PoolingStrategy getPoolingStrategy() {
        return poolingStrategy;
    }

    @Override
    public boolean isAutoGrantingPermissions() {
        return autoGrantPermissions;
    }

    @Override
    public String getExcludedAnnotation() {
        return excludedAnnotation;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public Map<String, String> getInstrumentationArgs() {
        return instrumentationArgs;
    }

    public AdbInterface.Type getAdbUsageType() {
        return adbUsageType;
    }

    public DroidherdConfig getDroidherdConfig() {
        return droidherdConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("androidSdk", androidSdk)
                .append("applicationApk", applicationApk)
                .append("instrumentationApk", instrumentationApk)
                .append("applicationPackage", applicationPackage)
                .append("instrumentationPackage", instrumentationPackage)
                .append("testRunnerClass", testRunnerClass)
                .append("output", output)
                .append("title", title)
                .append("subtitle", subtitle)
                .append("testClassPattern", testClassPattern)
                .append("testPackage", testPackage)
                .append("testOutputTimeout", testOutputTimeout)
                .append("testSize", testSize)
                .append("excludedSerials", excludedSerials)
                .append("totalAllowedRetryQuota", totalAllowedRetryQuota)
                .append("retryPerTestCaseQuota", retryPerTestCaseQuota)
                .append("isCoverageEnabled", isCoverageEnabled)
                .append("poolingStrategy", poolingStrategy)
                .append("autoGrantPermissions", autoGrantPermissions)
                .append("excludedAnnotation", excludedAnnotation)
                .append("applicationInfo", applicationInfo)
                .append("instrumentationArgs", instrumentationArgs)
                .append("adbUsageType", adbUsageType)
                .append("droidherdConfig", droidherdConfig)
                .toString();
    }

    public static class Builder {
        private File androidSdk;
        private File applicationApk;
        private File instrumentationApk;
        private String applicationPackage;
        private String instrumentationPackage;
        private String testRunnerClass;
        private File output;
        private String title;
        private String subtitle;
        private String testClassRegex;
        private String testPackage;
        private long testOutputTimeout;
        private IRemoteAndroidTestRunner.TestSize testSize;
        private Collection<String> excludedSerials;
        private int totalAllowedRetryQuota;
        private int retryPerTestCaseQuota;
        private boolean isCoverageEnabled;
        private PoolingStrategy poolingStrategy;
        private boolean autoGrantPermissions;
        private String excludedAnnotation;
        private ApplicationInfo applicationInfo;
        private String clientType;
        private Map<String, String> instrumentationArgs;
        private Map<String, String> emulatorParameters;
        private AdbInterface.Type adbUsageType;
        private Integer minimumRequiredEmulators;
        private String droidherdAuthProviderType;
        private AuthProvider droidherdAuthProvider;
        private Map<String, Integer> emulators;
        private String emulatorFarmEndpoint;

        public static Builder configuration() {
            return new Builder();
        }

        public Builder withAndroidSdk(@Nonnull File androidSdk) {
            this.androidSdk = androidSdk;
            return this;
        }

        public Builder withApplicationApk(@Nonnull File applicationApk) {
            this.applicationApk = applicationApk;
            return this;
        }

        public Builder withInstrumentationApk(@Nonnull File instrumentationApk) {
            this.instrumentationApk = instrumentationApk;
            return this;
        }

        public Builder withOutput(@Nonnull File output) {
            this.output = output;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder withTestClassRegex(String testClassRegex) {
            this.testClassRegex = testClassRegex;
            return this;
        }

        public Builder withTestPackage(String testPackage) {
            this.testPackage = testPackage;
            return this;
        }

        public Builder withTestOutputTimeout(int testOutputTimeout) {
            this.testOutputTimeout = testOutputTimeout;
            return this;
        }

        public Builder withTestSize(String testSize) {
            this.testSize = (testSize == null ? null : IRemoteAndroidTestRunner.TestSize.getTestSize(testSize));
            return this;
        }

        public Builder withExcludedSerials(Collection<String> excludedSerials) {
            this.excludedSerials = excludedSerials;
            return this;
        }

        public Builder withTotalAllowedRetryQuota(int totalAllowedRetryQuota) {
            this.totalAllowedRetryQuota = totalAllowedRetryQuota;
            return this;
        }

        public Builder withRetryPerTestCaseQuota(int retryPerTestCaseQuota) {
            this.retryPerTestCaseQuota = retryPerTestCaseQuota;
            return this;
        }

        public Builder withCoverageEnabled(boolean isCoverageEnabled) {
            this.isCoverageEnabled = isCoverageEnabled;
            return this;
        }

        public Builder withPoolingStrategy(@Nullable PoolingStrategy poolingStrategy) {
            this.poolingStrategy = poolingStrategy;
            return this;
        }

        public Builder withAutoGrantPermissions(boolean autoGrantPermissions) {
            this.autoGrantPermissions = autoGrantPermissions;
            return this;
        }

        public Builder withExcludedAnnotation(String excludedAnnotation) {
            this.excludedAnnotation = excludedAnnotation;
            return this;
        }

        public Builder withClientType(String type) {
            this.clientType = type;
            return this;
        }

        public Builder withInstrumentationArgs(String instrumentationArgs) {
            this.instrumentationArgs = StringUtils.isBlank(instrumentationArgs)
                    ? Collections.emptyMap()
                    : Arrays.stream(instrumentationArgs.split(","))
                    .map(it -> it.split(":"))
                    .collect(Collectors.toMap(it -> it[0].trim(), it -> it[1]));
            return this;
        }

        public Builder withEmulatorParameters(String parameters) {
            this.emulatorParameters = StringUtils.isBlank(parameters)
                    ? Collections.emptyMap()
                    : Arrays.stream(parameters.split(","))
                    .map(it -> it.split(":"))
                    .collect(Collectors.toMap(it -> it[0].trim(), it -> it[1]));
            return this;
        }

        public Builder withAdbUsageType(String value) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(value)) {
                adbUsageType = AdbInterface.Type.valueOf(value);
            }
            return this;
        }

        public Builder withMinimumRequiredEmulators(Integer minimumRequiredEmulators) {
            this.minimumRequiredEmulators = minimumRequiredEmulators;
            return this;
        }

        public Builder withDroidherdAuthProviderType(String droidherdAuthType) {
            this.droidherdAuthProviderType = droidherdAuthType;
            return this;
        }

        public Builder withEmulators(String emulators) {
            this.emulators = StringUtils.isBlank(emulators)
                    ? null
                    : Arrays.stream(emulators.split(","))
                    .map(it -> it.split(":"))
                    .collect(Collectors.toMap(it -> it[0].trim(), it -> Integer.valueOf(it[1])));

            return this;
        }

        public Builder withEmulatorFarmEndpoint(String emulatorFarmEndpoint) {
            this.emulatorFarmEndpoint = emulatorFarmEndpoint;
            return this;
        }

        public Configuration build() {
            checkNotNull(androidSdk, "SDK is required.");
            checkArgument(androidSdk.exists(), "SDK directory does not exist.");
            checkNotNull(applicationApk, "Application APK is required.");
            checkArgument(applicationApk.exists(), "Application APK file does not exist.");
            checkNotNull(instrumentationApk, "Instrumentation APK is required.");
            checkArgument(instrumentationApk.exists(), "Instrumentation APK file does not exist.");
            InstrumentationInfo instrumentationInfo = parseFromFile(instrumentationApk);
            checkNotNull(instrumentationInfo.getApplicationPackage(), "Application package was not found in test APK");
            applicationPackage = instrumentationInfo.getApplicationPackage();
            checkNotNull(instrumentationInfo.getInstrumentationPackage(), "Instrumentation package was not found in test APK");
            instrumentationPackage = instrumentationInfo.getInstrumentationPackage();
            checkNotNull(instrumentationInfo.getTestRunnerClass(), "Test runner class was not found in test APK");
            testRunnerClass = instrumentationInfo.getTestRunnerClass();
            checkNotNull(output, "Output path is required.");

            title = assignValueOrDefaultIfNull(title, Defaults.TITLE);
            subtitle = assignValueOrDefaultIfNull(subtitle, Defaults.SUBTITLE);
            testClassRegex = assignValueOrDefaultIfNull(testClassRegex, CommonDefaults.TEST_CLASS_REGEX);
            testPackage = assignValueOrDefaultIfNull(testPackage, instrumentationInfo.getApplicationPackage());
            testOutputTimeout = assignValueOrDefaultIfZero(testOutputTimeout, Defaults.TEST_OUTPUT_TIMEOUT_MILLIS);
            excludedSerials = assignValueOrDefaultIfNull(excludedSerials, Collections.emptyList());
            checkArgument(totalAllowedRetryQuota >= 0, "Total allowed retry quota should not be negative.");
            checkArgument(retryPerTestCaseQuota >= 0, "Retry per test case quota should not be negative.");
            retryPerTestCaseQuota = assignValueOrDefaultIfZero(retryPerTestCaseQuota, Defaults.RETRY_QUOTA_PER_TEST_CASE);
            logArgumentsBadInteractions();
            poolingStrategy = validatePoolingStrategy(poolingStrategy);
            applicationInfo = ApplicationInfoFactory.parseFromFile(applicationApk);
            adbUsageType = assignValueOrDefaultIfNull(adbUsageType, AdbInterface.Type.Droidherd);
            return new Configuration(this, createDroidherdConfig());
        }

        private DroidherdConfig createDroidherdConfig() {
            if (StringUtils.isBlank(emulatorFarmEndpoint)) {
                logger.info("Emulators endpoint not defined. Running on physical devices.");
                return new DroidherdConfig();
            }
            String authProvider = droidherdAuthProviderType == null
                    ? System.getenv("DROIDHERD_AUTH_PROVIDER")
                    : droidherdAuthProviderType;
            String path = (authProvider == null || authProvider.contains(".") ? authProvider : "ru.tinkoff.testops.droidherd.auth." + authProvider);
            droidherdAuthProvider = AuthProviderCreator.create(path);
            emulators = assignValueOrDefaultIfNull(emulators, () -> { throw new RuntimeException("emulators parameter not set"); });
            minimumRequiredEmulators = assignValueOrDefaultIfZero(minimumRequiredEmulators, 1);
            checkArgument(emulators.size() > 0, "No emulators specified");
            int totalAmountOfEmulators = emulators.values().stream().mapToInt(it -> it).sum();
            logger.info("Amount of emulators - " + totalAmountOfEmulators);
            checkArgument(totalAmountOfEmulators >= minimumRequiredEmulators, "Required emulators cannot be less then minimum");
            emulatorParameters = assignValueOrDefaultIfNull(emulatorParameters, Collections.emptyMap());
            return new DroidherdConfig(
                    clientType, emulatorParameters, minimumRequiredEmulators, droidherdAuthProvider, emulators, emulatorFarmEndpoint
            );
        }

        private static String assignValueOrDefaultIfEmpty(String value, String defaultValue) {
            return StringUtils.isNotBlank(value) ? value : defaultValue;
        }

        private static <T> T assignValueOrDefaultIfNull(T value, T defaultValue) {
            return value != null ? value : defaultValue;
        }

        private static <T> T assignValueOrDefaultIfNull(T value, Supplier<T> supplier) {
            return value != null ? value : supplier.get();
        }

        private static <T extends Number> T assignValueOrDefaultIfZero(T value, T defaultValue) {
            return value.longValue() != 0 ? value : defaultValue;
        }

        private void logArgumentsBadInteractions() {
            if (totalAllowedRetryQuota > 0 && totalAllowedRetryQuota < retryPerTestCaseQuota) {
                logger.warn("Total allowed retry quota [" + totalAllowedRetryQuota + "] " +
                        "is smaller than Retry per test case quota [" + retryPerTestCaseQuota + "]. " +
                        "This is suspicious as the first mentioned parameter is an overall cap.");
            }
        }

        /**
         * We need to make sure zero or one strategy has been passed. If zero default to pool per device. If more than one
         * we throw an exception.
         */
        private PoolingStrategy validatePoolingStrategy(PoolingStrategy poolingStrategy) {
            if (poolingStrategy == null) {
                logger.warn("No strategy was chosen in configuration, so defaulting to one pool per device");
                poolingStrategy = new PoolingStrategy();
                poolingStrategy.eachDevice = true;
            } else {
                long selectedStrategies = Stream.of(
                        poolingStrategy.eachDevice,
                        poolingStrategy.splitTablets,
                        poolingStrategy.computed,
                        poolingStrategy.manual)
                        .filter(p -> p != null)
                        .count();
                if (selectedStrategies > Defaults.STRATEGY_LIMIT) {
                    throw new IllegalArgumentException("You have selected more than one strategies in configuration. " +
                            "You can only select up to one.");
                }
            }

            return poolingStrategy;
        }
    }
}
