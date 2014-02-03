/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * <p>
 * This lifestyle guarantees that only one instance of a specific class exists per thread.
 * </p>
 * <p>
 * This class can be used instead of {@link ThreadLocal} class like the following:
 * </p>
 * 
 * <pre>
 * private static final ThreadSpecific&lt;SimpleDateFormat&gt; formatter = new ThreadSpecific(SimpleDateFormat.class);
 * </pre>
 * <p>
 * This is equivalent to the next code.
 * </p>
 * 
 * <pre>
 * private static final ThreadLocal&lt;SimpleDateFormat&gt; formatter = new ThreadLocal&lt;SimpleDateFormat&gt;() {
 *     &#064;Override
 *     protected SimpleDateFormat initialValue() {
 *         return new SimpleDateFormat();
 *     }
 * };
 * </pre>
 * 
 * @param <M> A {@link Manageable} class.
 * @see Prototype
 * @see Singleton
 * @see Preference
 * @version 2011/11/04 0:12:03
 */
public class ThreadSpecific<M> extends Prototype<M> {

    /** The actual storage. */
    protected final ThreadLocal<M> local = new ThreadLocal();

    /**
     * Create ThreadSpecific instance.
     * 
     * @param modelClass
     */
    protected ThreadSpecific(Class<M> modelClass) {
        super(modelClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M get() {
        M object = local.get();

        if (object == null) {
            object = super.get();
            local.set(object);
        }
        return object;
    }
}
