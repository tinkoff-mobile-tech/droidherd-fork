package com.shazam.fork.suite;

import com.shazam.fork.model.TestCaseEvent;

import java.util.Collection;

public class CachedTestsLoader implements TestsLoader {
    private final TestsLoader loader;
    private Collection<TestCaseEvent> loadedTests = null;

    public CachedTestsLoader(TestsLoader loader) {
        this.loader = loader;
    }

    @Override
    public Collection<TestCaseEvent> loadTestSuite() throws NoTestCasesFoundException {
        if (loadedTests == null) {
            loadedTests = loader.loadTestSuite();
        }
        return loadedTests;
    }
}
