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
        stack.add(new OperandConstant(constant));
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

        switch (localVariable[1]) {
        case "Z": // boolean
            operand = new Operand(localVariable[0], (int) variable == 1);
            break;

        case "C": // char
            operand = new Operand(localVariable[0], (char) ((Integer) variable).intValue());
            break;

        default:
            operand = new Operand(localVariable[0], variable);
            break;
        }

        stack.add(new Operand(name, operand.value));
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
     * {@inheritDoc}
     */
    @Override
    public void condition(String contionalExpression) {
        OperandCondition condition = new OperandCondition(contionalExpression, null);
        stack.add(condition);
    }

    /**
     * @see hub.powerassert.PowerAssert.Increment#recodeIncrement(int, int)
     */
    @Override
    public void recodeIncrement(int id, int increment) {
        String[] localVariable = localVariables.get(id);
        Operand latest = stack.peekLast();

        if (latest == null || !latest.toString().equals(localVariable[0])) {
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
                operand = new OperandConstant(Boolean.valueOf(operand.value.toString()));
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
    public void staticMethod(String className, String methodName, String description, Object value) {
        // build method invocation
        OperandMethod method = new OperandMethod(new OperandConstant(className, false), methodName, description, value);

        stack.add(method);
        operands.add(method);
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

        // top level operand must be conditional operand because of assert statement
        OperandCondition result = (OperandCondition) stack.peekLast();

        // write assertion code
        builder.append(result).append("\n");

        // search top level operand
        Operand top = result;

        if (result.right.value instanceof Integer && result.left.value instanceof Boolean) {
            top = result.left;
        }

        // collect all variable operans
        List<Operand> variables = new ArrayList();

        for (Operand operand : operands) {
            if (operand.isVariableHolder() && operand != top) {
                variables.add(operand);
            }
        }

        Iterator<Operand> iterator = variables.iterator();
        builder.append("┌─────────────────────────────────────────\n");

        if (iterator.hasNext()) {
            render(builder, iterator.next());

            while (iterator.hasNext()) {
                builder.append("├─────────────────────────────────────────\n");
                render(builder, iterator.next());
            }
        }
        builder.append("└─────────────────────────────────────────\n");

        return builder.toString();
    }

    private void render(StringBuilder builder, Operand operand) {
        Object value = operand.value;
        String name = operand.toString();
        String[] lines = operand.toValueExpression().split("\r\n|\r|\n");

        builder.append("│").append(name);

        if (value != null && !value.getClass().isPrimitive()) {
            builder.append("　　　　#")
                    .append(value.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(System.identityHashCode(value)))
                    .append("");
        }
        builder.append("\n");

        for (String line : lines) {
            builder.append("│　　").append(line).append("\n");
        }
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
     * @version 2012/01/20 13:17:56
     */
    private static class OperandConstant extends Operand {

        /**
         * @param value
         */
        private OperandConstant(Object value) {
            this(value, true);
        }

        /**
         * @param value
         */
        private OperandConstant(Object value, boolean decorate) {
            super(decorate ? getDisplayName(value) : String.valueOf(value), value);
        }

        private static String getDisplayName(Object value) {
            if (value instanceof String) {
                return "\"" + value + "\"";
            }

            if (value instanceof Class) {
                return ((Class) value).getSimpleName() + ".class";
            }

            return String.valueOf(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        boolean isVariableHolder() {
            return false;
        }
    }

    /**
     * @version 2012/01/20 17:43:57
     */
    private class OperandCondition extends Operand {

        private final Operand left;

        private final Operand right;

        private final String expression;

        /**
         * @param expression
         * @param value
         */
        private OperandCondition(String expression, Object value) {
            super(expression, value);

            this.expression = expression;
            this.right = stack.pollLast();
            this.left = stack.pollLast();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            // Integer value represents various types (int, char and boolean).
            // We have to check the opposite term' type to infer its actual type.
            if (left.value instanceof Integer) {
                if (right.value instanceof Boolean) {
                    return right.toString();
                } else if (right.value instanceof Character) {
                    return convert(left, char.class) + " " + expression + " " + right;
                }
            }

            if (right.value instanceof Integer) {
                if (left.value instanceof Boolean) {
                    return left.toString();
                } else if (left.value instanceof Character) {
                    return left + " " + expression + " " + convert(right, char.class);
                }
            }
            return left + " " + expression + " " + right;
        }

    }

    /**
     * @version 2012/01/19 16:18:02
     */
    private class OperandArray extends Operand {

        private final String className;

        private final Class type;

        private final int size;

        private final List<Operand> values = new ArrayList();

        /**
         * @param name
         * @param value
         */
        private OperandArray(String name, Object value) {
            super(name, value);

            this.className = name;
            this.type = value.getClass().getComponentType();

            // Boolean array is initialized with false values, other type arrays are initialized
            // with null values. Array store operation will be invoked array length times but false
            // value will not be invoked. So we should fill with false values to normalize setup.
            this.size = Array.getLength(value);

            for (int i = 0; i < size; i++) {
                values.add(new OperandConstant(false));
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
            // Integer value represents various types (int, char and boolean).
            // We have to check the array type to infer its actual type.
            if (operand.value instanceof Integer) {
                if (type == boolean.class) {
                    values.set(index, new OperandConstant(convert(operand, boolean.class), false));
                } else if (type == char.class) {
                    values.set(index, new OperandConstant(convert(operand, char.class), false));
                } else {
                    values.set(index, operand);
                }
            } else {
                values.set(index, operand);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        boolean isVariableHolder() {
            return false;
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
         * <p>
         * Normal method invocation.
         * </p>
         * 
         * @param name A method name.
         * @param description A method description.
         * @param value A method result.
         */
        private OperandMethod(String name, String description, Object value) {
            this(null, name, description, value);
        }

        /**
         * <p>
         * Repesents method invocation.
         * </p>
         * 
         * @param invoker A method invoker.
         * @param name A method name.
         * @param description A method description.
         * @param value A method result.
         */
        private OperandMethod(Operand invoker, String name, String description, Object value) {
            super(name, value);

            this.methodName = name;
            this.parameterTypes = Type.getMethodType(description).getArgumentTypes();

            int size = parameterTypes.length - 1;

            for (int i = size; 0 <= i; i--) {
                Operand operand = stack.pollLast();

                // Integer value represents various types (int, char and boolean).
                // We have to check the parameter type to infer its actual type.
                if (operand.value instanceof Integer) {
                    switch (parameterTypes[i].getSort()) {
                    case Type.BOOLEAN:
                        parameters.add(0, new OperandConstant(convert(operand, boolean.class), false));
                        break;

                    case Type.CHAR:
                        parameters.add(0, new OperandConstant(convert(operand, char.class), false));
                        break;

                    default:
                        parameters.add(0, operand);
                        break;
                    }
                } else {
                    parameters.add(0, operand);
                }
            }

            this.invoker = invoker != null ? invoker : stack.pollLast();
        }

        /**
         * @see hub.powerassert.Operand#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            // write invoker
            if (!invoker.toString().equals("this")) {
                builder.append(invoker).append('.');
            }

            builder.append(methodName).append('(');

            for (int i = 0; i < parameters.size(); i++) {
                Operand value = parameters.get(i);

                if (i + 1 != parameterTypes.length) {
                    builder.append(value).append(", ");
                } else {
                    // last parameter processing
                    if (value instanceof OperandArray) {
                        OperandArray array = (OperandArray) value;

                        if (array.size == 0) {
                            // delete last separator ',' unless this method has only varargs
                            if (parameterTypes.length != 1) {
                                builder.delete(builder.length() - 2, builder.length());
                            }
                        } else {
                            // expand array elements
                            Iterator<Operand> iterator = array.values.iterator();

                            if (iterator.hasNext()) {
                                builder.append(iterator.next());

                                while (iterator.hasNext()) {
                                    builder.append(", ").append(iterator.next());
                                }
                            }
                        }
                    } else {
                        builder.append(value);
                    }
                }
            }
            builder.append(')');

            return builder.toString();
        }
    }
}