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

import kiss.I;
import kiss.Manageable;
import kiss.ThreadSpecific;

import org.junit.Rule;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
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
                                thrower.desc = "()V";
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
    private static final class BytecodeWriter
            implements Constant<Type>, Variable<Type>, LocalVariable<Type>, Expression<Type>, MethodCall<Type> {

        private static final Type OBJECT_TYPE = Type.getType(Object.class);

        /** The method bytecode. */
        private final MethodNode methodNode;

        /** The declared local variable index. */
        private int declaredLocalVariableIndex;

        /** The index node to insert. */
        private AbstractInsnNode index;

        /**
         * @param methodNode
         */
        private BytecodeWriter(MethodNode methodNode) {
            this.methodNode = methodNode;
            this.declaredLocalVariableIndex = methodNode.localVariables.size() + 1;

            for (LocalVariableNode local : (List<LocalVariableNode>) methodNode.localVariables) {
                String id = PowerAssertionContext.computeLocalVariableId("class", methodNode.name, methodNode.desc, String.valueOf(local.index));

                PowerAssertionContext.getLocalVariable(id, local);
            }
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
                write(new MethodInsnNode(INVOKESTATIC, wrapper.getInternalName(), "valueOf", Type.getMethodDescriptor(wrapper, type)));
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
            AbstractInsnNode copy = index.clone(null);

            write(new MethodInsnNode(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;"));
            write(copy);
            wrap(type);
            write(RecodableMethod.get(Constant.class).toNode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeVariable(Type type, String expression) {
            AbstractInsnNode copy = index.clone(null);

            write(new MethodInsnNode(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;"));
            write(copy);
            wrap(type);
            write(new LdcInsnNode(expression));
            write(RecodableMethod.get(Variable.class).toNode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeLocalVariable(String id, Type type) {
            AbstractInsnNode copy = index.clone(null);

            write(new MethodInsnNode(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;"));
            write(new LdcInsnNode(PowerAssertionContext.computeLocalVariableId("class", methodNode.name, methodNode.desc, id)));
            write(copy);
            wrap(type);
            write(RecodableMethod.get(LocalVariable.class).toNode());
        }

        /**
         * @see hub.PowerAssert.Expression#recodeExpression(java.lang.String)
         */
        @Override
        public void recodeExpression(String expression) {
            write(new MethodInsnNode(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;"));
            write(new LdcInsnNode(expression));
            write(RecodableMethod.get(Expression.class).toNode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void recodeMethod(String name, int paramsSize, Type type) {
            Type returnType = type.getReturnType();

            write(new InsnNode(DUP));
            write(new VarInsnNode(returnType.getOpcode(ISTORE), declaredLocalVariableIndex + 1));

            write(new MethodInsnNode(INVOKESTATIC, "hub/PowerAssert$PowerAssertionContext", "get", "()Lhub/PowerAssert$PowerAssertionContext;"));
            write(new LdcInsnNode(name));
            write(new IntInsnNode(BIPUSH, paramsSize));
            write(new VarInsnNode(returnType.getOpcode(ILOAD), declaredLocalVariableIndex + 1));

            // warp primitive type if needed
            wrap(returnType);
            write(RecodableMethod.get(MethodCall.class).toNode());
        }

        /**
         * <p>
         * Helper method to write bytecode in current location.
         * </p>
         * 
         * @param node A code to write.
         */
        private void write(AbstractInsnNode node) {
            methodNode.instructions.insert(index, node);
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
                Type methodType = Type.getMethodType(method.desc);

                recodeMethod(method.name, methodType.getArgumentTypes().length, methodType);
                break;

            case AbstractInsnNode.VAR_INSN:
                VarInsnNode localVariableNode = (VarInsnNode) node;
                Type localVariableType = Type.INT_TYPE;

                switch (localVariableNode.getOpcode()) {
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
                recodeLocalVariable(String.valueOf(localVariableNode.var), localVariableType);
                break;

            case AbstractInsnNode.JUMP_INSN:
                switch (node.getOpcode()) {
                case IFEQ:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    recodeExpression("==");
                    break;

                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    recodeExpression("!=");
                    break;
                }

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
        public PowerAssertionError() {
            super(PowerAssertionContext.get().toString());

            this.context = PowerAssertionContext.get();
        }
    }

    /**
     * @version 2012/01/11 11:27:35
     */
    @Manageable(lifestyle = ThreadSpecific.class)
    public static class PowerAssertionContext implements Constant, Variable, LocalVariable, Expression, MethodCall {

        /** The local variable name mapping. */
        private static final Map<String, Local> locals = new HashMap();

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
        public void recodeLocalVariable(String id, Object variable) {
            Operand operand;
            Local local = getLocalVariable(id, null);

            switch (local.type.getSort()) {
            case BOOLEAN:
                operand = new Operand(local.name, (int) variable == 1);
                break;

            default:
                operand = new Operand(local.name, variable);
                break;
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

        public static PowerAssertionContext get() {
            return I.make(PowerAssertionContext.class);
        }

        /**
         * @param id
         * @param node
         */
        public static Local getLocalVariable(String id, LocalVariableNode node) {
            Local local = locals.get(id);

            if (local == null) {
                local = new Local();

                locals.put(id, local);
            }

            if (local.name == null && node != null) {
                local.name = node.name;
                local.type = Type.getType(node.desc);
            }
            return local;
        }

        /**
         * <p>
         * Compute identifier for local variable.
         * </p>
         * 
         * @param className
         * @param methodName
         * @param methodDescriptor
         * @param position
         * @return
         */
        public static String computeLocalVariableId(String className, String methodName, String methodDescriptor, String position) {
            return className + methodName + methodDescriptor + position;
        }

        /**
         * @version 2012/01/14 20:50:07
         */
        private static class Local {

            /** The variable type. */
            public Type type;

            /** The variable name. */
            public String name;
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
        void recodeLocalVariable(String id, T variable);
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

        /** The method owner. */
        private final String owner;

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
            this.owner = Type.getType(method.getDeclaringClass()).getInternalName();
        }

        /**
         * <p>
         * Convert to method node.
         * </p>
         * 
         * @return
         */
        private MethodInsnNode toNode() {
            return new MethodInsnNode(INVOKEINTERFACE, owner, name, descriptor);
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
