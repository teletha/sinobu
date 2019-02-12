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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.WiseTriFunction;
import kiss.model.Model;

/**
 * Super minimum expression language.
 */
public class Expression {

    /** The expression syntax. */
    private static final Pattern expression = Pattern.compile("\\{([^}]+)\\}");

    /**
     * Calculate expression language in the specified text by using the given contexts.
     * 
     * @param text A text with {some} placefolder.
     * @param contexts A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, Object... contexts) {
        return express(text, contexts, (WiseTriFunction[]) null);
    }

    /**
     * Calculate expression language in the specified text by using the given contexts.
     * 
     * @param text A text with {some} placefolder.
     * @param contexts A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, Object[] contexts, WiseTriFunction<Model, Object, String, Object>... resolvers) {
        resolvers = I.array(new WiseTriFunction[] {(WiseTriFunction<Model, Object, String, Object>) Model::get}, resolvers);

        StringBuilder str = new StringBuilder();

        // find all expression placeholder
        Matcher matcher = expression.matcher(text);

        nextPlaceholder: while (matcher.find()) {
            // normalize expression (remove all white space) and split it
            String[] e = matcher.group(1).replaceAll("[\\s　]", "").split("\\.");

            // evaluate each model (first model has high priority)
            nextContext: for (int i = 0; i < contexts.length; i++) {
                Object c = contexts[i];

                // evaluate expression from head
                nextExpression: for (int j = 0; j < e.length; j++) {
                    Model m = Model.of(c);

                    // evaluate expression by each resolvers
                    for (int k = 0; k < resolvers.length; k++) {
                        Object o = resolvers[k].apply(m, c, e[j]);

                        if (o != null) {
                            // suitable value was found, step into next expression
                            c = o;
                            continue nextExpression;
                        }
                    }

                    // any resolver can't find suitable value, try to next context
                    continue nextContext;
                }

                // full expression was evaluated collectly, convert it to string
                matcher.appendReplacement(str, I.transform(c, String.class));

                continue nextPlaceholder;
            }

            // any context can't find suitable value, so use empty text
            matcher.appendReplacement(str, "");
        }
        matcher.appendTail(str);

        return str.toString();
    }
}
