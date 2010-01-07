/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ezbean.Context;
import ezbean.Extensible;
import ezbean.model.ClassUtil;
import ezbean.model.Model;
import ezbean.model.Property;

/**
 * <p>
 * You can enhance the class file generation at runitme by Ezbean.
 * </p>
 * <p>
 * This class provides the ability of {@link ClassAdapter} and some helper methods to write byte
 * code.
 * </p>
 * <p>
 * Ezbean automatically recognizes all {@link Enhancer} class in classpaths and build a chain of
 * {@link Enhancer}. So you only have to implement the class which extends {@link Enhancer} class.
 * </p>
 * 
 * @version 2009/08/31 4:55:41
 */
public class Enhancer extends ClassAdapter implements Extensible {

    /** The type for {@link Context}. */
    private static final Type CONTEXT = Type.getType(Context.class);

    /**
     * The class name for the implemented class. This name conforms to not Fully Qualified Class
     * Name (JVMS 2.7) but Internal Form of Fully Qualified Class Name (JVMS 4.2).
     */
    protected String className;

    /** The model. */
    protected Model<?> model;

    /** The type of this model. */
    protected Type modelType;

    /** The current processing method visitor. */
    protected MethodVisitor mv;

    /**
     * <p>
     * This constructor of subclass <em>must not</em> have parameters.
     * </p>
     */
    protected Enhancer() {
        super(null); // delayed setup
    }

    /**
     * <p>
     * Build interceptor chain.
     * </p>
     * 
     * @param cv A class vistor.
     * @param model A target model information of the specified class
     * @param className A fully qualified class name for being generated class.
     */
    final Enhancer chain(ClassVisitor cv, Model model, String className) {
        this.cv = cv;
        this.model = model;
        this.modelType = Type.getType(model.type);
        this.className = className.replace('.', '/');

        // API definition
        return this;
    }

    /**
     * <p>
     * Generate a byte code which represents the specified class name and implements the specified
     * model.
     * </p>
     * 
     * @param trace If trace is <code>true</code>, this class generate the byte code for mock
     *            object. Otherwise for bean object.
     */
    final void write(int trace) {
        // ================================================
        // START CODING
        // ================================================
        // The following steps is an outline flow.
        // 1. define class
        //
        // 2. define default constructor
        // The generated class must have only the default constructor which has no parameters. To
        // resolve dependencies, we provide the solution named as 'builtin construction'. The
        // constructor must call the parent constructor, so we prepare arguments in that step by
        // using bytecode enhancement.
        //
        // 3. define properties
        //
        // 4. implement accessible interfaces
        //
        // 5 fiinish
        //
        // ================================================

        // -----------------------------------------------------------------------------------
        // Define Class
        // -----------------------------------------------------------------------------------
        // public class GeneratedClass extends SuperClass implements Inteface1, Interface2....
        visit(V1_5, ACC_PUBLIC | ACC_SUPER, className, null, modelType.getInternalName(), new String[] {
                "ezbean/Accessible", "java/io/Serializable"});

        // don't use visitSource method because this generated source is unknown
        // visitSource(className, null);

        // -----------------------------------------------------------------------------------
        // Define Constructor
        // -----------------------------------------------------------------------------------
        // decide constructor
        Constructor constructor = ClassUtil.getMiniConstructor(model.type);
        String descriptor = Type.getConstructorDescriptor(constructor);

        // public GeneratedClass( param1, param2 ) { super(param1, param2); ... }
        mv = visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        mv.visitCode();
        for (int i = 0; i < constructor.getParameterTypes().length + 1; i++) {
            mv.visitVarInsn(ALOAD, i); // allocate 'this' and parameter variables
        }
        mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), "<init>", descriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // compute by ASM
        mv.visitEnd();

