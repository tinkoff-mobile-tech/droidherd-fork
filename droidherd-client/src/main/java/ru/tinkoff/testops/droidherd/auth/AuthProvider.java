package ru.tinkoff.testops.droidherd.auth;

import java.util.function.Supplier;

public interface AuthProvider extends Supplier<DroidherdAuthData> {
}
