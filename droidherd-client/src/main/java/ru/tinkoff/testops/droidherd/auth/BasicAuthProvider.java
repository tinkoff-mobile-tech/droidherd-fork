package ru.tinkoff.testops.droidherd.auth;

import java.util.Optional;

public class BasicAuthProvider implements AuthProvider {
    private final DroidherdAuthData data;
    private final String clientId;
    private final String token;

    public BasicAuthProvider() {
        this.data = new DroidherdAuthData() {
            @Override
            public String getClientId() {
                return clientId;
            }

            @Override
            public String getToken() {
                return token;
            }
        };
        this.clientId = Optional.ofNullable(System.getenv("DROIDHERD_CLIENT_ID"))
                .orElse("fork_default");
        this.token = Optional.ofNullable(System.getenv("DROIDHERD_AUTH_TOKEN"))
                .orElse("");
    }

    @Override
    public DroidherdAuthData get() {
        return data;
    }
}
