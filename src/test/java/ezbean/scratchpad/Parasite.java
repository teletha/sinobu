/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.scratchpad;

import java.lang.reflect.Field;

import ezbean.I;
import ezbean.Prototype;

/**
 * Problem : The service provider class should know the detail of host class implementation. This
 * blocks loose coupling.
 * <p>
 * Manageable Annotation must change.
 * </p>
 * 
 * <pre>
 * public @interface Manageable {
 * 
 *     Class&lt;?&gt; lifestyle() default Prototype.class;
 * }
 * </pre>
 * 
 * @version 2008/05/30 22:01:28
 */
public class Parasite<M> extends Prototype<M> {

    /** The host class for this lifestyle. */
    private final Class host;

    /** The host location. */
    private Field field;

    /**
     * Create Parasite instance.
     * 
     * @param modelClass
     * @param hostClass
     */
    public Parasite(Class modelClass, Class hostClass) {
        super(modelClass);

        // decide host class
        host = hostClass;

        // decide host location
        for (Field field : host.getDeclaredFields()) {
            if (field.getType() == modelClass) {
                // make this field accessible fast
                field.setAccessible(true);

                // assign
                this.field = field;
                break;
            }
        }
    }

    /**
     * @see ezbean.Prototype#resolve()
     */
    @Override
    public M resolve() {
        try {
            return (M) field.get(I.make(host));
        } catch (Exception e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }
}
