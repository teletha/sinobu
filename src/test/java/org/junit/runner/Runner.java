/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package org.junit.runner;

import org.junit.runner.notification.RunNotifier;

/**
 * <p>
 * Replace Runner class by forcing JVM to load this class before loading the legitimate Runner
 * class.
 * </p>
 * 
 * @version 2012/01/12 16:03:19
 */
public abstract class Runner implements Describable {

    static {
        // Hook point for Junit.
    }

    /**
     * Run the tests for this runner.
     * 
     * @param notifier will be notified of events while tests are being run--tests being started,
     *            finishing, and failing
     */
    public abstract void run(RunNotifier notifier);

    /**
     * @return the number of tests to be run by the receiver
     */
    public int testCount() {
        return getDescription().testCount();
    }
}
