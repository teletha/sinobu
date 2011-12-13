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

import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Opcodes.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ezbean.model.ClassUtil;
import ezbean.model.Model;

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
 * @version 2011/12/11 20:27:13
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

        // -----------------------------------------------------------------------------------
        // Collect Annotation Information
        // -----------------------------------------------------------------------------------
        Listeners<Method, Annotation> map = new Listeners();

        for (Class clazz : ClassUtil.getTypes(model.type)) {
            for (Method method : clazz.getDeclaredMethods()) {
                // exclude the method which modifier is final, static, private or native
                if (((STATIC | PRIVATE | NATIVE | FINAL) & method.getModifiers()) != 0) {
                    continue;
                }

                // exclude the method which is created by compiler
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

                Annotation[] annotations = method.getAnnotations();

                if (annotations.length != 0) {
                    // check method overriding
                    for (Method candidate : map.keySet()) {
                        if (candidate.getName().equals(method.getName()) && Arrays.deepEquals(candidate.getParameterTypes(), method.getParameterTypes())) {
                            method = candidate; // detect overriding
                            break;
                        }
                    }

                    for (Annotation annotation : annotations) {
                        if (!(annotation instanceof Deprecated)) {
                            map.push(method, annotation);
                        }
                    }
                }
            }
        }

        int counter = 0;

        for (; counter < map.size();) {
            FieldVisitor fv = visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "a".concat(String.valueOf(counter++)), "Ljava/util/List;", null, null);
            fv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Define Annotation Pool
        // -----------------------------------------------------------------------------------
        if (map.size() != 0) {
            mv = visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            Label start = new Label();
            Label end = new Label();
            Label error = new Label();
            mv.visitTryCatchBlock(start, end, error, "java/lang/Exception");
            mv.visitLabel(start);

            counter = 0;

            for (Method method : map.keySet()) {
                mv.visitLdcInsn(Type.getType(method.getDeclaringClass()));
                mv.visitLdcInsn(method.getName());

                Class[] params = method.getParameterTypes();
                mv.visitIntInsn(BIPUSH, params.length);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

                for (int i = 0; i < params.length; i++) {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, i);

                    if (params[i].isPrimitive()) {
                        mv.visitFieldInsn(GETSTATIC, ClassUtil.wrap(params[i]).getName().replace('.', '/'), "TYPE", "Ljava/lang/Class;");
                    } else {
                        mv.visitLdcInsn(Type.getType(params[i]));
                    }
                    mv.visitInsn(AASTORE);
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
                mv.visitMethodInsn(INVOKESTATIC, "ezbean/model/ClassUtil", "getAnnotation", "(Ljava/lang/reflect/Method;)Ljava/util/List;");
                mv.visitFieldInsn(PUTSTATIC, className, "a".concat(String.valueOf(counter++)), "Ljava/util/List;");
            }
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(error);
            mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
            mv.visitVarInsn(ASTORE, 0);
            mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Define Interceptable Methods
        // -----------------------------------------------------------------------------------
        counter = 0;

        for (Entry<Method, List<Annotation>> entry : map.entrySet()) {
            Method method = entry.getKey();
            Type methodType = Type.getType(method);

            mv = visitMethod(ACC_PUBLIC, method.getName(), methodType.getDescriptor(), null, null);

            // Write annotations
            for (Annotation annotation : entry.getValue()) {
                annotation(mv.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true), annotation);
                // meta(mv.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true),
                // annotation);
            }

            // Write code
            mv.visitCode();

            // First parameter : Method delegation
            Handle handle = new Handle(H_INVOKESPECIAL, className.substring(0, className.length() - 1), method.getName(), methodType.getDescriptor());
            mv.visitLdcInsn(handle);

            // Second parameter : Callee instance
            mv.visitVarInsn(ALOAD, 0);

            // Third parameter : Method parameter delegation
            Class[] params = method.getParameterTypes();

            mv.visitIntInsn(BIPUSH, params.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            // objects[0] ~ [n] are method parameter
            for (int i = 0; i < params.length; i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitVarInsn(Type.getType(params[i]).getOpcode(ILOAD), i + 1);
                wrap(params[i]);
                mv.visitInsn(AASTORE);
            }

            // Fourth parameter : Pass annotation information
            mv.visitFieldInsn(GETSTATIC, className, "a".concat(String.valueOf(counter++)), "Ljava/util/List;");

            // Invoke interceptor method
            mv.visitMethodInsn(INVOKESTATIC, "ezbean/Interceptor", "invoke", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/Object;[Ljava/lang/Object;Ljava/util/List;)Ljava/lang/Object;");
            cast(method.getReturnType());
            mv.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
            mv.visitMaxs(0, 0); // compute by ASM
            mv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Finish Writing Source Code
        // -----------------------------------------------------------------------------------
        visitEnd();
    }

    private void annotation(AnnotationVisitor av, Annotation annotation) {
        // For access non-public annotation class.
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            method.setAccessible(true);

            try {
                Class clazz = method.getReturnType();
                String name = method.getName();
                Object value = method.invoke(annotation);

                if (clazz.isAnnotation()) {
                    // Annotation
                    annotation(av.visitAnnotation(name, Type.getDescriptor(clazz)), (Annotation) value);
                } else if (clazz.isArray()) {
                    // Array
                    Class type = clazz.getComponentType();
                    AnnotationVisitor visitor = av.visitArray(name);

                    for (int i = 0; i < Array.getLength(value); i++) {
                        if (type.isAnnotation()) {
                            // Annotation Array
                            annotation(visitor.visitAnnotation(null, Type.getDescriptor(type)), (Annotation) Array.get(value, i));
                        } else if (type == Class.class) {
                            // Class Array
                            visitor.visit(null, Type.getType((Class) Array.get(value, i)));
                        } else {
                            // Other Type Array
                            visitor.visit(null, Array.get(value, i));
                        }
                    }
                    visitor.visitEnd();
                } else if (clazz == Class.class) {
                    // Class
                    av.visit(method.getName(), Type.getType((Class) value));
                } else {
                    // Other Type
                    av.visit(method.getName(), value);
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        av.visitEnd();
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
