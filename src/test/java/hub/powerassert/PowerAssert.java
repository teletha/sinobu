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

import static org.objectweb.asm.Opcodes.*;
import hub.bytecode.Agent;
import hub.bytecode.Agent.Translator;
import hub.bytecode.LocalVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

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
    static final Map<Integer, String[]> localVariables = new ConcurrentHashMap();

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
            mv.visitMethodInsn(INVOKESTATIC, "hub/powerassert/PowerAssertContext", "get", "()Lhub/powerassert/PowerAssertContext;");

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
}
