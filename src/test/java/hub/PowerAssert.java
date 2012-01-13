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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.objectweb.asm.Type;
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
                BytecodeWriter builder = new BytecodeWriter(methodNode);

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
    private static final class BytecodeWriter implements Constant<Type>, Variable<LocalVariableNode> {

        private static final Type OBJET_TYPE = Type.getType(Object.class);

        private static final String context = "hub/PowerAssert$PowerAssertionContext";

        private final MethodNode methodNode;

        private final int contextIndex;

        private final InsnList nodes;

        /** The index node to insert. */
        private AbstractInsnNode index;

        /**
         * @param methodNode
         */
        private BytecodeWriter(MethodNode methodNode) {
            this.methodNode = methodNode;
            this.contextIndex = methodNode.localVariables.size() + 1;
            this.nodes = methodNode.instructions;
        }

        /**
         * <p>
         * Helper method to write bytecode which wrap the primitive value which is last on operand
         * stack to its wrapper value.
         * </p>
         * 
         * @param type
         */
        private void wrap(Type type) {
            Type wrapper = getWrapperType(type);

            if (wrapper != type) {
                build(new MethodInsnNode(INVOKESTATIC, wrapper.getInternalName(), "valueOf", Type.getMethodDescriptor(wrapper, type)));
            }
        }

        /**
         * <p>
         * Search wrapper type of the specified primitive type.
         * </p>
         * 
         * @param type
         * @return
         */
        private Type getWrapperType(Type type) {
            switch (type.getSort()) {
            case Type.BOOLEAN:
                return Type.getType(Boolean.class);

            case Type.INT:
                return Type.getType(Integer.class);

            case Type.LONG:
                return Type.getType(Long.class);

            case Type.FLOAT:
                return Type.getType(Float.class);

            case Type.DOUBLE:
                return Type.getType(Double.class);

            case Type.CHAR:
                return Type.getType(Character.class);

            case Type.BYTE:
                return Type.getType(Byte.class);

            case Type.SHORT:
                return Type.getType(Short.class);

            default:
                return type;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeConstant(Type type) {
            RecodableMethod method = RecodableMethod.get(Constant.class);
            AbstractInsnNode copy = index.clone(null);

            build(new VarInsnNode(ALOAD, contextIndex)); // load context
            build(copy);
            wrap(type);
            build(new MethodInsnNode(INVOKEVIRTUAL, context, method.name, method.descriptor));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeVariable(LocalVariableNode node, String expression) {
            RecodableMethod method = RecodableMethod.get(Variable.class);
            AbstractInsnNode copy = index.clone(null);

            build(new VarInsnNode(ALOAD, contextIndex));
            build(copy);
            wrap(Type.getType(node.desc));
            build(new LdcInsnNode(expression));
            build(new MethodInsnNode(INVOKEVIRTUAL, context, method.name, method.descriptor));
        }

        private void build(AbstractInsnNode node) {
            nodes.insert(index, node);
            index = node;
        }

        private void add(AbstractInsnNode node) {
            // update index
            index = node;

            switch (node.getType()) {
            case AbstractInsnNode.INT_INSN:
                recodeConstant(INT_TYPE);
                break;

            case AbstractInsnNode.INSN:
                switch (node.getOpcode()) {
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    recodeConstant(INT_TYPE);
                    break;

                case LCONST_0:
                case LCONST_1:
                    recodeConstant(LONG_TYPE);
                    break;

                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    recodeConstant(FLOAT_TYPE);
                    break;

                case DCONST_0:
                case DCONST_1:
                    recodeConstant(DOUBLE_TYPE);
                    break;
                }
                break;

            case AbstractInsnNode.LDC_INSN:
                recodeConstant(Type.getType(((LdcInsnNode) node).cst.getClass()));
                break;

            case AbstractInsnNode.METHOD_INSN:
                MethodInsnNode method = (MethodInsnNode) node;
                Type returnType = Type.getReturnType(method.desc);
                build(new InsnNode(DUP));
                build(new VarInsnNode(returnType.getOpcode(ISTORE), contextIndex + 1));

                build(new VarInsnNode(ALOAD, contextIndex));
                build(new LdcInsnNode(method.name));
                build(new LdcInsnNode(method.desc));
                build(new VarInsnNode(returnType.getOpcode(ILOAD), contextIndex + 1));

                // warp primitive type if needed
                if (returnType == Type.BOOLEAN_TYPE) {
                    build(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
                } else if (returnType == Type.INT_TYPE) {
                    build(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
                }
                build(new MethodInsnNode(INVOKEVIRTUAL, context, "addMethod", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
                break;

            case AbstractInsnNode.VAR_INSN:
                LocalVariableNode localVariable = (LocalVariableNode) methodNode.localVariables.get(((VarInsnNode) node).var);
                recodeVariable(localVariable, localVariable.name);
                break;

            case AbstractInsnNode.JUMP_INSN:
                build(new VarInsnNode(ALOAD, contextIndex));

                switch (node.getOpcode()) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    build(new LdcInsnNode("=="));
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
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
    public static class PowerAssertionContext implements Constant, Variable {

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

        public void addVariable(Object value, String name) {
            Operand operand = new Operand(name, value);
            stack.add(operand);
            operands.add(operand);
        }

        public void addExpression(String expression) {
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

        public void addMethod(String name, String description, Object value) {
            Type type = Type.getMethodType(description);

            // build method invocation
            StringBuilder invocation = new StringBuilder("()");

            for (int i = 0, length = type.getArgumentTypes().length; i < length; i++) {
                invocation.insert(1, stack.pollLast());

                if (i + 1 != length) {
                    invocation.insert(1, ", ");
                }
            }
            invocation.insert(0, name).insert(0, '.').insert(0, stack.pollLast());

            // code.append(invocation);

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
     * @version 2012/01/14 2:02:54
     */
    private static class RecodableMethod {

        /** The cache for recoder type. */
        private static final Map<Class, RecodableMethod> types = new HashMap();

        /** The method name. */
        private final String name;

        /** The method descriptor. */
        private final String descriptor;

        /** The method type. */
        private final Type type;

        /**
         * 
         */
        private RecodableMethod(Class<? extends Recodable> recoder) {
            Method method = recoder.getMethods()[0];
            this.name = method.getName();
            this.type = Type.getType(method);
            this.descriptor = type.getDescriptor();
        }

        /**
         * <p>
         * Search recoder method.
         * </p>
         * 
         * @param recoder
         * @return
         */
        private static RecodableMethod get(Class<? extends Recodable> recoder) {
            RecodableMethod method = types.get(recoder);

            if (method == null) {
                method = new RecodableMethod(recoder);

                types.put(recoder, method);
            }
            return method;
        }
    }
}
