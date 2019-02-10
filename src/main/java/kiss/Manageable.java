/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DOCUMENT.
 * 
 * @see Lifestyle
 * @version 2008/12/07 08:47:29
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manageable {

    /**
     * Configure the lifestyle of the instance which is annotated class. The default value is
     * {@link Prototype}.
     * 
     * @see Prototype
     */
    Class<? extends Lifestyle> lifestyle() default Prototype.class;
}
