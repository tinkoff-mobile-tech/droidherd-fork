package com.shazam.fork.injector.system;

import com.shazam.fork.Configuration;
import org.apache.commons.io.FileUtils;
import ru.tinkoff.testops.droidherd.DroidherdClient;
import ru.tinkoff.testops.droidherd.RetrofitProvider;
import ru.tinkoff.testops.droidherd.impl.DroidherdApiProvider;
import ru.tinkoff.testops.droidherd.impl.DroidherdApiProviderImpl;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

public class DroidherdClientInjector {
    private static final DroidherdApiProvider apiProvider = createProvider(configuration());
    private static final DroidherdClient client = create(configuration());

    private static DroidherdClient create(Configuration configuration) {
        String adbPath = FileUtils.getFile(configuration.getAndroidSdk().getAbsoluteFile(), "platform-tools", "adb").getAbsolutePath();
        return new DroidherdClient(
                adbPath,
                apiProvider(),
                configuration.getDroidherdConfig()
        );
    }

    private static DroidherdApiProvider createProvider(Configuration configuration) {
        RetrofitProvider retrofitProvider = new RetrofitProvider(configuration.getDroidherdConfig().serviceUrl);
        return new DroidherdApiProviderImpl(
                retrofitProvider,
                configuration.getDroidherdConfig().authProvider
        );
    }

    private DroidherdClientInjector() {

    }

    public static DroidherdClient clientInstance() {
        return client;
    }

    public static DroidherdApiProvider apiProvider() {
        return apiProvider;
    }
}
