package ru.tinkoff.testops.droidherd;

import ru.tinkoff.testops.droidherd.auth.AuthProvider;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

public class DroidherdConfig {
    public final String clientType;
    public final Map<String, String> emulatorParameters;
    public final int minimumRequiredEmulators;
    public final AuthProvider authProvider;
    public final Map<String, Integer> emulators;
    public final String serviceUrl;

    public DroidherdConfig() {
        this(null, Collections.emptyMap(), 0, null, Collections.emptyMap(), null);
    }

    public DroidherdConfig(
            String clientType,
            Map<String, String> emulatorParameters,
            Integer minimumRequiredEmulators,
            AuthProvider authProvider,
            Map<String, Integer> emulators,
            String serviceUrl) {
        this.clientType = clientType;
        this.emulatorParameters = emulatorParameters;
        this.minimumRequiredEmulators = minimumRequiredEmulators;
        this.authProvider = authProvider;
        this.emulators = emulators;
        this.serviceUrl = serviceUrl;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DroidherdConfig.class.getSimpleName() + "[", "]")
                .add("clientType='" + clientType + "'")
                .add("emulatorParameters=" + emulatorParameters)
                .add("minimumRequiredEmulators=" + minimumRequiredEmulators)
                .add("authProvider=" + authProvider)
                .add("emulators=" + emulators)
                .add("serviceUrl='" + serviceUrl + "'")
                .toString();
    }

    public boolean isConfigured() {
        return serviceUrl != null && !emulators.isEmpty();
    }
}
