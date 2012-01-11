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
package ezbean.xml;

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
     * Describe the location path you want to search. If you don't specify it, Ezbean uses the name
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
