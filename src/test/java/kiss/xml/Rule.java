/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Pattern matching descriptor.
 * </p>
 * 
 * @see XMLScanner
 * @version 2012/01/11 9:55:27
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {

    /**
     * <p>
     * Describe the location path you want to search. If you don't specify it, Sinobu uses the name
     * of the annotated method.
     * </p>
     * <p>
     * You can specify multiple values with whitespace separator. The following example will Match
     * both "one" element and "anothor" element.
     * </p>
     * 
     * <pre>
     * @Rule(match = "one anothor")
     * </pre>
     * 
     * @return A simple pattern value like XPath.
     */
    String match() default "";

    /**
     * Priority of this rule.
     * 
     * @return A priority.
     */
    int priority() default 0;
}
