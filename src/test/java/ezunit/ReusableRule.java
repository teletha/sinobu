/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import ezbean.model.ClassUtil;

/**
 * <p>
 * {@link ReusableRule} is replacement of the old-school super testcase class for JUnit. User can
 * override methods to intercept test method invocation.
 * </p>
 * <ul>
 * <li>{@link ReusableRule#beforeClass()}</li>
 * <li>{@link ReusableRule#before(Method)}</li>
 * <li>{@link ReusableRule#after(Method)}</li>
 * <li>{@link ReusableRule#afterClass()}</li>
 * </ul>
 * <p>
 * Testcase must define {@link ReusableRule} as static field because JUnit instantiates testcase
 * class each test method invocation. The example is following:
 * </p>
 * 
 * <pre>
 * public class SomeTest {
 * 
 *     &#064;Rule
 *     public static YourReusableRule rule = new YourReusable();
 * }
 * </pre>
 * <p>
 * The implementation of {@link ReusableRule} can contains sub rules like the following:
 * </p>
 * 
 * <pre>
 * public class YourRule extends ReusableRule {
 * 
 *     &#064;Rule
 *     public static SubRule sub = new SubRule();
 * }
 * </pre>
 * 
 * @version 2010/10/07 21:37:38
 */
public abstract class ReusableRule implements MethodRule {

    /** The testcase class. */
    protected final Class testcase = getCaller();

    /** The root directory of testcases. */
    protected final Path testcaseRoot = ClassUtil.getArchive(testcase);

    /** The parent directory of testcase class. */
    protected final Path testcaseDirectory = testcaseRoot.resolve(testcase.getPackage().getName().replace('.', '/'));

    /** The sub rules. */
    private List<Field> rules = new ArrayList();

    /** The nunber of test methods. */
    private int tests = 0;

    /** The nunber of executed test methods. */
    private int executed = 0;

    /** The current trapped error. */
    private Throwable error;

    /** The shutdown hook to invoke afterClass method when only selected test method is executed. */
    private final AfterClassInvoker invoker = new AfterClassInvoker();

    /**
     * Subclass only can instantiate.
     */
    protected ReusableRule() {
        for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(Rule.class) && Modifier.isPublic(field.getModifiers())) {
                if (MethodRule.class.isAssignableFrom(field.getType())) {
                    rules.add(field);
                }
            }
        }

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
                        } catch (Throwable e) {
                            throw error(e);
                        }
                    }

                    // check initialization error
                    if (error != null) {
                        throw error;
                    }

                    if (!skip(method.getMethod())) {
                        try {
                            // invoke before
                            before(method.getMethod());

                            // make chain of method rules
                            Statement statement = base;

                            for (Field rule : rules) {
                                statement = ((MethodRule) rule.get(ReusableRule.this)).apply(statement, method, target);
                            }

                            // invoke test method
                            statement.evaluate();
                        } catch (Throwable e) {
                            throw error(e);
                        } finally {
                            // invoke after
                            after(method.getMethod());
                        }
                    }
                } catch (Throwable e) {
                    throw error(e);
                } finally {
                    // call after class
                    if (executed == tests) {
                        // unregister
                        Runtime.getRuntime().removeShutdownHook(invoker);

                        // invoke afterClass
                        try {
                            afterClass();
                        } catch (Throwable e) {
                            throw error(e);
                        }
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
     * @throws Exception Test will be stop.
     */
    protected void beforeClass() throws Exception {
    }

    /**
     * <p>
     * This method is invoked every before all test methods.
     * </p>
     * 
     * @param method A current processing test method.
     * @throws Exception Test will be stop.
     */
    protected void before(Method method) throws Exception {
    }

    /**
     * <p>
     * Decide whether the specified method is invoked or not.
     * </p>
     * 
     * @param method A target test method.
     * @return A result.
     */
    protected boolean skip(Method method) {
        return false;
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
     * Helper method to throw error user-friendly.
     * </p>
     * 
     * @param throwable A current caught error.
     * @return A root error.
     */
    private Throwable error(Throwable throwable) {
        if (error == null) {
            error = throwable;
        } else {
            error.addSuppressed(throwable);
        }
        return error;
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
