/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.lang.System.Logger.Level;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggerPluginTest {

    private static final String name = "LoggerPlugin";

    private static final Error error = new Error();

    private static ThreadLocal<Deque<Log>> logs = ThreadLocal.withInitial(() -> new ArrayDeque());

    @BeforeAll
    static void init() {
        I.env(name + ".file", Level.OFF);
        I.env(name + ".console", Level.OFF);
        I.env(name + ".extra", Level.ALL);

        I.Logger = (name, level, message) -> {
            logs.get().add(new Log(name, level, message));
        };
    }

    @BeforeEach
    void setup() {
        logs.get().clear();
    }

    @Test
    void traceWithDirectString() {
        I.trace(name, "TEST");
        assert checkLog(Level.TRACE, "TEST");
    }

    @Test
    void traceWithLazyEvaluation() {
        I.trace(name, (Supplier) () -> "LAZY");
        assert checkLog(Level.TRACE, "LAZY");
    }

    @Test
    void traceWithError() {
        I.trace(name, error);
        assert checkLog(Level.TRACE, error);
    }

    @Test
    void debugWithDirectString() {
        I.debug(name, "TEST");
        assert checkLog(Level.DEBUG, "TEST");
    }

    @Test
    void debugWithLazyEvaluation() {
        I.debug(name, (Supplier) () -> "LAZY");
        assert checkLog(Level.DEBUG, "LAZY");
    }

    @Test
    void debugWithError() {
        I.debug(name, error);
        assert checkLog(Level.DEBUG, error);
    }

    @Test
    void infoWithDirectString() {
        I.info(name, "TEST");
        assert checkLog(Level.INFO, "TEST");
    }

    @Test
    void infoWithLazyEvaluation() {
        I.info(name, (Supplier) () -> "LAZY");
        assert checkLog(Level.INFO, "LAZY");
    }

    @Test
    void infoWithError() {
        I.info(name, error);
        assert checkLog(Level.INFO, error);
    }

    @Test
    void warnWithDirectString() {
        I.warn(name, "TEST");
        assert checkLog(Level.WARNING, "TEST");
    }

    @Test
    void warnWithLazyEvaluation() {
        I.warn(name, (Supplier) () -> "LAZY");
        assert checkLog(Level.WARNING, "LAZY");
    }

    @Test
    void warnWithError() {
        I.warn(name, error);
        assert checkLog(Level.WARNING, error);
    }

    @Test
    void errorWithDirectString() {
        I.error(name, "TEST");
        assert checkLog(Level.ERROR, "TEST");
    }

    @Test
    void errorWithLazyEvaluation() {
        I.error(name, (Supplier) () -> "LAZY");
        assert checkLog(Level.ERROR, "LAZY");
    }

    @Test
    void errorWithError() {
        I.error(name, error);
        assert checkLog(Level.ERROR, error);
    }

    private boolean checkLog(Level level, Object message) {
        Log log = logs.get().pollFirst();
        assert log != null;
        assert log.name.equals(name);
        assert log.level == level;
        assert log.message.equals(message);
        return true;
    }

    record Log(String name, Level level, Object message) {
    }
}
