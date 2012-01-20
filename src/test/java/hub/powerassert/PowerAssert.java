/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

import hub.bytecode.Agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * <p>
 * Enhance assertion message in testcase.
 * </p>
 * <p>
 * {@link PowerAssert} is declared implicitly, so you don't use this class except for testing
 * {@link PowerAssert} itself. Implicit declaration is achived by class loading replacement. You
 * have to add this jar file in classpath before the JUnit jar file.
 * </p>
 * <p>
 * Using {@link PowerAssertOff} annotation, you can stop {@link PowerAssert}'s functionality for
 * each test classes or testcase methods.
 * </p>
 * 
 * @version 2012/01/19 11:50:38
 */
public class PowerAssert implements TestRule {

    /** The recode for the translated classes. */
    private static final Set<String> translated = new CopyOnWriteArraySet();

    /** The actual translator. */
    private static final Agent agent = new Agent(PowerAssertTranslator.class);

    /** The self tester. */
    private final PowerAssertTester tester;

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this(null);
    }

    /**
     * Test for {@link PowerAssert}.
     */
    PowerAssert(PowerAssertTester tester) {
        this.tester = tester;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (Throwable error) {
                    if (error instanceof AssertionError) {
                        // should we print this error message in detal?
                        if (description.getAnnotation(PowerAssertOff.class) == null && !description.getTestClass()
                                .isAnnotationPresent(PowerAssertOff.class)) {
                            // translate assertion code only once
                            if (translated.add(description.getClassName())) {
                                agent.transform(description.getTestClass());

                                evaluate(); // retry testcase
                                return;
                            }

                            // retrieve context
                            PowerAssertContext context = PowerAssertContext.get();

                            if (tester != null) {
                                // for sel test
                                tester.validate(context);
                                return;
                            }

                            // replace error message
                            String message = error.getLocalizedMessage();

                            if (message == null) {
                                message = "";
                            }

                            List<StackTraceElement> elements = new ArrayList();
                            elements.addAll(Arrays.asList(error.getStackTrace()));

                            elements.add(0, new StackTraceElement("a", "test\n", "", -1));

                            error = new AssertionError(message + "\r\n" + context);
                            error.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));
                        }
                    }
                    throw error; // rethrow
                }
            }
        };
    }
}
