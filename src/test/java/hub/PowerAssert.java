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
import hub.Agent.Translator;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert extends ReusableRule {

    @Rule
    private final Agent agent = new Agent(new PowerAssertTranslator());

    /** The caller class. */
    private final Class caller;

    /** The internal name of caller class. */
    private final String internalName;

    /** The tester flag. */
    private final boolean selfTest;

    /** The expected operands. */
    private final List<Operand> expecteds = new ArrayList();

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this.caller = UnsafeUtility.getCaller(1);
        this.internalName = caller.getName().replace('.', '/');
        this.selfTest = false;

        // force to transform
        agent.transform(caller);
    }

    /**
     * Test for {@link PowerAssert}.
     */
    PowerAssert(boolean selfTest) {
        this.caller = UnsafeUtility.getCaller(1);
        this.internalName = caller.getName().replace('.', '/');
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
        if (selfTest && throwable instanceof PowerAssertionError) {
            PowerAssertionError error = (PowerAssertionError) throwable;

            for (Operand expected : expecteds) {
                if (!error.context.operands.contains(expected)) {
                    return new AssertionError("Can't capture the below operand.\r\nCode  : " + expected.name + "\r\nValue : " + expected.value);
                }
            }
            return null;
        } else {
            return throwable;
        }
    }

    /**
     * @version 2012/01/10 11:51:03
     */
    private final class PowerAssertTranslator implements Translator {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canTranslate(String name) {
            return name.equals(internalName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void translate(ClassNode ast) {
            for (MethodNode methodNode : (List<MethodNode>) ast.methods) {
                // create context
                InsnList context = new InsnList();
                context.add(new TypeInsnNode(NEW, "hub/PowerAssert$PowerAssertionContext"));
                context.add(new InsnNode(DUP));
                context.add(new MethodInsnNode(INVOKESPECIAL, "hub/PowerAssert$PowerAssertionContext", "<init>", "()V"));
                context.add(new VarInsnNode(ASTORE, methodNode.localVariables.size() + 1));

                boolean underAssertion = false;
                ContextBuilder builder = new ContextBuilder(methodNode);

                Iterator<AbstractInsnNode> nodes = methodNode.instructions.iterator();

                while (nodes.hasNext()) {
                    AbstractInsnNode node = nodes.next();

                    if (underAssertion) {
                        // check end
                        if (node.getType() == AbstractInsnNode.TYPE_INSN) {
                            TypeInsnNode type = (TypeInsnNode) node;

                            if (type.getOpcode() == NEW && type.desc.equals("java/lang/AssertionError")) {
                                underAssertion = false;

                                // replace error
                                type.desc = "hub/PowerAssert$PowerAssertionError";

                                MethodInsnNode thrower = (MethodInsnNode) node.getNext().getNext();
                                thrower.owner = "hub/PowerAssert$PowerAssertionError";
                                thrower.desc = "(Lhub/PowerAssert$PowerAssertionContext;)V";

                                // load context
                                methodNode.instructions.insertBefore(thrower, new VarInsnNode(ALOAD, methodNode.localVariables.size() + 1));
                            }
                        }

                        // construct assert statement
                        builder.add(node);
                    } else if (node.getType() == AbstractInsnNode.FIELD_INSN) {
                        FieldInsnNode field = (FieldInsnNode) node;

                        if (field.getOpcode() == GETSTATIC && field.name.equals("$assertionsDisabled")) {
                            underAssertion = true;

                            // reset stack frame depth
                            methodNode.maxStack = 0;
                            methodNode.maxLocals = 0;

                            // skip next jump
                            nodes.next();
                        }
                    }
                }

                methodNode.instructions.insert(context);
            }
        }
    }

    /**
     * @version 2012/01/11 10:48:47
     */
    private static final class ContextBuilder {

        private static final String context = "hub/PowerAssert$PowerAssertionContext";

        private final MethodNode methodNode;

        private final int position;

        private final InsnList nodes;

        /** The index node to insert. */
        private AbstractInsnNode index;

        /**
         * @param methodNode
         */
        private ContextBuilder(MethodNode methodNode) {
            this.methodNode = methodNode;
            this.position = methodNode.localVariables.size() + 1;
            this.nodes = methodNode.instructions;
        }

        private void build(AbstractInsnNode node) {
            nodes.insertBefore(index, node);
        }

        private void add(AbstractInsnNode node) {
            // update index
            index = node;

            switch (node.getType()) {
            case AbstractInsnNode.INSN:
                switch (node.getOpcode()) {
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    build(new VarInsnNode(ALOAD, position));
                    build(node.clone(null));
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "add", "(I)V"));
                    break;

                case LCONST_0:
                case LCONST_1:
                    build(new VarInsnNode(ALOAD, position));
                    build(node.clone(null));
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "add", "(J)V"));
                    break;

                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    build(new VarInsnNode(ALOAD, position));
                    build(node.clone(null));
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "add", "(F)V"));
                    break;

                case DCONST_0:
                case DCONST_1:
                    build(new VarInsnNode(ALOAD, position));
                    build(node.clone(null));
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "add", "(D)V"));
                    break;
                }
                break;

            case AbstractInsnNode.VAR_INSN:
                LocalVariableNode localVariable = (LocalVariableNode) methodNode.localVariables.get(((VarInsnNode) node).var);

                build(new VarInsnNode(ALOAD, position));
                build(node.clone(null));
                build(new LdcInsnNode(localVariable.name));

                switch (node.getOpcode()) {
                case ILOAD:
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "addVariable", "(" + localVariable.desc + "Ljava/lang/String;)V"));
                    break;

                case LLOAD:
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "addVariable", "(JLjava/lang/String;)V"));
                    break;

                case FLOAD:
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "addVariable", "(FLjava/lang/String;)V"));
                    break;

                case DLOAD:
                    build(new MethodInsnNode(INVOKEVIRTUAL, context, "addVariable", "(DLjava/lang/String;)V"));
                    break;
                }

                break;

            case AbstractInsnNode.JUMP_INSN:
                build(new VarInsnNode(ALOAD, position));

                switch (node.getOpcode()) {
                case IF_ICMPEQ:
                case IFEQ:
                    build(new LdcInsnNode("=="));
                    break;

                case IFNE:
                    build(new LdcInsnNode("!="));
                    break;
                }
                build(new MethodInsnNode(INVOKEVIRTUAL, context, "addExpression", "(Ljava/lang/String;)V"));
                break;
            }
        }
    }

    /**
     * @version 2012/01/11 11:15:09
     */
    public static class PowerAssertionError extends AssertionError {

        /**
         * 
         */
        private static final long serialVersionUID = 4801744854945741947L;

        /** The error context. */
        private final PowerAssertionContext context;

        /**
         * @param context
         */
        public PowerAssertionError(PowerAssertionContext context) {
            super(context.toString());

            this.context = context;
        }
    }

    /**
     * @version 2012/01/11 11:27:35
     */
    public static class PowerAssertionContext {

        /** The operand stack. */
        private ArrayDeque<Operand> stack = new ArrayDeque();

        /** The using operand list. */
        private ArrayList<Operand> operands = new ArrayList();

        /** The source code representation. */
        private StringBuilder code = new StringBuilder("\r\n");

        public void add(int value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void add(long value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void add(float value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void add(double value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void add(short value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void add(boolean value) {
            Operand operand = new Operand(value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(int value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(long value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(float value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(double value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(short value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addVariable(boolean value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addExpression(String expression) {
            code.append(stack.pollLast()).append(' ').append(expression).append(' ').append(stack.pollLast());
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(code);

            for (Operand operand : operands) {
                if (!operand.constant) {
                    builder.append("\r\n").append(operand.name).append(" : ").append(operand.value);
                }
            }
            return builder.toString();
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
            this.name = String.valueOf(value);
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
}
