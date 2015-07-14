/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import static kiss.lambda.Lambda.*;

import java.math.BigInteger;
import java.util.function.Function;

import org.junit.Test;

/**
 * @version 2014/07/20 3:33:24
 */
public class LambdaTest {

    @Test
    public void testname() throws Exception {
        Function<BigInteger, BigInteger> fib = recursive(f -> n -> {
            if (n.intValue() <= 2) return BigInteger.ONE;
            return f.apply(n.subtract(BigInteger.ONE)).add(f.apply(n.subtract(BigInteger.valueOf(2))));
        });
        assert fib.apply(BigInteger.valueOf(10)).intValue() == 55;
    }
}
