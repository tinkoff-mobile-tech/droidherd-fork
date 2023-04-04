package com.shazam.fork.suite;

import com.shazam.fork.model.TestCaseEvent;

import java.util.Collection;

public interface TestsLoader {
    Collection<TestCaseEvent> loadTestSuite() throws NoTestCasesFoundException;
}
