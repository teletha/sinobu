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
package ezunit;

import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import ezbean.I;

/**
 * @version 2010/01/08 2:19:31
 */
public abstract class EzRule implements MethodRule {

    /** The testcase class. */
    protected final Class testcase = getCaller();

    /** The nunber of test methods. */
    private int tests = 0;

    /** The nunber of executed test methods. */
    private int executed = 0;

    /** The initialization error holder. */
    private Throwable initializationError;

    /** The shutdown hook to invoke afterClass method when only selected test method is executed. */
    private final AfterClassInvoker invoker = new AfterClassInvoker();

    /**
     * 
     */
    protected EzRule() {
        for (Method method : testcase.getMethods()) {
            if (method.isAnnotationPresent(Test.class) && !method.isAnnotationPresent(Ignore.class)) {
                tests++;
            }
        }
    }

    /**
     * @see org.junit.rules.MethodRule#apply(org.junit.runners.model.Statement,
     *      org.junit.runners.model.FrameworkMethod, java.lang.Object)
     */
    public final Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            /**
             * @see org.junit.runners.model.Statement#evaluate()
             */
            public void evaluate() throws Throwable {
                try {
                    // call before class
                    if (executed++ == 0) {
                        try {
                            // register
                            Runtime.getRuntime().addShutdownHook(invoker);

                            // invoke beforeClass
                            beforeClass();
                        } catch (Exception e) {
                            throw I.quiet(initializationError = e);
                        }
                    }

                    // check initialization error
                    if (initializationError != null) {
                        throw I.quiet(initializationError);
                    }

                    try {
                        // invoke before
                        before(method.getMethod());

                        // invoke test method
                        base.evaluate();
                    } catch (Exception e) {
                        throw I.quiet(e);
                    } finally {
                        // invoke after
                        after(method.getMethod());
                    }
                } finally {
                    // call before class
                    if (executed == tests) {
                        // unregister
                        Runtime.getRuntime().removeShutdownHook(invoker);

                        // invoke afterClass
                        afterClass();
                    }
                }
            }
        };
    }

    /**
     * <p>
     * This method is invoked once before all test methods.
     * </p>
     * 
     * @throws Exception
     */
    protected void beforeClass() throws Exception {
    }

    /**
     * <p>
     * This method is invoked every before all test methods.
     * </p>
     * 
     * @param method A current processing test method.
     * @throws Exception
     */
    protected void before(Method method) throws Exception {
    }

    /**
     * <p>
     * This method is invoked every after all test methods.
     * </p>
     * 
     * @param method A current processing test method.
     */
    protected void after(Method method) {
    }

    /**
     * <p>
     * This method is invoked once after all test methods.
     * </p>
     */
    protected void afterClass() {
    }

    /**
     * <p>
     * Search caller testcase class.
     * </p>
     * 
     * @return A testcase class.
     */
    private static final Class getCaller() {
        // caller
        Exception e = new Exception();
        StackTraceElement[] elements = e.getStackTrace();

        for (StackTraceElement element : elements) {
            String name = element.getClassName();

            if (name.endsWith("Test")) {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException classNotFoundException) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(classNotFoundException);
                }
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error("Testcase is not found.");
    }

    /**
     * @version 2010/01/08 2:11:08
     */
    private final class AfterClassInvoker extends Thread {

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            afterClass();
        }
    }
}
