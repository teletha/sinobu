/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @version 2013/12/27 9:59:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableAnnotationContainer.class)
public @interface RepeatableAnnotation {

    String value();
}
