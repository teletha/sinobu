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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Statistics implements Comparable<Statistics> {

    private final String name;

    /** The results. */
    private List<Sample> samples = new ArrayList();

    /** The sorted result. */
    private List<Sample> sorted = new ArrayList();

    /** The summary statistic. */
    private BigInteger arithmeticMean;

    /** The summary statistic. */
    private BigInteger variance;

    /** The summary statistic. */
    private double standardDeviation;

    /** The summary statistic. */
    private BigInteger median;

    /** The summary statistic. */
    private BigInteger mode;

    /**
     * 
     */
    Statistics(String name) {
        this.name = name;
    }

    /**
     * @param sample
     */
    void addSample(Sample sample) {
        samples.add(sample);
        sorted.add(sample);
    }

    void calculate() {
        // sort
        Collections.sort(sorted);

        // Prepare
        BigInteger size = BigInteger.valueOf(sorted.size());

        // Arithmetic Mean
        BigInteger sum = ZERO;

        for (Sample sample : sorted) {
            sum = sum.add(sample.timesPerExecution);
        }
        arithmeticMean = sum.divide(size);

        // Variance and Standard Deviation
        sum = ZERO;

        for (Sample sample : sorted) {
            sum = sum.add(sample.timesPerExecution.subtract(arithmeticMean).pow(2));
        }
        variance = sum.divide(size);
        standardDeviation = Math.sqrt(sum.divide(size).subtract(ONE).doubleValue());

        // Find Outlier and remove it
        Iterator<Sample> iterator = sorted.iterator();

        while (iterator.hasNext()) {
            Sample sample = iterator.next();
            sample.isOutlier = 3 < Math.abs(sample.timesPerExecution.subtract(arithmeticMean).doubleValue() / standardDeviation);

            if (sample.isOutlier) {
                iterator.remove();
            }
        }

        // Median
        int newSize = sorted.size();

        if (newSize % 2 == 1) {
            median = sorted.get((newSize + 1) / 2).timesPerExecution;
        } else {
            BigInteger one = sorted.get(newSize / 2).timesPerExecution;
            BigInteger other = sorted.get(newSize / 2 + 1).timesPerExecution;

            median = one.add(other).divide(TWO);
        }

        // Mode
        int max = 0;
        Map<BigInteger, Integer> counters = new HashMap();

        for (Sample sample : sorted) {
            Integer count = counters.get(sample.timesPerExecution);

            if (count == null) {
                count = 1;
            } else {
                count++;
            }

            if (max < count) {
                max = count;
                mode = sample.timesPerExecution;
            }
            counters.put(sample.timesPerExecution, count);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Statistics o) {
        return arithmeticMean.compareTo(o.arithmeticMean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "\t " + arithmeticMean + "ns";
    }
}