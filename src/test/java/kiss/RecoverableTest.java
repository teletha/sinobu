/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;

/**
 * @version 2018/04/02 9:25:12
 */
public class RecoverableTest {

    @Test
    void ok() {
        String value = I.run(this::value);
        assert value == "pass";
    }

    private String value() {
        return "pass";
    }

    @Test
    void exception1() {
        AtomicInteger value = new AtomicInteger();
        I.run(unstableOperation(() -> value.set(20), IOException.class), I.retryWhen(IOException.class));
        assert value.get() == 20;
    }

    @Test
    void exception1Signalable() {
        AtomicInteger value = new AtomicInteger();
        I.run(unstableOperation(() -> value.set(20), IOException.class), e -> e.type(IOException.class));
        assert value.get() == 20;
    }

    @Test
    void error1() {
        String value = I.run(unstableOperation(() -> "pass", LinkageError.class), I.retryWhen(LinkageError.class, 1));
        assert value == "pass";
    }

    @Test
    void error1Signalable() {
        String value = I.run(unstableOperation(() -> "pass", LinkageError.class), e -> e.type(LinkageError.class).take(1));
        assert value == "pass";
    }

    @ExpectThrow(Error.class)
    void error1WithoutRecovery() {
        String value = I.run(unstableOperation(() -> "pass", Error.class));
        assert value == "pass";
    }

    @Test
    void error1WithoutRecoverSignalable() {
        Assertions.assertThrows(Error.class, () -> {
            I.run(unstableOperation(() -> "pass", Error.class));
        });
    }

    @ExpectThrow(Error.class)
    void error2() {
        I.run(unstableOperation(() -> "error", Error.class, Error.class), I.retryWhen(Error.class, 1));
    }

    @Test
    void error2Signalable() {
        Assertions.assertThrows(Error.class, () -> {
            I.run(unstableOperation(() -> "pass", Error.class, Error.class), e -> e.type(Error.class).take(1));
        });
    }

    @Test
    void error2WithUnlimitedRetry() {
        String value = I.run(unstableOperation(() -> "pass", Error.class, Error.class), I.retryWhen(Error.class));
        assert value == "pass";
    }

    @Test
    void error2WithUnlimitedRetrySignalable() {
        String value = I.run(unstableOperation(() -> "pass", Error.class, Error.class), e -> e.type(Error.class));
        assert value == "pass";
    }

    @ExpectThrow(Exception.class)
    void recoverOnlyError() {
        I.run(unstableOperation(() -> "error", Error.class, Exception.class), I.retryWhen(Error.class));
    }

    @Test
    void recoverOnlyErrorSignalable() {
        Assertions.assertThrows(Exception.class, () -> {
            I.run(unstableOperation(() -> "error", Error.class, Exception.class), e -> e.type(Error.class));
        });
    }

    @ExpectThrow(Error.class)
    void recoverOnlyException() {
        I.run(unstableOperation(() -> "error", Error.class, Exception.class), I.retryWhen(Exception.class));
    }

    @Test
    void recoverOnlyExceptionSignalable() {
        Assertions.assertThrows(Error.class, () -> {
            I.run(unstableOperation(() -> "error", Error.class, Exception.class), e -> e.type(Exception.class));
        });
    }

    @Test
    void recoverErrorAndException() {
        String value = I
                .run(unstableOperation(() -> "pass", Error.class, Exception.class), I.retryWhen(Exception.class), I.retryWhen(Error.class));
        assert value == "pass";
    }

    @Test
    void recoverErrorAndExceptionSignalable() {
        I.run(unstableOperation(() -> "error", Error.class, Exception.class), e -> e.type(Error.class, Exception.class));
    }

    @Test
    void recoverSubType() {
        String value = I.run(unstableOperation(() -> "pass", IOException.class), I.retryWhen(Exception.class));
        assert value == "pass";
    }

    @Test
    void recoverSubTypeSignalable() {
        String value = I.run(unstableOperation(() -> "pass", IOException.class), e -> e.type(Exception.class));
        assert value == "pass";
    }

    /**
     * <p>
     * Helper method to create operation with failuers.
     * </p>
     * 
     * @param op
     * @param errorType
     * @return
     */
    private WiseRunnable unstableOperation(Runnable op, Class<? extends Throwable>... errors) {
        AtomicInteger count = new AtomicInteger();

        return () -> {
            if (count.get() < errors.length) {
                throw I.make(errors[count.getAndIncrement()]);
            }
            op.run();
        };
    }

    /**
     * <p>
     * Helper method to create operation with failuers.
     * </p>
     * 
     * @param op
     * @param errorType
     * @return
     */
    private <R> WiseSupplier<R> unstableOperation(Supplier<R> op, Class<? extends Throwable>... errors) {
        AtomicInteger count = new AtomicInteger();

        return () -> {
            if (count.get() < errors.length) {
                throw I.make(errors[count.getAndIncrement()]);
            }
            return op.get();
        };
    }
}
