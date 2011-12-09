/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean;

import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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
 * @version 2011/11/19 18:55:44
 */
public class Enhancer extends ClassVisitor implements Extensible {

    /**
     * The class name for the implemented class. This name conforms to not Fully Qualified Class
     * Name (JVMS 2.7) but Internal Form of Fully Qualified Class Name (JVMS 4.2).
     */
    protected String className;

    /** The model. */
    protected Model model;

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
        super(ASM4); // delayed setup
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
    final void write() {
        Type context = Type.getType(Listeners.class);

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
        visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, modelType.getInternalName(), new String[] {"java/io/Serializable"});

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
        for (int i = 0; i < model.properties.size(); i++) {
            Property property = model.properties.get(i);
            Type type = Type.getType(property.model.type);

            // The current processing accesser information (name and descriptor).
            String[] infos = info(property);

            /**
             * Define setter method.
             * 
             * <pre>
             * if (context == null) {
             *     super.setProperty(newValue);
             * } else {
             *     Interceptor.invoke(this, &quot;propertyName&quot;, newValue);
             * }
             * </pre>
             * 
             * @see ezbean.module.Coder#setter()
             */

            mv = visitMethod(ACC_PUBLIC, infos[2], infos[3], null, null);
            mv.visitCode();

            Label invoke = new Label();
            Label end = new Label();
            boolean may = property.getAccessor(true).getAnnotations().length == 0;

            if (may) {
                // if (context == null) {
                field(GETFIELD, context, "context");
                mv.visitJumpInsn(IFNONNULL, invoke);

                // super.setter(param);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(type.getOpcode(ILOAD), 1);
                mv.visitMethodInsn(INVOKESPECIAL, modelType.getInternalName(), infos[2], infos[3]);

                // } else {
                mv.visitJumpInsn(GOTO, end);
                mv.visitLabel(invoke);
            }

            // Interceptor.invoke(this, "propertyName", param);
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitLdcInsn(property.name); // property name
            mv.visitVarInsn(type.getOpcode(ILOAD), 1); // new value
            wrap(property.model.type); // warp to none-primitive type
            mv.visitMethodInsn(INVOKESTATIC, "ezbean/Interceptor", "invoke", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");

            // }
            if (may) mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0); // compute by ASM
            mv.visitEnd();
        }

        /**
         * <p>
         * Make field.
         * </p>
         * 
         * <pre>
             * private transient Context context;
             * </pre>
         */
        // make context field
        field(NEW, context, "context");

        // -----------------------------------------------------------------------------------
        // Finish Writing Source Code
        // -----------------------------------------------------------------------------------
        visitEnd();
    }

    /**
     * <p>
     * Helper method to write method invocation code for property accessor.
     * </p>
     * 
     * @param method A target property accessor.
     * @param type A invocation type.
     */
    protected final void write(Method method, int type) {
        Class[] params = method.getParameterTypes();
        Class returnType = method.getReturnType();

        mv.visitVarInsn(ALOAD, 0);

        // Write method invocation.
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, 2);
            cast(params[i]);
        }
        mv.visitMethodInsn(type, type == INVOKESPECIAL ? modelType.getInternalName() : className, method.getName(), Type.getMethodDescriptor(method));

        // write return type.
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
            visitField(ACC_PUBLIC | ACC_TRANSIENT, name, type.getDescriptor(), null, null).visitEnd();
        } else {
            if (operation == GETFIELD) {
                mv.visitVarInsn(ALOAD, 0); // load 'this' variable
            }
            mv.visitFieldInsn(operation, className, name, type.getDescriptor());
        }
    }
}
