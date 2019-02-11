/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.experimental;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.model.Model;
import kiss.model.Property;

/**
 * Super minimum expression language.
 */
public class Expression2 {

    private static final Pattern expression = Pattern.compile("\\{([^}]+)\\}");

    /**
     * Calculate expression language in the specified text by using the given contexts.
     * 
     * @param text A text with {some} placefolder.
     * @param models A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, Object... models) {
        StringBuilder replaced = new StringBuilder();
        Matcher matcher = expression.matcher(text);

        root: while (matcher.find()) {
            String[] expressions = matcher.group(1).split("\\.");

            if (models != null) {
                for (Object model : models) {
                    Object o = resolve(expressions, 0, model);

                    if (o != null) {
                        matcher.appendReplacement(replaced, I.transform(o, String.class));
                        continue root;
                    }
                }
            }
            matcher.appendReplacement(replaced, "");
        }
        matcher.appendTail(replaced);

        return replaced.toString();
    }

    /**
     * <p>
     * Compute the specified property variable.
     * </p>
     * 
     * @param expressions
     * @param index
     * @param value
     * @return
     */
    private static Object resolve(String[] expressions, int index, Object value) {
        if (value == null || expressions.length == index) {
            return value;
        }

        String expression = expressions[index].strip();

        Model model = Model.of(value);
        Property property = model.property(expression);

        // find in property
        if (property != null) {
            return resolve(expressions, index + 1, model.get(value, property));
        }

        // property is not found, invoke method instead
        try {
            Method method = model.type.getMethod(expression);

            if (method.getReturnType() != void.class) {
                return resolve(expressions, index + 1, method.invoke(value));
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
