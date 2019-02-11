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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.Signal;
import kiss.WiseTriFunction;
import kiss.model.Model;
import kiss.model.Property;

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
     * @param models A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, List models, WiseTriFunction<Model, Object, Property, Object>... resolvers) {
        Signal<WiseTriFunction<Model, Object, Property, Object>> signal = I.signal(resolvers);
        signal.startWith(Model::get);
        resolvers = I.array(resolvers, Model::get);

        StringBuilder replaced = new StringBuilder();
        Matcher matcher = expression.matcher(text);

        root: while (matcher.find()) {
            String[] expressions = matcher.group(1).split("\\.");

            model: for (Object o : models) {
                for (int i = 0; i < expressions.length; i++) {
                    if (o == null) {
                        continue model;
                    }

                    String expression = expressions[i].strip();
                    Model model = Model.of(o);
                    Property property = model.property(expression);
                    if (property == null) property = new Property(model, expression);

                    for (WiseTriFunction resolver : resolvers) {
                        o = resolver.apply(model, o, property);

                        if (o != null) {
                            break;
                        }
                    }
                }

                if (o != null) {
                    matcher.appendReplacement(replaced, I.transform(o, String.class));
                    continue root;
                }
            }
            matcher.appendReplacement(replaced, "");
        }
        matcher.appendTail(replaced);

        return replaced.toString();
    }
}
