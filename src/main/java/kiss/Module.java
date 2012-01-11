/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static org.objectweb.asm.Opcodes.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import kiss.model.ClassUtil;

/**
 * <h2>Module System</h2>
 * <p>
 * Module is a kind of classpath. It can be dynamically unloaded and knows much about information of
 * all classes which is managed by this.
 * </p>
 * <p>
 * The module doesn't collect detailed information on the managed class until needing it. This lazy
 * evaluation relieves us of the following annoying works.
 * </p>
 * <ul>
 * <li>Module Loading Order</li>
 * <li>Dependency Graph Management</li>
 * </ul>
 * <h2>Generated Class Naming Strategy</h2>
 * <p>
 * We adopted Suffix Naming Strategy of an automatically generated class at first (e.g. SomeClass+,
 * AnotherClass-). But this strategy has problem against to core package classes (e.g.
 * java.util.Date class, java.awt.Dimension). Therefore, we adopt Preffix Naming Strategy now.
 * </p>
 * 
 * @version 2011/11/19 19:06:03
 */
class Module extends URLClassLoader {

    /** The root of this module. */
    final Path path;

    /** The class scanner. */
    private ModuleVisitor visitor;

    /** The current processing method visitor. */
    private MethodVisitor mv;

    /**
     * <p>
     * Module constructor should be package private.
     * </p>
     * 
     * @param path A module path as classpath, A <code>null</code> is not accepted.
     */
    Module(Path path) throws MalformedURLException {
        super(new URL[] {path.toUri().toURL()}, I.$loader);

        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        // Store original module path for unloading.
        this.path = path;
        this.visitor = new ModuleVisitor(this);
    }

    /**
     * Returns the automatic generated class which implements or extends the given model.
     * 
     * @param model A class information of the model.
     * @param trace If trace is <code>1</code>, the generated code will be traceable code for mock
     *            object, otherwise for normal bean object.
     * @return A generated {@link Class} object.
     */
    synchronized Class define(Class model, Cache interceptables) {
        // Compute fully qualified class name for the generated class.
        // The coder class name is prefix to distinguish enhancer type by a name and make core
        // package classes (e.g. swing components) enhance.
        // The statement "String name = coder.getName() + model.type.getName();" produces larger
        // byte code and more objects. To reduce them, we should use the method "concat".
        String name = model.getName().concat("+");

        if (name.startsWith("java.")) {
            name = "$".concat(name);
        }

        // find class from cache of class loader
        Class clazz = findLoadedClass(name);

        if (clazz == null) {
            // start writing byte code
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            // write code actually
            write(writer, model, name.replace('.', '/'), interceptables);

            // retrieve byte code
            byte[] bytes = writer.toByteArray();

            // define class
            clazz = defineClass(name, bytes, 0, bytes.length, model.getProtectionDomain());
        }

        // API definition
        return clazz;
    }

