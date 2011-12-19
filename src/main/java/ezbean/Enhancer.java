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
        visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, modelType.getInternalName(), null);

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
        Cache<Method, Annotation> map = new Cache();

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

        // -----------------------------------------------------------------------------------
        // Define Annotation Pool
        // -----------------------------------------------------------------------------------
        if (map.size() != 0) {
            visitField(ACC_PRIVATE + ACC_STATIC, "pool", "Ljava/util/Map;", null, null).visitEnd();

            Label end = new Label();
            Label loop = new Label();

            mv = visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitFieldInsn(PUTSTATIC, className, "pool", "Ljava/util/Map;");
            mv.visitLdcInsn(Type.getType("L" + className + ";"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitInsn(ARRAYLENGTH);
            mv.visitVarInsn(ISTORE, 2);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 1);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(loop);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ASTORE, 0);
            mv.visitFieldInsn(GETSTATIC, className, "pool", "Ljava/util/Map;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getAnnotations", "()[Ljava/lang/annotation/Annotation;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitIincInsn(1, 1); // increment counter
            mv.visitLabel(end);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitJumpInsn(IF_ICMPLT, loop);
            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 4);
            mv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Define Interceptable Methods
        // -----------------------------------------------------------------------------------
        for (Entry<Method, List<Annotation>> entry : map.entrySet()) {
            Method method = entry.getKey();
            Type methodType = Type.getType(method);

            mv = visitMethod(ACC_PUBLIC, method.getName(), methodType.getDescriptor(), null, null);

            // Write annotations
            for (Annotation annotation : entry.getValue()) {
                annotate(annotation, mv.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true));
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
            mv.visitFieldInsn(GETSTATIC, className, "pool", "Ljava/util/Map;");
            mv.visitLdcInsn(method.getName().concat(Arrays.toString(method.getParameterTypes())));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/annotation/Annotation;");

            // Invoke interceptor method
            mv.visitMethodInsn(INVOKESTATIC, "ezbean/Interceptor", "invoke", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/annotation/Annotation;)Ljava/lang/Object;");
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

    /**
     * <p>
     * Helper method to write annotation code.
     * </p>
     * 
     * @param annotation An annotation you want to write.
     * @param visitor An annotation target.
     */
    protected final void annotate(Annotation annotation, AnnotationVisitor visitor) {
        // For access non-public annotation class, use "getDeclaredMethods" instead of "getMethods".
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            method.setAccessible(true);

            try {
                Class clazz = method.getReturnType();
                Object value = method.invoke(annotation);

                if (clazz == Class.class) {
                    // Class
                    visitor.visit(method.getName(), Type.getType((Class) value));
                } else if (clazz.isEnum()) {
                    // Enum
                    visitor.visitEnum(method.getName(), Type.getDescriptor(clazz), ((Enum) value).name());
                } else if (clazz.isAnnotation()) {
                    // Annotation
                    annotate((Annotation) value, visitor.visitAnnotation(method.getName(), Type.getDescriptor(clazz)));
                } else if (clazz.isArray()) {
                    // Array
                    clazz = clazz.getComponentType();
                    AnnotationVisitor array = visitor.visitArray(method.getName());

                    for (int i = 0; i < Array.getLength(value); i++) {
                        if (clazz.isAnnotation()) {
                            // Annotation Array
                            annotate((Annotation) Array.get(value, i), array.visitAnnotation(null, Type.getDescriptor(clazz)));
                        } else if (clazz == Class.class) {
                            // Class Array
                            array.visit(null, Type.getType((Class) Array.get(value, i)));
                        } else if (clazz.isEnum()) {
                            // Enum Array
                            array.visitEnum(null, Type.getDescriptor(clazz), ((Enum) Array.get(value, i)).name());
                        } else {
                            // Other Type Array
                            array.visit(null, Array.get(value, i));
                        }
                    }
                    array.visitEnd();
                } else {
                    // Other Type
                    visitor.visit(method.getName(), value);
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
        visitor.visitEnd();
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
}
