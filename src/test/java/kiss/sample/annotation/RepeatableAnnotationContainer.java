/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @version 2014/07/12 11:41:03
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatableAnnotationContainer {

    RepeatableAnnotation[] value();
}