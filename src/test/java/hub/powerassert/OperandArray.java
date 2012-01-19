/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @version 2012/01/19 16:18:02
 */
public class OperandArray extends Operand {

    private final String className;

    private final Object value;

    private final List<Operand> values = new ArrayList();

    /**
     * @param name
     * @param value
     */
    public OperandArray(String name, Object value) {
        super(name, value);

        this.className = name;
        this.value = value;
    }

    /**
     * @see hub.powerassert.Operand#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("new ");
        builder.append(className).append("[] {");

        Iterator<Operand> iterator = values.iterator();

        if (iterator.hasNext()) {
            builder.append(iterator.next());

            while (iterator.hasNext()) {
                builder.append(", ").append(iterator.next());
            }
        }

        builder.append('}');
        return builder.toString();
    }

    public void addValue(Operand operand) {
        values.add(operand);
    }

    /**
     * @see hub.powerassert.Operand#toValueExpression()
     */
    @Override
    public String toValueExpression() {
        switch (value.getClass().getComponentType().getSimpleName()) {
        case "int":
            return Arrays.toString((int[]) value);

        case "long":
            return Arrays.toString((long[]) value);

        case "float":
            return Arrays.toString((float[]) value);

        case "double":
            return Arrays.toString((double[]) value);

        case "char":
            return Arrays.toString((char[]) value);

        case "boolean":
            return Arrays.toString((boolean[]) value);

        case "short":
            return Arrays.toString((short[]) value);

        case "byte":
            return Arrays.toString((byte[]) value);

        default:
            return Arrays.toString((Object[]) value);
        }
    }
}
