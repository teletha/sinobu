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

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.objectweb.asm.Type;

/**
 * @version 2012/01/11 11:27:35
 */
@Manageable(lifestyle = ThreadSpecific.class)
public class PowerAssertContext implements Recoder {

    /** The local variable name mapping. */
    static final Map<Integer, String[]> localVariables = new ConcurrentHashMap();

    /** The operand stack frame. */
    ArrayDeque<Operand> stack = new ArrayDeque();

    /** The using operand list. */
    ArrayList<Operand> operands = new ArrayList();

    /** The incremetn state. */
    private String nextIncrement;

    /**
     * {@inheritDoc}
     */
    @Override
    public void constant(Object constant) {
        stack.add(new Operand(constant));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void localVariable(int id, Object variable) {
        Operand operand;
        String[] localVariable = localVariables.get(id);
        String name = localVariable[0];

        if (nextIncrement != null) {
            name = nextIncrement.concat(name);

            nextIncrement = null;
        }

        if (localVariable[1].equals("Z")) {
            operand = new Operand(localVariable[0], (int) variable == 1);
        } else {
            operand = new Operand(localVariable[0], variable);
        }

        stack.add(new Operand(name, null));
        operands.add(operand);
    }

    /**
     * @see hub.powerassert.Recoder#operator(java.lang.String)
     */
    @Override
    public void operator(String operator) {
        if (1 < stack.size()) {
            Operand right = stack.pollLast();
            Operand left = stack.pollLast();

            if (operator.equals("==") || operator.equals("!=")) {
                // check operands
                if (right.value instanceof Integer && ((Integer) right.value).intValue() == 0 && left.value instanceof Boolean) {

                    // boolean == 0 or boolean != 0
                    stack.add(left);
                    return;
                }
            }
            stack.add(new Operand(left + " " + operator + " " + right, null));
        }
    }

    /**
     * @see hub.powerassert.PowerAssert.Increment#recodeIncrement(int, int)
     */
    @Override
    public void recodeIncrement(int id, int increment) {
        String[] localVariable = localVariables.get(id);
        Operand latest = stack.peekLast();

        if (latest == null || !latest.name.equals(localVariable[0])) {
            // pre increment
            switch (increment) {
            case 1:
                nextIncrement = "++";
                break;

            case -1:
                nextIncrement = "--";
                break;
            }
        } else {
            // post increment
            switch (increment) {
            case 1:
                stack.add(new Operand(stack.pollLast() + "++", null));
                break;

            case -1:
                stack.add(new Operand(stack.pollLast() + "--", null));
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void negative() {
        stack.add(new Operand("-" + stack.pollLast(), null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instanceOf(String className) {
        stack.add(new Operand(stack.pollLast() + " instanceof " + className, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void field(String expression, Object variable) {
        Operand operand = new Operand(stack.pollLast() + "." + expression, variable);
        stack.add(operand);
        operands.add(operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void staticField(String expression, Object variable) {
        Operand operand = new Operand(expression, variable);
        stack.add(operand);
        operands.add(operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void constructor(String name, String description, Object value) {
        // build method invocation
        StringBuilder invocation = new StringBuilder("()");
        Type[] params = Type.getMethodType(description).getArgumentTypes();
        int size = params.length;

        for (int i = 0; i < size; i++) {
            Type type = params[i];
            Operand operand = stack.pollLast();

            if (type.getSort() == Type.BOOLEAN && operand.value instanceof Integer) {
                // format
                operand = new Operand(Boolean.valueOf(operand.value.toString()));
            }
            invocation.insert(1, operand);

            if (i + 1 != size) {
                invocation.insert(1, ", ");
            }
        }
        invocation.insert(0, name).insert(0, "new ");

        Operand operand = new Operand(invocation.toString(), value);
        stack.add(operand);
        operands.add(operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void method(String name, String description, Object value) {
        // build method invocation
        OperandMethod method = new OperandMethod(name, description, value);

        stack.add(method);
        operands.add(method);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void staticMethod(String name, String description, Object value) {
        // build method invocation
        StringBuilder invocation = new StringBuilder("()");
        Type[] params = Type.getMethodType(description).getArgumentTypes();
        int size = params.length;

        for (int i = 0; i < size; i++) {
            Type type = params[i];
            Operand operand = stack.pollLast();

            if (type.getSort() == Type.BOOLEAN && operand.value instanceof Integer) {
                // format
                operand = new Operand(Boolean.valueOf(operand.value.toString()));
            }
            invocation.insert(1, operand);

            if (i + 1 != size) {
                invocation.insert(1, ", ");
            }
        }
        invocation.insert(0, name);

        Operand operand = new Operand(invocation.toString(), value);
        stack.add(operand);
        operands.add(operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void arrayIndex(Object value) {
        Operand index = stack.pollLast();
        Operand operand = new Operand(stack.pollLast() + "[" + index + "]", value);

        stack.add(operand);
        operands.add(operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void arrayNew(String className, Object value) {
        // remove previous array size constant
        stack.pollLast();

        Operand operand = new OperandArray(className, value);
        stack.add(operand);
        operands.add(operand);
    }

    /**
     * @see hub.powerassert.Recoder#arrayStore()
     */
    @Override
    public void arrayStore() {
        // remove previous two operand
        Operand value = stack.pollLast(); // value
        Operand index = stack.pollLast(); // index
        OperandArray array = (OperandArray) stack.peekLast();

        array.add((Integer) index.value, value);
    }

    /**
     * @see hub.powerassert.Recoder#clear()
     */
    public void clear() {
        stack.clear();
        operands.clear();
        nextIncrement = null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("assert ");
        builder.append(stack.peek()).append("\r\n");

        for (Operand operand : operands) {
            builder.append("\r\n").append(operand).append(" : ").append(operand.toValueExpression());
        }
        return builder.toString();
    }

    /**
     * <p>
     * Retrieve thread specific context.
     * </p>
     * 
     * @return
     */
    public static PowerAssertContext get() {
        return I.make(PowerAssertContext.class);
    }

    /**
     * @version 2012/01/19 16:18:02
     */
    private class OperandArray extends Operand {

        private final String className;

        private final Object value;

        private final Class type;

        private final List<Operand> values = new ArrayList();

        /**
         * @param name
         * @param value
         */
        private OperandArray(String name, Object value) {
            super(name, value);

            this.className = name;
            this.value = value;
            this.type = value.getClass().getComponentType();

            // Boolean array is initialized with false values, other type arrays are initialized
            // with null values. Array store operation will be invoked array length times but false
            // value will not be invoked. So we should fill with false values to normalize setup.
            int size = Array.getLength(value);

            for (int i = 0; i < size; i++) {
                values.add(new Operand(false));
            }
        }

        /**
         * <p>
         * Add value.
         * </p>
         * 
         * @param operand
         */
        private void add(int index, Operand operand) {
            if (type == boolean.class && operand.value instanceof Integer) {
                values.set(index, new Operand(((Integer) operand.value).intValue() == 1));
            } else {
                values.set(index, operand);
            }
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

    /**
     * @version 2012/01/19 16:58:26
     */
    private class OperandMethod extends Operand {

        private final Operand invoker;

        private final String methodName;

        private final Type[] parameterTypes;

        private final List<Operand> parameters = new ArrayList();

        /**
         * @param name
         * @param value
         */
        private OperandMethod(String name, String description, Object value) {
            super(name, value);

            this.methodName = name;
            this.parameterTypes = Type.getMethodType(description).getArgumentTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                Operand operand = stack.pollLast();

                if (parameterTypes[i] == Type.BOOLEAN_TYPE && operand.value instanceof Integer) {
                    parameters.add(0, new Operand(((Integer) operand.value).intValue() == 1));
                } else {
                    parameters.add(0, operand);
                }
            }
            invoker = stack.pollLast();
        }

        /**
         * @see hub.powerassert.Operand#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (!invoker.toString().equals("this")) {
                builder.append(invoker).append('.');
            }
            builder.append(methodName).append('(');

            for (int i = 0; i < parameterTypes.length; i++) {
                Type type = parameterTypes[i];
                Operand value = parameters.get(i);

                if (i + 1 == parameterTypes.length && type.getSort() == Type.ARRAY && value instanceof OperandArray) {
                    // for varargs
                    OperandArray array = (OperandArray) value;

                    Iterator<Operand> iterator = array.values.iterator();

                    if (iterator.hasNext()) {
                        builder.append(iterator.next());

                        while (iterator.hasNext()) {
                            builder.append(", ").append(iterator.next());
                        }
                    }
                } else {
                    builder.append(value);
                }

                if (i + 1 != parameterTypes.length) {
                    builder.append(", ");
                }
            }
            builder.append(')');

            return builder.toString();
        }

        /**
         * @see hub.powerassert.Operand#toValueExpression()
         */
        @Override
        public String toValueExpression() {
            return super.toValueExpression();
        }
    }
}