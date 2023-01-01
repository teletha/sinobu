/*
 * Copyright (C) 2023 The SINOBU Development Team
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

import kiss.model.Model;
import kiss.model.Property;

/**
 * Annotation that is managed by some domain.
 * <p>
 * When used for any {@link Class}, it means that the instance of that {@link Class} is managed by
 * the lifestyle specified by {@link #value()}.
 * <p>
 * When used for any field, it means that the field is managed as {@link Property}. Specifying a
 * {@link Lifestyle} is meaningless.
 * 
 * @see Lifestyle
 * @see Model
 * @see Property
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Managed {

    /**
     * Configure the lifestyle of the instance which is annotated class. The default value is
     * {@link I#prototype(Class)}.
     * 
     * @see I#prototype(Class)
     * @see Singleton
     */
    Class<? extends Lifestyle> value() default Lifestyle.class;
}