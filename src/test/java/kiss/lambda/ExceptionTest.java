/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.crypto.IllegalBlockSizeException;

import org.junit.Test;

import kiss.I;
import kiss.UsefulBiConsumer;
import kiss.UsefulBiFunction;
import kiss.UsefulConsumer;
import kiss.UsefulFunction;
import kiss.UsefulRunnable;

/**
 * @version 2017/03/13 10:33:07
 */
public class ExceptionTest {

    @Test(expected = IllegalBlockSizeException.class)
    public void runnable() {
        Runnable lambda = I.quiet(this::runnableThrow);
        assert lambda != null;
        assert lambda instanceof UsefulRunnable;
        lambda.run();
    }

    private void runnableThrow() throws Exception {
        throw new IllegalBlockSizeException();
    }

    @Test(expected = IllegalBlockSizeException.class)
    public void consumer() {
        Consumer<Object> lambda = I.quiet(this::consumerThrow);
        assert lambda != null;
        assert lambda instanceof UsefulConsumer;
        lambda.accept(null);
    }

    private void consumerThrow(Object p) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @Test(expected = IllegalBlockSizeException.class)
    public void biconsumer() {
        BiConsumer<Object, Object> lambda = I.quiet(this::biconsumerThrow);
        assert lambda != null;
        assert lambda instanceof UsefulBiConsumer;
        lambda.accept(null, null);
    }

    private void biconsumerThrow(Object p, Object q) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @Test(expected = IllegalBlockSizeException.class)
    public void function() {
        Function<Object, Object> lambda = I.quiet(this::functionThrow);
        assert lambda != null;
        assert lambda instanceof UsefulFunction;
        lambda.apply(null);
    }

    private Object functionThrow(Object p) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @Test(expected = IllegalBlockSizeException.class)
    public void bifunction() {
        BiFunction<Object, Object, Object> lambda = I.quiet(this::bifunctionThrow);
        assert lambda != null;
        assert lambda instanceof UsefulBiFunction;
        lambda.apply(null, null);
    }

    private Object bifunctionThrow(Object p, Object q) throws Exception {
        throw new IllegalBlockSizeException();
    }
}