        // -----------------------------------------------------------------------------------
        // Define Properties
        // -----------------------------------------------------------------------------------
        for (Property property : this.model.properties) {
            Type type = Type.getType(property.model.type);

            // The current processing accesser information (name and descriptor).
            String[] infos = info(property);

            /**
             * <p>
             * Define getter method.
             * </p>
             * <p>
             * This is an example about attribute property.
             * </p>
             * 
             * <pre>
             * public int getSome() {
             *     // track property path
             *     I.mock(&quot;some&quot;);
             * 
             *     return super.getSome();
             * }
             * </pre>
             * <p>
             * This is an example about none attribute property.
             * </p>
             * 
             * <pre>
             * public Some getSome() {
             *     // track property path
             *     I.mock(&quot;some&quot;);
             * 
             *     // return the trackable mock object
             *     return I.mock(Some.class);
             * }
             * </pre>
             * 
             * @see ezbean.graph.Coder#getter(ezbean.model.Property, org.objectweb.asm.Type)
             */
            if (trace != 0) {
                mv = visitMethod(ACC_PUBLIC, infos[0], infos[1], null, null);
                mv.visitCode();
                // invoke Ezbean mock method with property name
                mv.visitLdcInsn(property.name); // load 1st arguments
                mv.visitMethodInsn(INVOKESTATIC, "ezbean/I", "mock", "(Ljava/lang/Object;)Ljava/lang/Object;");
                mv.visitInsn(POP);

                if (property.isAttribute()) {
                    // This code is tricky because of footprint shurinking.
                    //
                    // Type.getOpcode(0) returns 1(long), 2(float), 3(double), 4(object array) or
                    // 0(other). The current type's sort is 7(long), 6(float), 8(double), 9(array),
                    // 10(object) or 1-5 (other). So the following code returns 10(long), 11(float),
                    // 15(double), 1(object array) or 2-6(other). These values are equivalent to the
                    // constant value code of itself.
                    mv.visitInsn((type.getOpcode(0) * 2 + 1 + Math.min(type.getSort(), 9)) % 17);
                } else {
                    mv.visitLdcInsn(type); // load 1st arguments
                    mv.visitMethodInsn(INVOKESTATIC, "ezbean/I", "mock", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                }

                // end method
                mv.visitInsn(type.getOpcode(IRETURN));
                mv.visitMaxs(0, 0); // compute by ASM
                mv.visitEnd();
            }

            /**
             * Define setter method.
             * 
             * <pre>
             * Object oldValue = super.getProperty();
             * 
             * super.setProperty(newValue);
             * 
             * if (context != null) {
             *     context.propertyChange(this, &quot;propertyName&quot;, oldValue, super.getProperty());
             * }
             * </pre>
             * 
             * @see ezbean.module.Coder#setter()
             */
            if (trace == 0) {
                mv = visitMethod(ACC_PUBLIC, infos[2], infos[3], null, null);
                mv.visitCode();

                // call super getter method and store the returned value
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), infos[0], infos[1]);
                mv.visitVarInsn(type.getOpcode(ISTORE), type.getSize() + 1);

                // call super setter method
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(type.getOpcode(ILOAD), 1);
                mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), infos[2], infos[3]);

                // invoke propertyChange method if the Context is not null
                field(GETFIELD, CONTEXT, "ezContext");
                Label branch = new Label();
                mv.visitJumpInsn(IFNULL, branch);
                field(GETFIELD, CONTEXT, "ezContext");
                mv.visitVarInsn(ALOAD, 0); // 1st this
                mv.visitLdcInsn(property.name); // 2nd "propertyName"
                mv.visitVarInsn(type.getOpcode(ILOAD), type.getSize() + 1); // 3rd old
                wrap(property.model.type); // warp to none-primitive type
                mv.visitVarInsn(ALOAD, 0); // 4th newValue
                mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), infos[0], infos[1]);
                wrap(property.model.type); // warp to none-primitive type
                mv.visitMethodInsn(INVOKEVIRTUAL, CONTEXT.getInternalName(), "propertyChange", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                mv.visitLabel(branch);

                // end methods
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0); // compute by ASM
                mv.visitEnd();
            }
        }

        // -----------------------------------------------------------------------------------
        // Implement Accessible Interfaces
        // -----------------------------------------------------------------------------------
        if (trace == 0) {
            /**
             * <p>
             * Implement the method {@link Accessible#ezContext()}.
             * </p>
             * <p>
             * Make field and method.
             * </p>
             * 
             * <pre>
             * private transient Context context;
             * 
             * public Context ezContext() {
             *     if (context == null) {
             *         context = new Context();
             *     }
             *     return context;
             * }
             * </pre>
             */
            // make field
            field(NEW, CONTEXT, "ezContext");

            // make method
            mv = visitMethod(ACC_PUBLIC | ACC_TRANSIENT, "ezContext", "()" + CONTEXT.getDescriptor(), null, null);
            mv.visitCode();
            field(GETFIELD, CONTEXT, "ezContext");
            Label branch = new Label();
            mv.visitJumpInsn(IFNONNULL, branch);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, CONTEXT.getInternalName());
            mv.visitInsn(DUP);

            mv.visitMethodInsn(INVOKESPECIAL, CONTEXT.getInternalName(), "<init>", "()V");
            field(PUTFIELD, CONTEXT, "ezContext");
            mv.visitLabel(branch);
            field(GETFIELD, CONTEXT, "ezContext");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 1);
            mv.visitEnd();

            /**
             * <p>
             * Implement the method {@link Accessible#ezAccess(int, Object)}. Getter and setter
             * methods have very similar code structure, so we can write that code in one method.
             * </p>
             * 
             * <pre>
             * public void ezAccess(int methodId, Object value) {
             *     switch (methodId) {
             *     default: // no such property
             *         throw new IllegalArgumentException(propertyName);
             * 
             *     case 0: // identifier of the property &quot;property1&quot;
             *         return getProperty1();
             * 
             *     case 1: // identifier of the property &quot;property1&quot;
             *         setProperty1((String) value);
             *         return null;
             * 
             *     case 2: // identifier of the property &quot;property2&quot;
             *         return Integer.valueOf(getProperty2()); // dynamic cast for primitive type
             * 
             *     case 3: // identifier of the property &quot;property2&quot;
             *         setProperty2(((Integer) value).intValue()); // dynamic cast for primitive type
             *         return null;
             *     }
             * }
             * </pre>
             */
            mv = visitMethod(ACC_PUBLIC, "ezAccess", "(ILjava/lang/Object;)Ljava/lang/Object;", null, null);

            // create label for each methods
            int size = model.properties.size() * 2;

            // create label for each property
            Label[] labels = new Label[size];

            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            // create label for default case
            Label label = new Label();

            // start switch statement
            mv.visitCode();
            mv.visitVarInsn(ILOAD, 1); // load 1st argument "propertyId"
            mv.visitTableSwitchInsn(0, size - 1, label, labels);

            // At first, we define default case for footprint optimazation
            mv.visitLabel(label);
            mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "()V");
            mv.visitInsn(ATHROW);

            // Then, we should define property accessors.
            for (int i = 0; i < size; i += 2) {
                Property property = model.properties.get(i / 2);

                // write code for getter
                mv.visitLabel(labels[i]);
                write(property.getAccessor(false), INVOKEVIRTUAL);

                // write code for setter
                mv.visitLabel(labels[i + 1]);
                write(property.getAccessor(true), INVOKEVIRTUAL);
            }

            mv.visitMaxs(0, 0); // compute by ASM
            mv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Finish Writing Source Code
        // -----------------------------------------------------------------------------------
        visitEnd();
    }

    protected final void write(Method method, int type) {
        Class[] params = method.getParameterTypes();
        Class returnType = method.getReturnType();

        mv.visitVarInsn(ALOAD, 0);
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, 2);

            if (type == INVOKESPECIAL) {
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(AALOAD);
            }
            cast(params[i]);
        }
        mv.visitMethodInsn(type, type == INVOKESPECIAL ? modelType.getInternalName() : className, method.getName(), Type.getMethodDescriptor(method));
        wrap(returnType);
        if (returnType == Void.TYPE) mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
    }

    /**
     * <p>
     * Helper method to compute accessors information.
     * </p>
     * <ol>
     * <li>A getter method name</li>
     * <li>A getter method description</li>
     * <li>A setter method name</li>
     * <li>A setter method description</li>
     * </ol>
     * 
     * @param property
     * @return
     */
    protected final String[] info(Property property) {
        String[] info = new String[4];

        // Getter name and description
        info[0] = property.getAccessor(false).getName();
        info[1] = Type.getMethodDescriptor(property.getAccessor(false));

        // Setter name and description
        info[2] = property.getAccessor(true).getName();
        info[3] = Type.getMethodDescriptor(property.getAccessor(true));

        // API definition
        return info;
    }

    /**
     * <p>
     * Hepler method to define accessor methods.
     * </p>
     * 
     * <pre>
     * private $Name;
     * 
     * public Name getName() {
     *     return $Name;
     * }
     * 
     * public void setName(Name name) {
     *     this.$Name = name;
     * }
     * </pre>
     * 
     * @param type A property type.
     * @param name A property name. The first character should be upper case.
     */
    protected final void accessor(String name, Type type) {
        // make field
        field(NEW, type, name);

        // make getter
        mv = visitMethod(ACC_PUBLIC, "get" + name, "()" + type.getDescriptor(), null, null);
        mv.visitCode();
        field(GETFIELD, type, name);
        mv.visitInsn(type.getOpcode(IRETURN));
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // make setter
        mv = visitMethod(ACC_PUBLIC, "set" + name, "(" + type.getDescriptor() + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); // 'this'
        mv.visitVarInsn(type.getOpcode(ILOAD), 1); // 1st argument
        field(PUTFIELD, type, name);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Helper method to write cast code. This cast mostly means down cast. (e.g. Object -> String,
     * Object -> int)
     * 
     * @param clazz A class to cast.
     * @return A class type to be casted.
     */
    protected final Type cast(Class clazz) {
        Type type = Type.getType(clazz);

        if (clazz.isPrimitive()) {
            if (clazz != Void.TYPE) {
                Type wrapper = Type.getType(ClassUtil.wrap(clazz));
                mv.visitTypeInsn(CHECKCAST, wrapper.getInternalName());
                mv.visitMethodInsn(INVOKEVIRTUAL, wrapper.getInternalName(), clazz.getName() + "Value", "()" + type.getDescriptor());
            }
        } else {
            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
        }

        // API definition
        return type;
    }

    /**
     * Helper method to write cast code. This cast mostly means up cast. (e.g. String -> Object, int
     * -> Integer)
     * 
     * @param clazz A primitive class type to wrap.
     */
    protected final void wrap(Class clazz) {
        if (clazz.isPrimitive() && clazz != Void.TYPE) {
            Type wrapper = Type.getType(ClassUtil.wrap(clazz));
            mv.visitMethodInsn(INVOKESTATIC, wrapper.getInternalName(), "valueOf", "(" + Type.getType(clazz)
                    .getDescriptor() + ")" + wrapper.getDescriptor());
        }
    }

    /**
     * Helper method to make field operation.
     * 
     * @param operation
     * @param type
     */
    protected final void field(int operation, Type type, String name) {
        if (operation == NEW) {
            visitField(ACC_PRIVATE | ACC_TRANSIENT, name, type.getDescriptor(), null, null).visitEnd();
        } else {
            if (operation == GETFIELD) {
                mv.visitVarInsn(ALOAD, 0); // load 'this' variable
            }
            mv.visitFieldInsn(operation, className, name, type.getDescriptor());
        }
    }
}
