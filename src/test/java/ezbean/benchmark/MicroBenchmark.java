/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.benchmark;

import static java.math.BigInteger.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import ezbean.I;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/04 20:53:30
 */
public final class MicroBenchmark<T> {

    /** 2 */
    static final BigInteger TWO = new BigInteger("2");

    /** 1,000 */
    static final BigInteger K = new BigInteger("1000");

    /** 1,000,000 */
    static final BigInteger M = new BigInteger("1000000");

    /** 1,000,000,000 */
    static final BigInteger G = new BigInteger("1000000000");

    /** The task to estimate execution time. */
    private final Callable<T> task;

    /** The frequency of measurement. */
    private final int frequency;

    /** The time of JVM warmup. (unit: second) */
    private final int warmup;

    /** The threshold of measurement time. (unit: ns) */
    private final BigInteger threshold = new BigInteger("1").multiply(G);

    /**
     * Create Benchmark instance.
     * 
     * @param task A task to benchmark.
     * @param frequency A frequency of measurement.
     */
    public MicroBenchmark(Callable<T> task) {
        this(task, 30);
    }

    /**
     * Create Benchmark instance.
     * 
     * @param task A task to benchmark.
     * @param frequency A frequency of measurement.
     */
    public MicroBenchmark(Callable<T> task, int frequency) {
        this(task, frequency, 5);
    }

    /**
     * Create Benchmark instance.
     * 
     * @param task A task to benchmark.
     * @param frequency A frequency of measurement.
     * @param warmup A time of JVM warmup. (unit: second)
     */
    public MicroBenchmark(Callable<T> task, int frequency, int warmup) {
        this.task = task;
        this.frequency = frequency;
        this.warmup = warmup;

        if (frequency < 10) {
            fail("There is too few measurement number of times. (minimus is 10)");
        }

        if (100 <= frequency) {
            fail("There is too many measurement number of times. (maximum is 99)");
        }
    }

    /**
     * Execute benchmarck test and report it.
     */
    public MeasurementResult execute() {
        MeasurementResult first = measure(1);
        assertNotNull("Benckmark task must return not null but something.", first.hash);
        assertEquals("Benchmark task must be able to execute within 1 second.", -1, first.time.compareTo(threshold));

        // warmup JVM
        System.out.println("JVM is warming up now...");

        for (long count = 1, start = System.currentTimeMillis(); System.currentTimeMillis() - start < 1000 * warmup; count *= 2) {
            measure(count);
        }

        // decided the number of executions
        BigInteger frequency = ONE;

        while (true) {
            MeasurementResult result = measure(frequency.longValue());

            if (result.time.compareTo(threshold) == -1) {
                frequency = frequency.multiply(TWO);
            } else {
                frequency = frequency.multiply(G).divide(result.time);
                break;
            }
        }

        // measure actually
        DecimalFormat counterFormat = new DecimalFormat("00");

        MeasurementResult[] results = new MeasurementResult[this.frequency];

        for (int i = 0; i < this.frequency; i++) {
            results[i] = measure(frequency.longValue());
            System.out.println(counterFormat.format(i + 1) + " : " + results[i]);
        }

        // prevent dead-code-elimination

        // report
        MeasurementResult total = new MeasurementResult(0, 0, 0);

        for (MeasurementResult result : results) {
            total = total.add(result);
        }
        System.out.println("sum : " + total);
        return total;
    }

    /**
     * Measures the execution time of <code>frequency</code> calls of the specified task.
     */
    private MeasurementResult measure(long frequency) {
        try {
            // reduce dead-code-elimination
            int hash = 0;

            // measure actually
            long start = System.nanoTime();
            for (long i = 0; i < frequency; i++) {
                hash ^= task.call().hashCode();
            }
            long end = System.nanoTime();

            // calculate execution time
            return new MeasurementResult(frequency, end - start, hash);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
