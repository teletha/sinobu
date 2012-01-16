/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import hub.Agent.Translator;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.junit.Rule;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert extends ReusableRule {

    /** The local variable name mapping. */
    private static final Map<Integer, String[]> localVariables = new ConcurrentHashMap();

    @Rule
    private final Agent agent = new Agent(PowerAssertTranslator.class);

    /** The caller class. */
    private final Class caller;

    /** The tester flag. */
    private final boolean selfTest;

    /** The expected operands. */
    private final List<Operand> expecteds = new ArrayList();

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this.caller = UnsafeUtility.getCaller(1);
        this.selfTest = false;

        // force to transform
        agent.transform(caller);
    }

    /**
     * Test for {@link PowerAssert}.
     */
    PowerAssert(boolean selfTest) {
        this.caller = UnsafeUtility.getCaller(1);
        this.selfTest = selfTest;

        // force to transform
        agent.transform(caller);
    }

    /**
     * @param name
     * @param value
     */
    void willCapture(String name, Object value) {
        expecteds.add(new Operand(name, value));
    }

    /**
     * @see hub.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        expecteds.clear();
    }

    /**
     * @see hub.ReusableRule#validateError(java.lang.Throwable)
     */
    @Override
    protected Throwable validateError(Throwable throwable) {
        if (selfTest && throwable instanceof AssertionError) {
            PowerAssertContext context = PowerAssertContext.get();

            for (Operand expected : expecteds) {
                if (!context.operands.contains(expected)) {
                    return new AssertionError("Can't capture the below operand.\r\nCode  : " + expected.name + "\r\nValue : " + expected.value + "\r\n\r\n" + context);
                }
            }
            return null;
        } else {
            return throwable;
        }
    }

    /**
     * @version 2012/01/14 22:48:47
     */
    private static class PowerAssertTranslator extends Translator {

        private static final Type OBJECT_TYPE = Type.getType(Object.class);

        /** The state. */
        private boolean startAssertion = false;

        /** The state. */
        private boolean skipNextJump = false;

        /** The state. */
        private boolean processAssertion = false;

        private void loadContext() {
            super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertContext", "get", "()Lhub/PowerAssert$PowerAssertContext;");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (!startAssertion && opcode == GETSTATIC && name.equals("$assertionsDisabled")) {
                startAssertion = true;
                skipNextJump = true;

                super.visitFieldInsn(opcode, owner, name, desc);
            } else {
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (skipNextJump) {
                skipNextJump = false;
                processAssertion = true;

                super.visitJumpInsn(opcode, label);
            } else if (!processAssertion) {
                super.visitJumpInsn(opcode, label);
            } else {
                super.visitJumpInsn(opcode, label);

                switch (opcode) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertContext", "get", "()Lhub/PowerAssert$PowerAssertContext;");
                    super.visitLdcInsn("==");
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertContext", "recodeExpression", "(Ljava/lang/String;)V");
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertContext", "get", "()Lhub/PowerAssert$PowerAssertContext;");
                    super.visitLdcInsn("!=");
                    super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertContext", "recodeExpression", "(Ljava/lang/String;)V");
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (processAssertion && opcode == NEW && type.equals("java/lang/AssertionError")) {
                processAssertion = false;

                super.visitTypeInsn(opcode, type);
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            // replace invocation of AssertionError constructor.
            if (opcode == INVOKESPECIAL && owner.equals("java/lang/AssertionError")) {
                loadContext(); // first parameter
                mv.visitMethodInsn(opcode, owner, name, "(Ljava/lang/Object;)V"); // replace
                return;
            }

            super.visitMethodInsn(opcode, owner, name, desc);

            if (processAssertion) {
                // recode method invocation
                Type methodType = Type.getType(desc);
                Type returnType = methodType.getReturnType();

                mv.visitInsn(DUP);
                mv.visitVarInsn(returnType.getOpcode(ISTORE), 0);

                loadContext();
                mv.visitLdcInsn(name);
                mv.visitIntInsn(BIPUSH, methodType.getArgumentTypes().length);
                mv.visitVarInsn(returnType.getOpcode(ILOAD), 0);
                wrap(returnType);
                mv.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertContext", "recodeMethod", "(Ljava/lang/String;ILjava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);

            if (processAssertion) {
                loadContext();
                mv.visitIntInsn(opcode, operand);
                wrap(INT_TYPE);
                invokeVirtual(PowerAssertContext.class, Constant.class);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitInsn(int opcode) {
            super.visitInsn(opcode);

            if (processAssertion) {
                switch (opcode) {
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    loadContext();
                    mv.visitInsn(opcode);
                    wrap(INT_TYPE);
                    invokeVirtual(PowerAssertContext.class, Constant.class);
                    break;

                case LCONST_0:
                case LCONST_1:
                    loadContext();
                    mv.visitInsn(opcode);
                    wrap(LONG_TYPE);
                    invokeVirtual(PowerAssertContext.class, Constant.class);
                    break;

                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    loadContext();
                    mv.visitInsn(opcode);
                    wrap(FLOAT_TYPE);
                    invokeVirtual(PowerAssertContext.class, Constant.class);
                    break;

                case DCONST_0:
                case DCONST_1:
                    loadContext();
                    mv.visitInsn(opcode);
                    wrap(DOUBLE_TYPE);
                    invokeVirtual(PowerAssertContext.class, Constant.class);
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitLdcInsn(Object value) {
            super.visitLdcInsn(value);

            if (processAssertion) {
                loadContext();
                mv.visitLdcInsn(value);
                wrap(Type.getType(value.getClass()));
                invokeVirtual(PowerAssertContext.class, Constant.class);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitVarInsn(int opcode, int index) {
            super.visitVarInsn(opcode, index);

            if (processAssertion) {
                Type localVariableType = Type.INT_TYPE;

                switch (opcode) {
                case LLOAD:
                    localVariableType = Type.LONG_TYPE;
                    break;

                case FLOAD:
                    localVariableType = Type.FLOAT_TYPE;
                    break;

                case DLOAD:
                    localVariableType = Type.DOUBLE_TYPE;
                    break;

                case ALOAD:
                    localVariableType = OBJECT_TYPE;
                    break;
                }

                super.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertContext", "get", "()Lhub/PowerAssert$PowerAssertContext;");
                super.visitLdcInsn(new Integer(hashCode() + index));
                super.visitVarInsn(opcode, index);
                wrap(localVariableType);
                super.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertContext", "recodeLocalVariable", "(ILjava/lang/Object;)V");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            super.visitLocalVariable(name, desc, signature, start, end, index);

            localVariables.put(hashCode() + index, new String[] {name, desc});
        }

    }

    /**
     * @version 2012/01/11 11:27:35
     */
    @Manageable(lifestyle = ThreadSpecific.class)
    public static class PowerAssertContext implements Constant, Variable, LocalVariable, Expression, MethodCall {

        /** The operand stack. */
        private ArrayDeque<Operand> stack = new ArrayDeque();

        /** The using operand list. */
        private ArrayList<Operand> operands = new ArrayList();

        /** The source code representation. */
        private StringBuilder code = new StringBuilder("\r\n");

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeConstant(Object constant) {
            Operand operand = new Operand(constant);
            stack.add(operand);
            operands.add(operand);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeVariable(Object variable, String expression) {
            Operand operand = new Operand(expression, variable);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeLocalVariable(int id, Object variable) {
            Operand operand;
            String[] localVariable = localVariables.get(id);

            if (localVariable[1].equals("Z")) {
                operand = new Operand(localVariable[0], (int) variable == 1);
            } else {
                operand = new Operand(localVariable[0], variable);
            }

            stack.add(operand);
            operands.add(operand);
        }

        /**
         * @see hub.PowerAssert.Expression#recodeExpression(java.lang.String)
         */
        @Override
        public void recodeExpression(String expression) {
            switch (stack.size()) {
            case 0:
                break;

            case 1:
                code.append(stack.pollLast());
                break;

            default:
                code.append(stack.pollLast()).append(' ').append(expression).append(' ').append(stack.pollLast());
                break;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeMethod(String name, int paramsSize, Object value) {
            // build method invocation
            StringBuilder invocation = new StringBuilder("()");

            for (int i = 0; i < paramsSize; i++) {
                invocation.insert(1, stack.pollLast());

                if (i + 1 != paramsSize) {
                    invocation.insert(1, ", ");
                }
            }
            invocation.insert(0, name).insert(0, '.').insert(0, stack.pollLast());

            Operand operand = new Operand(invocation.toString(), value);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(code);
            builder.append("\r\n");

            for (Operand operand : operands) {
                if (!operand.constant) {
                    builder.append("\r\n").append(operand.name).append(" : ").append(operand.value);
                }
            }
            return builder.toString();
        }

        public static PowerAssertContext get() {
            return I.make(PowerAssertContext.class);
        }
    }

    /**
     * @version 2012/01/11 14:11:46
     */
    private static class Operand {

        /** The human redable expression. */
        private String name;

        /** The actual value. */
        private Object value;

        /** The constant flag. */
        private boolean constant;

        /**
         * 
         */
        private Operand(Object value) {
            this.name = value instanceof String ? "\"" + value + "\"" : String.valueOf(value);
            this.value = value;
            this.constant = true;
        }

        /**
         * 
         */
        private Operand(String name, Object value) {
            this.name = name;
            this.value = value;
            this.constant = false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Operand other = (Operand) obj;
            if (name == null) {
                if (other.name != null) return false;
            } else if (!name.equals(other.name)) return false;
            if (value == null) {
                if (other.value != null) return false;
            } else if (!value.equals(other.value)) return false;
            return true;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * <p>
     * Marker interface for type-safe bytecode builder.
     * </p>
     * 
     * @version 2012/01/14 2:08:48
     */
    private static interface Recodable {
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface Constant<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param constant
         */
        void recodeConstant(T constant);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface LocalVariable<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeLocalVariable(int id, T variable);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface Variable<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeVariable(T variable, String expression);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Expression<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeExpression(String expression);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface MethodCall<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeMethod(String name, int paramsSize, T value);
    }
}
