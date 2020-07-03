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

public class Sample implements Comparable<Sample> {

    /** The frequency of measurement. */
    final BigInteger frequency;

    /** The measurement time. */
    final BigInteger time;

    /** The measurement time per one execution of the specified task. */
    final BigInteger timesPerExecution;

    /** The number of task executions per one second. */
    final BigInteger executionsPerSecond;

    /** The check sum. */
    final int hash;

    /** The state. */
    boolean isOutlier = false;

    /***
     * Create MeasurementResult instance.
     * 
     * @param frequency
     * @param time
     */
    Sample(BigInteger frequency, long time, int hash) {
        this(frequency, new BigInteger(String.valueOf(time)), hash);
    }

    /**
     * Create MeasurementResult instance.
     * 
     * @param frequency
     * @param time
     */
    Sample(BigInteger frequency, BigInteger time, int hash) {
        this.frequency = frequency;
        this.time = time;
        this.hash = hash;
        this.timesPerExecution = (frequency.equals(ZERO)) ? ZERO : time.divide(frequency);
        this.executionsPerSecond = (time.equals(ZERO)) ? ZERO : frequency.multiply(Benchmark.G).divide(time);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat();

        StringBuilder builder = new StringBuilder();
        builder.append(format.format(time.divide(Benchmark.M)));
        builder.append("ms  \t");
        builder.append(format.format(executionsPerSecond));
        builder.append("call/s \t");
        builder.append(format.format(timesPerExecution));
        builder.append("ns/call");

        if (isOutlier) {
            builder.append("   ☠");
        }

        return builder.toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Sample o) {
        return timesPerExecution.compareTo(o.timesPerExecution);
    }
}
