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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert implements TestRule {

    /** The recode for the translated classes. */
    private static final Set<String> translateds = new CopyOnWriteArraySet();

    /** The local variable name mapping. */
    private static final Map<Integer, String[]> localVariables = new ConcurrentHashMap();

    private static final Agent agent = new Agent(PowerAssertTranslator.class);

    /** The tester flag. */
    private final boolean selfTest;

    /** The expected operands. */
    private final List<Operand> expecteds = new ArrayList();

    /** The expected operands. */
    private final List<String> operators = new ArrayList();

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this.selfTest = false;
    }

    /**
     * Test for {@link PowerAssert}.
     */
    PowerAssert(boolean selfTest) {
        this.selfTest = selfTest;
    }

    /**
     * @param name
     * @param value
     */
    void willCapture(String name, Object value) {
        expecteds.add(new Operand(name, value));
    }

    /**
     * @param operator
     */
    void willUseOperator(String operator) {
        operators.add(operator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void evaluate() throws Throwable {
                if (translateds.add(description.getClassName())) {
                    agent.transform(description.getTestClass());
                }

                expecteds.clear();
                operators.clear();

                try {
                    statement.evaluate();
                } catch (Throwable e) {
                    if (e instanceof AssertionError) {
                        if (translateds.add(description.getClassName())) {
                            agent.transform(description.getTestClass());

                            evaluate(); // retry
                            return;
                        }

                        PowerAssertContext context = PowerAssertContext.get();

                        if (selfTest) {
                            for (Operand expected : expecteds) {
                                if (!context.operands.contains(expected)) {
                                    throw new AssertionError("Can't capture the below operand.\r\nCode  : " + expected.name + "\r\nValue : " + expected.value + "\r\n");
                                }
                            }

                            for (String operator : operators) {
                                if (context.stack.peek().name.indexOf(operator) == -1) {
                                    throw new AssertionError("Can't capture the below operator.\r\nCode  : " + operator + "\r\n");
                                }
                            }
                            return;
                        }

                        // replace message
                        String message = e.getLocalizedMessage();

                        if (message == null) {
                            message = "";
                        }
                        AssertionError error = new AssertionError(message + "\r\n" + context);
                        error.setStackTrace(e.getStackTrace());
                        throw error;
                    }
                    throw e;
                }
            }
        };
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

        /**
         * <p>
         * Helper method to write code which load {@link PowerAssertContext}.
         * </p>
         */
        private void loadContext() {
            mv.visitMethodInsn(INVOKESTATIC, "hub/PowerAssert$PowerAssertContext", "get", "()Lhub/PowerAssert$PowerAssertContext;");
        }

        /**
         * <p>
         * Compute simple class name.
         * </p>
         * 
         * @return
         */
        private String computeClassName(String internalName) {
            int index = internalName.lastIndexOf('$');

            if (index == -1) {
                index = internalName.lastIndexOf('/');
            }
            return index == -1 ? internalName : internalName.substring(index + 1);
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
                return;
            }

            super.visitFieldInsn(opcode, owner, name, desc);

            if (processAssertion) {
                Type type = Type.getType(desc);
                mv.visitInsn(DUP);

                int index = newLocalVariable(type);
                mv.visitVarInsn(type.getOpcode(ISTORE), index);
                loadContext();

                switch (opcode) {
                case GETFIELD:
                    mv.visitLdcInsn(name);
                    mv.visitVarInsn(type.getOpcode(ILOAD), index);
                    wrap(type);
                    invokeVirtual(PowerAssertContext.class, FieldAccess.class);
                    break;

                case GETSTATIC:
                    mv.visitLdcInsn(computeClassName(owner) + '.' + name);
                    mv.visitVarInsn(type.getOpcode(ILOAD), index);
                    wrap(type);
                    invokeVirtual(PowerAssertContext.class, StaticFieldAccess.class);
                    break;
                }
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

                // reset context
                loadContext();
                mv.visitMethodInsn(INVOKEVIRTUAL, "hub/PowerAssert$PowerAssertContext", "clear", "()V");
                return;
            }

            super.visitJumpInsn(opcode, label);

            if (processAssertion) {
                switch (opcode) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    recodeOperator("==");
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    recodeOperator("!=");
                    break;

                case IF_ICMPLT:
                    recodeOperator("<");
                    break;

                case IF_ICMPLE:
                    recodeOperator("<=");
                    break;

                case IF_ICMPGT:
                    recodeOperator(">");
                    break;

                case IF_ICMPGE:
                    recodeOperator(">=");
                    break;

                case IFNULL:
                    // recode null constant
                    call().recodeConstant(insn(ACONST_NULL));

                    // recode == expression
                    recodeOperator("==");
                    break;

                case IFNONNULL:
                    // recode null constant
                    call().recodeConstant(insn(ACONST_NULL));

                    // recode != expression
                    recodeOperator("!=");
                    break;
                }
            }
        }

        /**
         * <p>
         * Helper method to write operator code.
         * </p>
         * 
         * @param operator
         */
        private void recodeOperator(String operator) {
            loadContext();
            mv.visitLdcInsn(operator);
            invokeVirtual(PowerAssertContext.class, Operator.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (processAssertion && opcode == NEW && type.equals("java/lang/AssertionError")) {
                processAssertion = false;
            }

            super.visitTypeInsn(opcode, type);

            if (processAssertion) {
                if (opcode == INSTANCEOF) {
                    loadContext();
                    mv.visitLdcInsn(computeClassName(type));
                    invokeVirtual(PowerAssertContext.class, Instanceof.class);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);

            // replace invocation of AssertionError constructor.
            if (startAssertion && opcode == INVOKESPECIAL && owner.equals("java/lang/AssertionError")) {
                // reset state
                startAssertion = false;
                skipNextJump = false;
                processAssertion = false;
                return;
            }

            if (processAssertion) {
                // recode method invocation
                Type type;
                Type methodType = Type.getType(desc);

                if (opcode == INVOKESPECIAL && name.charAt(0) == '<') {
                    // constructor
                    type = Type.getType(owner);
                } else {
                    // method
                    type = methodType.getReturnType();
                }

                int index = newLocalVariable(type);

                mv.visitInsn(DUP);

                switch (opcode) {
                case INVOKESTATIC:
                    mv.visitVarInsn(type.getOpcode(ISTORE), index);
                    loadContext();
                    mv.visitLdcInsn(computeClassName(owner) + '.' + name);
                    mv.visitIntInsn(BIPUSH, methodType.getArgumentTypes().length);
                    mv.visitVarInsn(type.getOpcode(ILOAD), index);
                    wrap(type);
                    invokeVirtual(PowerAssertContext.class, StaticMethodCall.class);
                    break;

                case INVOKESPECIAL:
                    mv.visitVarInsn(type.getOpcode(ISTORE), index);
                    loadContext();
                    mv.visitLdcInsn(computeClassName(owner));
                    mv.visitIntInsn(BIPUSH, methodType.getArgumentTypes().length);
                    mv.visitVarInsn(type.getOpcode(ILOAD), index);
                    wrap(type);
                    invokeVirtual(PowerAssertContext.class, ConstructorCall.class);
                    break;

                default:
                    mv.visitVarInsn(type.getOpcode(ISTORE), index);
                    loadContext();
                    mv.visitLdcInsn(name);
                    mv.visitIntInsn(BIPUSH, methodType.getArgumentTypes().length);
                    mv.visitVarInsn(type.getOpcode(ILOAD), index);
                    wrap(type);
                    invokeVirtual(PowerAssertContext.class, MethodCall.class);
                    break;
                }
            }
        }

        /**
         * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
         */
        @Override
        public void visitIincInsn(int index, int increment) {
            super.visitIincInsn(index, increment);

            if (processAssertion) {
                loadContext();
                mv.visitLdcInsn(new Integer(hashCode() + index));
                mv.visitIntInsn(BIPUSH, increment);
                invokeVirtual(PowerAssertContext.class, Increment.class);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);

            if (processAssertion) {
                call().recodeConstant(intInsn(opcode, operand));
            }
        }

        /**
         * <p>
         * Helper method to write code.
         * </p>
         * 
         * @return
         */
        private Manipulation call() {
            loadContext();

            return callInterface(PowerAssertContext.class, Manipulation.class);
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
                    call().recodeConstant(insn(opcode, INT_TYPE));
                    break;

                case LCONST_0:
                case LCONST_1:
                    call().recodeConstant(insn(opcode, LONG_TYPE));
                    break;

                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    call().recodeConstant(insn(opcode, FLOAT_TYPE));
                    break;

                case DCONST_0:
                case DCONST_1:
                    call().recodeConstant(insn(opcode, DOUBLE_TYPE));
                    break;

                case IADD:
                case LADD:
                case FADD:
                case DADD:
                    recodeOperator("+");
                    break;

                case ISUB:
                case LSUB:
                case FSUB:
                case DSUB:
                    recodeOperator("-");
                    break;

                case IMUL:
                case LMUL:
                case FMUL:
                case DMUL:
                    recodeOperator("*");
                    break;

                case IDIV:
                case LDIV:
                case FDIV:
                case DDIV:
                    recodeOperator("/");
                    break;

                case IREM:
                case LREM:
                case FREM:
                case DREM:
                    recodeOperator("%");
                    break;

                case INEG:
                case LNEG:
                case FNEG:
                case DNEG:
                    loadContext();
                    invokeVirtual(PowerAssertContext.class, Negative.class);
                    break;

                case ISHL:
                case LSHL:
                    recodeOperator("<<");
                    break;

                case ISHR:
                case LSHR:
                    recodeOperator(">>");
                    break;

                case IUSHR:
                case LUSHR:
                    recodeOperator(">>>");
                    break;

                case IOR:
                case LOR:
                    recodeOperator("|");
                    break;

                case IXOR:
                case LXOR:
                    recodeOperator("^");
                    break;

                case IAND:
                case LAND:
                    recodeOperator("&");
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
                call().recodeConstant(ldc(value));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitVarInsn(int opcode, int index) {
            super.visitVarInsn(opcode, index);

            if (processAssertion) {
                Type type = Type.INT_TYPE;

                switch (opcode) {
                case LLOAD:
                    type = Type.LONG_TYPE;
                    break;

                case FLOAD:
                    type = Type.FLOAT_TYPE;
                    break;

                case DLOAD:
                    type = Type.DOUBLE_TYPE;
                    break;

                case ALOAD:
                    type = OBJECT_TYPE;
                    break;
                }

                // loadContext();
                // mv.visitLdcInsn(new Integer(hashCode() + index));
                // mv.visitVarInsn(opcode, index);
                // wrap(localVariableType);
                // invokeVirtual(PowerAssertContext.class, LocalVariable.class);

                call().recodeLocalVariable(hashCode() + index, local(opcode, index).wrap(type));
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
    public static class PowerAssertContext
            implements FieldAccess, LocalVariable, Operator, MethodCall, StaticFieldAccess, StaticMethodCall,
            Increment, Negative, Instanceof, ConstructorCall, Manipulation {

        /** The operand stack. */
        private ArrayDeque<Operand> stack = new ArrayDeque();

        /** The using operand list. */
        private ArrayList<Operand> operands = new ArrayList();

        /** The incremetn state. */
        private String nextIncrement;

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
        public void recodeLocalVariable(int id, Object variable) {
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
         * @see hub.PowerAssert.Operator#recodeOperator(java.lang.String)
         */
        @Override
        public void recodeOperator(String expression) {
            if (1 < stack.size()) {
                Operand right = stack.pollLast();
                Operand left = stack.pollLast();
                stack.add(new Operand(left + " " + expression + " " + right, null));
            }
        }

        /**
         * @see hub.PowerAssert.Increment#recodeIncrement(int, int)
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
        public void recodeNegative() {
            stack.add(new Operand("-" + stack.pollLast(), null));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeInstanceof(String className) {
            stack.add(new Operand(stack.pollLast() + " instanceof " + className, null));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeField(String expression, Object variable) {
            Operand operand = new Operand(stack.pollLast() + "." + expression, variable);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * @see hub.PowerAssert.StaticFieldAccess#recodeStaticField(java.lang.String,
         *      java.lang.Object)
         */
        @Override
        public void recodeStaticField(String expression, Object variable) {
            Operand operand = new Operand(expression, variable);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeConstructor(String name, int paramsSize, Object value) {
            // build method invocation
            StringBuilder invocation = new StringBuilder("()");

            for (int i = 0; i < paramsSize; i++) {
                invocation.insert(1, stack.pollLast());

                if (i + 1 != paramsSize) {
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
         * @see hub.PowerAssert.StaticMethodCall#recodeStaticMethod(java.lang.String, int,
         *      java.lang.Object)
         */
        @Override
        public void recodeStaticMethod(String name, int paramsSize, Object value) {
            // build method invocation
            StringBuilder invocation = new StringBuilder("()");

            for (int i = 0; i < paramsSize; i++) {
                invocation.insert(1, stack.pollLast());

                if (i + 1 != paramsSize) {
                    invocation.insert(1, ", ");
                }
            }
            invocation.insert(0, name);

            Operand operand = new Operand(invocation.toString(), value);
            stack.add(operand);
            operands.add(operand);
        }

        /**
         * <p>
         * Clear current context.
         * </p>
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
                if (!operand.constant) {
                    builder.append("\r\n").append(operand.name).append(" : ").append(operand.toValueExpression());
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
            if (value instanceof String) {
                this.name = "\"" + value + "\"";
            } else if (value instanceof Class) {
                this.name = ((Class) value).getSimpleName() + ".class";
            } else {
                this.name = String.valueOf(value);
            }
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
         * <p>
         * Compute human-readable expression of value.
         * </p>
         * 
         * @return
         */
        private String toValueExpression() {
            if (value == null) {
                return "null";
            } else if (value instanceof String) {
                return "\"" + value + "\"";
            } else if (value instanceof Class) {
                return ((Class) value).getSimpleName() + ".class";
            } else if (value instanceof Enum) {
                Enum enumration = (Enum) value;
                return enumration.getDeclaringClass().getSimpleName() + '.' + enumration.name();
            } else {
                return value.toString();
            }
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
     * @version 2012/01/18 0:47:32
     */
    public static interface Manipulation {

        /**
         * <p>
         * Recode constant value.
         * </p>
         * 
         * @param constant
         */
        void recodeConstant(Object constant);

        /**
         * <p>
         * Recode local variable value.
         * </p>
         * 
         * @param id A local variable id.
         * @param variable A value.
         */
        void recodeLocalVariable(int id, Object variable);
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
    private static interface LocalVariable extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeLocalVariable(int id, Object variable);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface FieldAccess<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param expression
         * @param variable
         */
        void recodeField(String expression, T variable);
    }

    /**
     * @version 2012/01/14 1:51:05
     */
    private static interface StaticFieldAccess<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param expression
         * @param variable
         */
        void recodeStaticField(String expression, T variable);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Operator<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeOperator(String expression);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Increment extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param id
         */
        void recodeIncrement(int id, int increment);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Negative extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         */
        void recodeNegative();
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface Instanceof extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         */
        void recodeInstanceof(String className);
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

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface StaticMethodCall<T> extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeStaticMethod(String name, int paramsSize, T value);
    }

    /**
     * @version 2012/01/14 14:42:28
     */
    private static interface ConstructorCall extends Recodable {

        /**
         * <p>
         * Recode constant.
         * </p>
         * 
         * @param variable
         * @param expression
         */
        void recodeConstructor(String name, int paramsSize, Object value);
    }

}
