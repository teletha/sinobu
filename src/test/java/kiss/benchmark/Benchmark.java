/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.benchmark;

import static java.math.BigInteger.*;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.TreeSet;

import kiss.I;

public class Benchmark {

    /** 2 */
    static final BigInteger TWO = new BigInteger("2");

    /** 1,000 */
    static final BigInteger K = new BigInteger("1000");

    /** 1,000,000 */
    static final BigInteger M = new BigInteger("1000000");

    /** 1,000,000,000 */
    static final BigInteger G = new BigInteger("1000000000");

    /** The number of trial. */
    private final int trials;

    /** The threshold of measurement time. (unit: ns) */
    private final BigInteger threshold = new BigInteger("1").multiply(G);

    private final TreeSet<Statistics> statistics = new TreeSet();

    /**
     * Create Benchmark instance.
     */
    public Benchmark() {
        this(5);
    }

    /**
     * Create Benchmark instance.
     * 
     * @param trials A number of trial.
     */
    public Benchmark(int trials) {
        this.trials = trials;

        if (trials < 5) {
            throw new AssertionError("There is too few trial number of times. (minimus is 5)");
        }

        if (60 < trials) {
            throw new AssertionError("There is too many trial number of times. (maximum is 60)");
        }
    }

    /**
     * <p>
     * Measure an execution speed of the specified code fragment.
     * </p>
     * 
     * @param measuredCode A code to be measured.
     */
    public void measure(String name, Code measuredCode) {
        write("<<<<<<<<<<  ", name, "  >>>>>>>>>>\n");

        Sample first = measure(measuredCode, ONE);

        if (first.hash == 0) throw new Error("Benckmark task must return not null but something.");
        if (first.time.compareTo(threshold) != -1) throw new Error("Benchmark task must be able to execute within 1 second.");

        write("Warmup JVM");

        // warmup JVM and decided the number of executions
        BigInteger frequency = ONE;

        while (true) {
            Sample result = measure(measuredCode, frequency);

            if (result.time.compareTo(threshold) == -1) {
                frequency = frequency.multiply(TWO);
            } else {
                frequency = frequency.multiply(G).divide(result.time);
                break;
            }
            write("..");
        }
        write("\r");

        // measure actually
        Statistics statistics = new Statistics(name);
        DecimalFormat counterFormat = new DecimalFormat("00");

        for (int i = 0; i < this.trials; i++) {
            Sample result = measure(measuredCode, frequency);

            // save
            statistics.addSample(result);

            // display for user
            write(counterFormat.format(i + 1), " : ", result, "\n");
        }

        // report
        statistics.calculate();
        this.statistics.add(statistics);
    }

    /**
     * Measures the execution time of <code>frequency</code> calls of the specified task.
     */
    private Sample measure(Code code, BigInteger frequency) {
        int hash = 0;

        try {
            // measure actually
            long start = System.nanoTime();
            for (long i = frequency.longValue(); 0 < i; i--) {
                hash ^= code.measure().hashCode(); // prevent dead-code-elimination
            }
            long end = System.nanoTime();

            // calculate execution time
            return new Sample(frequency, end - start, hash);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    public void show() {
        for (Statistics stat : statistics) {
            System.out.println(stat);
        }
    }

    /**
     * <p>
     * Helper method to write conosle message.
     * </p>
     * 
     * @param messages
     */
    private void write(Object... messages) {
        StringBuilder builder = new StringBuilder();

        for (Object message : messages) {
            builder.append(message);
        }

        System.out.print(builder);
    }

    /**
     * <p>
     * </p>
     * 
     * @version 2012/01/30 10:56:30
     */
    public static interface Code {

        /**
         * <p>
         * Write micro benchmark code.
         * </p>
         * 
         * @return A result of computation.
         * @throws Throwable
         */
        Object measure() throws Throwable;
    }
}
