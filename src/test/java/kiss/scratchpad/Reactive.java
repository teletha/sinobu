/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import java.beans.Introspector;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import kiss.Disposable;
import kiss.I;
import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2014/08/20 12:01:31
 */
public class Reactive {

    public static void main(String[] args) {
        Bean bean1 = new Bean();
        Bean bean2 = new Bean();

        Binder.of(bean1::getAge);

        Binder.bind(bean1, p -> {
            bean2.setAge(p.getAge() * 2);
            bean2.setName(p.getName());
        });

        Binder.bind(bean2, p -> {
            System.out.println(p.getAge() + " was changed.");
        });
    }

    /**
     * @version 2014/08/20 12:13:01
     */
    @Documented
    @Target(value = {ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Bindable {
    }

    /**
     * @version 2014/08/20 12:05:27
     */
    public static class Binder<T> {

        public static <T> Binder<T> of(Supplier<T> supplier) {
            return new Binder();
        }

        /**
         */
        public static <Param1, Param2> Disposable bind(Param1 param1, Consumer<Param1> reference) {
            try {
                String name = reference.getClass().getName();

                int start = 0;
                int end = name.indexOf("$$");
                Class declaration = Class.forName(name.substring(start, end));

                start = end + 10;
                end = name.indexOf("/", start);
                String methodName = "lambda$".concat(name.substring(start, end));

                for (Method method : declaration.getDeclaredMethods()) {
                    if (method.isSynthetic()) {
                        if (method.getName().equals(methodName)) {
                            Model model = Model.load(method.getParameterTypes()[0]);
                            PropertySearch search = new PropertySearch(model);

                            new ClassReader(declaration.getName()).accept(new MethodSearch(methodName, search), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                            for (Property property : search.properties) {
                                // TODO implement notification code
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
            return null;
        }
    }

    /**
     * @version 2014/08/20 13:01:08
     */
    private static class MethodSearch extends ClassVisitor {

        /** The target. */
        private final String name;

        /** The finder. */
        private final PropertySearch search;

        /**
         */
        public MethodSearch(String name, PropertySearch search) {
            super(Opcodes.ASM5);

            this.name = name;
            this.search = search;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (this.name.equals(name)) {
                return search;
            }
            return null;
        }
    }

    /**
     * @version 2014/08/20 13:06:34
     */
    private static class PropertySearch extends MethodVisitor {

        /** The target model. */
        private final Model model;

        /** The internal model name. */
        private final String modelInternal;

        /** The property names. */
        final Set<Property> properties = new HashSet();

        /**
         */
        protected PropertySearch(Model model) {
            super(Opcodes.ASM5);

            this.model = model;
            this.modelInternal = Type.getType(model.type).getInternalName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean flag) {
            if (modelInternal.equals(owner) && Type.getArgumentTypes(desc).length == 0) {
                Type returnType = Type.getReturnType(desc);
                String prefix = "get";

                if (returnType == Type.BOOLEAN_TYPE) {
                    prefix = "is";
                }

                if (name.startsWith(prefix)) {
                    Property property = model.getProperty(Introspector.decapitalize(name.substring(prefix.length())));

                    if (property != null) {
                        properties.add(property);
                    }
                }
            }
        }
    }

    /**
     * @version 2014/08/20 12:03:07
     */
    public static class Bean {

        private int age;

        private String name;

        /**
         * Get the age property of this {@link Reactive.Bean}.
         * 
         * @return The age property.
         */
        public int getAge() {
            return age;
        }

        /**
         * Set the age property of this {@link Reactive.Bean}.
         * 
         * @param age The age value to set.
         */
        public void setAge(int age) {
            this.age = age;
        }

        /**
         * Get the name property of this {@link Reactive.Bean}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link Reactive.Bean}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
