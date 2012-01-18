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

import java.util.ArrayDeque;
import java.util.ArrayList;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.objectweb.asm.Type;

/**
 * @version 2012/01/11 11:27:35
 */
@Manageable(lifestyle = ThreadSpecific.class)
public class PowerAssertContext implements Recoder {

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
        String[] localVariable = PowerAssert.localVariables.get(id);
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
        String[] localVariable = PowerAssert.localVariables.get(id);
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

        // method name
        invocation.insert(0, name);

        // remove "this" from this.methodCall() if needed
        Operand invoker = stack.pollLast();

        if (!invoker.name.equals("this")) {
            invocation.insert(0, '.').insert(0, invoker);
        }

        Operand operand = new Operand(invocation.toString(), value);
        stack.add(operand);
        operands.add(operand);
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
            builder.append("\r\n").append(operand.name).append(" : ").append(operand.toValueExpression());
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
}