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

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;

/**
 * @version 2018/03/31 23:14:18
 */
public class RecoverableTest {

    @Test
    public void ok() throws Exception {
        int value = I.run(this::value);
        assert value == 10;
    }

    private int value() {
        return 10;
    }

    @Test
    public void exception1() throws Exception {
        AtomicInteger value = new AtomicInteger();
        I.run(unstableOperation(() -> value.set(20), Exception.class), I.retryWhen(Exception.class));
        assert value.get() == 20;
    }

    @Test
    public void error1() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class), I.retryWhen(Error.class, 1));
        assert value == 10;
    }

    @ExpectThrow(Error.class)
    public void error1WithoutRecovery() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class));
        assert value == 10;
    }

    @ExpectThrow(Error.class)
    public void error2() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class, Error.class), I.retryWhen(Error.class, 1));
        assert value == 10;
    }

    @Test
    public void error2WithUnlimitedRetry() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class, Error.class), I.retryWhen(Error.class));
        assert value == 10;
    }

    @ExpectThrow(Exception.class)
    public void recoverOnlyError() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class, Exception.class), I.retryWhen(Error.class));
        assert value == 10;
    }

    @ExpectThrow(Error.class)
    public void recoverOnlyException() throws Exception {
        int value = I.run(unstableOperation(() -> 10, Error.class, Exception.class), I.retryWhen(Exception.class));
        assert value == 10;
    }

    @Test
    public void recoverErrorAndException() throws Exception {
        int value = I
                .run(unstableOperation(() -> 10, Error.class, Exception.class), I.retryWhen(Exception.class), I.retryWhen(Error.class));
        assert value == 10;
    }

    @Test
    public void recoverSubType() throws Exception {
        int value = I.run(unstableOperation(() -> 10, IOException.class), I.retryWhen(Exception.class));
        assert value == 10;
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
