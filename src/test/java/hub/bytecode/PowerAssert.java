/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.bytecode;

import static org.objectweb.asm.Opcodes.*;
import hub.bytecode.Agent.Translator;

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

        /** The state. */
        private boolean startAssertion = false;

        /** The state. */
        private boolean skipNextJump = false;

        /** The state. */
        private boolean processAssertion = false;

        /** The acrual recoder. */
        private final Recoder context = createAPI(PowerAssertContext.class, Recoder.class);

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
         * <p>
         * Helper method to write code.
         * </p>
         * 
         * @return
         */
        private Recoder recode() {
            // load context
            mv.visitMethodInsn(INVOKESTATIC, "hub/bytecode/PowerAssert$PowerAssertContext", "get", "()Lhub/bytecode/PowerAssert$PowerAssertContext;");

            // return API
            return context;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);

            if (!startAssertion && opcode == GETSTATIC && name.equals("$assertionsDisabled")) {
                startAssertion = true;
                skipNextJump = true;
                return;
            }

            if (processAssertion) {
                mv.visitInsn(DUP);

                // store current value
                LocalVariable local = newLocal(Type.getType(desc));
                local.store();

                switch (opcode) {
                case GETFIELD:
                    recode().field(name, local);
                    break;

                case GETSTATIC:
                    recode().staticField(computeClassName(owner) + '.' + name, local);
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
                recode().clear();
                return;
            }

            super.visitJumpInsn(opcode, label);

            if (processAssertion) {
                switch (opcode) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    recode().operator("==");
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    recode().operator("!=");
                    break;

                case IF_ICMPLT:
                    recode().operator("<");
                    break;

                case IF_ICMPLE:
                    recode().operator("<=");
                    break;

                case IF_ICMPGT:
                    recode().operator(">");
                    break;

                case IF_ICMPGE:
                    recode().operator(">=");
                    break;

                case IFNULL:
                    // recode null constant
                    recode().constant(insn(ACONST_NULL));

                    // recode == expression
                    recode().operator("==");
                    break;

                case IFNONNULL:
                    // recode null constant
                    recode().constant(insn(ACONST_NULL));

                    // recode != expression
                    recode().operator("!=");
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
            }

            super.visitTypeInsn(opcode, type);

            if (processAssertion) {
                if (opcode == INSTANCEOF) {
                    recode().instanceOf(computeClassName(type));
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
                mv.visitInsn(DUP);

                Type type = Type.getType(desc);
                int size = type.getArgumentTypes().length;

                // save current value
                LocalVariable local = newLocal(name.charAt(0) == '<' ? Type.getType(owner) : type.getReturnType());
                local.store();

                switch (opcode) {
                case INVOKESTATIC:
                    recode().staticMethod(computeClassName(owner) + '.' + name, size, local);
                    break;

                case INVOKESPECIAL:
                    recode().constructor(computeClassName(owner), size, local);
                    break;

                default:
                    recode().method(name, size, local);
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
                recode().recodeIncrement(hashCode() + index, increment);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);

            if (processAssertion) {
                recode().constant(intInsn(opcode, operand));
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
                case LCONST_0:
                case LCONST_1:
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                case DCONST_0:
                case DCONST_1:
                    recode().constant(insn(opcode));
                    break;

                case IADD:
                case LADD:
                case FADD:
                case DADD:
                    recode().operator("+");
                    break;

                case ISUB:
                case LSUB:
                case FSUB:
                case DSUB:
                    recode().operator("-");
                    break;

                case IMUL:
                case LMUL:
                case FMUL:
                case DMUL:
                    recode().operator("*");
                    break;

                case IDIV:
                case LDIV:
                case FDIV:
                case DDIV:
                    recode().operator("/");
                    break;

                case IREM:
                case LREM:
                case FREM:
                case DREM:
                    recode().operator("%");
                    break;

                case INEG:
                case LNEG:
                case FNEG:
                case DNEG:
                    recode().negative();
                    break;

                case ISHL:
                case LSHL:
                    recode().operator("<<");
                    break;

                case ISHR:
                case LSHR:
                    recode().operator(">>");
                    break;

                case IUSHR:
                case LUSHR:
                    recode().operator(">>>");
                    break;

                case IOR:
                case LOR:
                    recode().operator("|");
                    break;

                case IXOR:
                case LXOR:
                    recode().operator("^");
                    break;

                case IAND:
                case LAND:
                    recode().operator("&");
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
                recode().constant(ldc(value));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitVarInsn(int opcode, int index) {
            super.visitVarInsn(opcode, index);

            if (processAssertion) {
                recode().localVariable(hashCode() + index, local(opcode, index));
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
    public static class PowerAssertContext implements Recoder {

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
        public void constant(Object constant) {
            Operand operand = new Operand(constant);
            stack.add(operand);
            operands.add(operand);
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
         * @see hub.bytecode.PowerAssert.Recoder#operator(java.lang.String)
         */
        @Override
        public void operator(String expression) {
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
        public void constructor(String name, int paramsSize, Object value) {
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
        public void method(String name, int paramsSize, Object value) {
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
         * {@inheritDoc}
         */
        @Override
        public void staticMethod(String name, int paramsSize, Object value) {
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
         * @see hub.bytecode.PowerAssert.Recoder#clear()
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
    private static interface Recoder {

        /**
         * <p>
         * Clear current context.
         * </p>
         */
        void clear();

        /**
         * <p>
         * Recode constant value.
         * </p>
         * 
         * @param constant
         */
        void constant(Object constant);

        /**
         * <p>
         * Recode local variable value.
         * </p>
         * 
         * @param id A local variable id.
         * @param variable A value.
         */
        void localVariable(int id, Object variable);

        /**
         * <p>
         * Recode field access.
         * </p>
         * 
         * @param expression
         * @param variable
         */
        void field(String expression, Object variable);

        /**
         * <p>
         * Recode static field access.
         * </p>
         * 
         * @param expression
         * @param variable
         */
        void staticField(String expression, Object variable);

        /**
         * <p>
         * Recode operator.
         * </p>
         * 
         * @param expression
         */
        void operator(String operator);

        /**
         * <p>
         * Recode increment operation.
         * </p>
         * 
         * @param id A local variable id.
         * @param increment A increment value.
         */
        void recodeIncrement(int id, int increment);

        /**
         * <p>
         * Recode negative value operation.
         * </p>
         */
        void negative();

        /**
         * <p>
         * Recode instanceof operation.
         * </p>
         * 
         * @param className
         */
        void instanceOf(String className);

        /**
         * <p>
         * Recode method call.
         * </p>
         * 
         * @param name A method name.
         * @param paramsSize A method parameter size.
         * @param value A returned value.
         */
        void method(String name, int paramsSize, Object value);

        /**
         * <p>
         * Recode static method call.
         * </p>
         * 
         * @param name A method name.
         * @param paramsSize A method parameter size.
         * @param value A returned value.
         */
        void staticMethod(String name, int paramsSize, Object value);

        /**
         * <p>
         * Recode constructor call.
         * </p>
         * 
         * @param name A constructor name.
         * @param paramsSize A constructor parameter size.
         * @param value A constructed value.
         */
        void constructor(String name, int paramsSize, Object value);
    }
}