    /**
     * Collect all service provider classes which is managed by this module.
     * 
     * @param <S> A type of service provider interface.
     * @param spi A service provider interface.
     * @param single A flag for finding mode. <code>true</code> is single mode, <code>false</code>
     *            is all mode.
     * @return A list of all service provider classes in this module. Never be <code>null</code>.
     */
    <S> List<Class<S>> find(Class<S> spi, boolean single) {
        // check null
        if (spi == null) {
            return Collections.EMPTY_LIST;
        }
    
        // compute hash of service provider interface
        int hash = spi.getName().hashCode();
    
        // set up container
        List list = new ArrayList(4);
    
        // try to find all service providers
        for (Object[] info : visitor.infos) {
            try {
                if (test(hash, info)) {
                    list.add(loadClass((String) info[0]));
    
                    if (single) {
                        return list;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw I.quiet(e);
            }
        }
    
        // API definition
        return list;
    }

    /**
     * Check whether the class is a subclass of the class which is represented by the hash or not.
     * 
     * @param hash A hash to find.
     * @param info A class information.
     * @return A result.
     */
    private boolean test(int hash, Object[] info) throws ClassNotFoundException {
        int[] hashs = (int[]) info[1];
    
        if (hashs != null) {
            return -1 < Arrays.binarySearch(hashs, hash);
        }
    
        // lazy evaluation
        Class<?> clazz = loadClass((String) info[0]);
    
        // stealth class must be hidden from module
        if (ClassUtil.getMiniConstructor(clazz) == null) {
            return !visitor.infos.remove(info); // test method requires false for stealth classs
        }
    
        Set<Class> set = ClassUtil.getTypes(clazz);
        Annotation[] annotations = clazz.getAnnotations();
    
        // compute hash
        int i = 0;
        hashs = new int[set.size() + annotations.length];
    
        for (Class c : set) {
            hashs[i++] = c.getName().hashCode();
        }
    
        for (Annotation a : annotations) {
            hashs[i++] = a.annotationType().getName().hashCode();
        }
    
        // sort for search
        Arrays.sort(hashs);
    
        // register information of the service provider class
        info[1] = hashs;
    
        // API definition
        return test(hash, info);
    }

    /**
     * <p>
     * Generate a byte code which represents the specified class name and implements the specified
     * model.
     * </p>
     */
    private void write(ClassVisitor cv, Class model, String className, Cache<Method, Annotation> interceptables) {
        Type type = Type.getType(model);

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
        cv.visit(V1_7, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, type.getInternalName(), null);

        // don't use visitSource method because this generated source is unknown
        // visitSource(className, null);

        // -----------------------------------------------------------------------------------
        // Define Constructor
        // -----------------------------------------------------------------------------------
        // decide constructor
        Constructor constructor = ClassUtil.getMiniConstructor(model);
        String descriptor = Type.getConstructorDescriptor(constructor);

        // public GeneratedClass( param1, param2 ) { super(param1, param2); ... }
        mv = cv.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        mv.visitCode();
        for (int i = 0; i < constructor.getParameterTypes().length + 1; i++) {
            mv.visitVarInsn(ALOAD, i); // allocate 'this' and parameter variables
        }
        mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", descriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // compute by ASM
        mv.visitEnd();

        // -----------------------------------------------------------------------------------
        // Define Annotation Pool
        // -----------------------------------------------------------------------------------
        cv.visitField(ACC_PRIVATE + ACC_STATIC, "pool", "Ljava/util/Map;", null, null).visitEnd();

        Label end = new Label();
        Label loop = new Label();

        mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
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

        // -----------------------------------------------------------------------------------
        // Define Interceptable Methods
        // -----------------------------------------------------------------------------------
        for (Entry<Method, List<Annotation>> entry : interceptables.entrySet()) {
            Method method = entry.getKey();
            Type methodType = Type.getType(method);

            mv = cv.visitMethod(ACC_PUBLIC, method.getName(), methodType.getDescriptor(), null, null);

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
            mv.visitMethodInsn(INVOKESTATIC, "kiss/Interceptor", "invoke", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/annotation/Annotation;)Ljava/lang/Object;");
            cast(method.getReturnType());
            mv.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
            mv.visitMaxs(0, 0); // compute by ASM
            mv.visitEnd();
        }

        // -----------------------------------------------------------------------------------
        // Finish Writing Source Code
        // -----------------------------------------------------------------------------------
        cv.visitEnd();
    }

    /**
     * <p>
     * Helper method to write annotation code.
     * </p>
     * 
     * @param annotation An annotation you want to write.
     * @param visitor An annotation target.
     */
    private final void annotate(Annotation annotation, AnnotationVisitor visitor) {
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
    private final Type cast(Class clazz) {
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
    private final void wrap(Class clazz) {
        if (clazz.isPrimitive() && clazz != Void.TYPE) {
            Type wrapper = Type.getType(ClassUtil.wrap(clazz));
            mv.visitMethodInsn(INVOKESTATIC, wrapper.getInternalName(), "valueOf", "(" + Type.getType(clazz)
                    .getDescriptor() + ")" + wrapper.getDescriptor());
        }
    }
}
