package ru.tinkoff.testops.droidherd.auth;

public class AuthProviderCreator {
    public static AuthProvider create(String className) {
        try {
            if (className == null) {
                className = "ru.tinkoff.testops.droidherd.auth.BasicAuthProvider";
            }
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof AuthProvider) {
                return (AuthProvider) instance;
            }
            throw new RuntimeException("Bad auth provider supplied: " + className  + ". It must implements AuthProvider interface.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create droidherd auth provider: " + className, e);
        }
    }
}
