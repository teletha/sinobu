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

import static org.junit.Assert.*;
import static org.objectweb.asm.Opcodes.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import ezbean.I;
import ezunit.PrivateModule;

/**
 * @version 2009/07/10 13:08:34
 */
public class InterceptorTest {

    @Rule
    public static PrivateModule module = new PrivateModule();

    @Test
    public void intercept() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.intercept();
        assertEquals(3, intercepted.getCounter());
    }

    @Test
    public void paramInt() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.paramInt(10);
        assertEquals(12, intercepted.getCounter());
    }

    @Test
    public void paramString() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.paramString("10");
        assertEquals(12, intercepted.getCounter());
    }

    @Test
    public void params() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.params(2, 3);
        assertEquals(8, intercepted.getCounter());
    }

    @Test
    public void paramsTooMany() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.paramsTooMany(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(57, intercepted.getCounter());
    }

    @Test
    public void returnInt() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        assertEquals(1, intercepted.returnInt());
        assertEquals(2, intercepted.getCounter());
    }

    @Test
    public void returnString() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        assertEquals("1", intercepted.returnString());
        assertEquals(2, intercepted.getCounter());
    }

    @Test
    public void chain1() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.chain1();
        assertEquals(3, intercepted.getCounter());
    }

    @Test
    public void chain2() {
        InterceptedMethod intercepted = I.make(InterceptedMethod.class);
        assertEquals(0, intercepted.getCounter());

        intercepted.chain2();
        assertEquals(4, intercepted.getCounter());
    }

    /**
     * @version 2009/12/26 21:05:24
     */
    protected static class InterceptedMethod {

        private int counter = 0;

        /**
         * Get the counter property of this {@link InterceptorTest.InterceptedMethod}.
         * 
         * @return The counter property.
         */
        public int getCounter() {
            return counter;
        }

        /**
         * Set the counter property of this {@link InterceptorTest.InterceptedMethod}.
         * 
         * @param counter The counter value to set.
         */
        public void setCounter(int counter) {
            this.counter = counter;
        }

        @First
        public void intercept() {
            counter++;
        }

        @First
        public void paramInt(int i) {
            counter += i;
        }

        @First
        public void paramString(String i) {
            counter += Integer.parseInt(i);
        }

        @First
        public void params(int i, int j) {
            counter += i * j;
        }

        @First
        public void paramsTooMany(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j) {
            counter += a + b + c + d + e + f + g + h + i + j;
        }

        @First
        public int returnInt() {
            return counter;
        }

        @First
        public String returnString() {
            return Integer.valueOf(counter).toString();
        }

        @First
        @Second
        public void chain1() {
            counter++;
        }

        @Second
        @First
        public void chain2() {
            counter++;
        }
    }

    /**
     * @version 2009/12/26 20:45:26
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface First {
    }

    /**
     * @version 2009/12/26 20:45:26
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Second {
    }

    /**
     * @version 2009/12/26 20:45:26
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Third {
    }

    /**
     * @version 2009/12/26 20:46:09
     */
    public static class FirstInterceptor extends Interceptor<First> {

        /**
         * @see ezbean.module.Interceptor#invoke(java.lang.Object, java.lang.Object[],
         *      java.lang.annotation.Annotation)
         */
        @Override
        protected Object invoke(Object that, Object[] params, First annotation) {
            ((InterceptedMethod) that).counter++;
            Object result = super.invoke(that, params, annotation);
            ((InterceptedMethod) that).counter++;
            return result;
        }
    }

    /**
     * @version 2009/12/26 20:46:09
     */
    public static class SecondInterceptor extends Interceptor<Second> {

        /**
         * @see ezbean.module.Interceptor#invoke(java.lang.Object, java.lang.Object[],
         *      java.lang.annotation.Annotation)
         */
        @Override
        protected Object invoke(Object that, Object[] params, Second annotation) {
            InterceptedMethod method = (InterceptedMethod) that;

            if (method.counter == 0) {
                method.counter++;
            }
            return super.invoke(that, params, annotation);
        }
    }

    /**
     * @version 2010/01/24 13:00:32
     */
    public static interface SuperCallable {

        /**
         * Call the specified method through by-path.
         * 
         * @param id A method identifier.
         * @param params A list of parameters.
         * @return A result object or {@link Void#TYPE}.
         */
        Object ezCall(int id, Object... params);
    }

    /**
     * @version 2010/01/02 20:05:34
     */
    public static class InterceptorEnhancer extends Enhancer {

        /**
         * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            String[] dist = new String[interfaces.length + 1];

            for (int i = 0; i < interfaces.length; i++) {
                dist[i] = interfaces[i];
            }
            dist[interfaces.length] = SuperCallable.class.getName().replace('.', '/');

            super.visit(version, access, name, signature, superName, dist);
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visitEnd()
         */
        @Override
        public void visitEnd() {
            super.visitEnd();

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
            List<Method> intercepts = find(model.type);

            // create label for each methods
            int size = intercepts.size();

            if (size != 0) {
                mv = visitMethod(ACC_PUBLIC + ACC_VARARGS, "ezCall", "(I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);

                Label[] labels = codeSwitch(size);

                // Then, we should define property accessors.
                for (int i = 0; i < size; i++) {
                    mv.visitLabel(labels[i]);
                    write(intercepts.get(i), INVOKESPECIAL);
                }
                mv.visitMaxs(0, 0); // compute by ASM
                mv.visitEnd();
            }

            for (int i = 0; i < size; i++) {
                Method method = intercepts.get(i);
                Class[] parameters = method.getParameterTypes();

                mv = visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitIntInsn(BIPUSH, i); // method identifier
                mv.visitIntInsn(BIPUSH, parameters.length); // Object array size
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // create Object array
                for (int j = 0; j < parameters.length; j++) {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, j);
                    mv.visitVarInsn(Type.getType(parameters[j]).getOpcode(ILOAD), j + 1);
                    wrap(parameters[j]);
                    mv.visitInsn(AASTORE);
                }
                mv.visitMethodInsn(INVOKESTATIC, "ezbean/module/Interceptor", "invoke", "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;");
                mv.visitInsn(cast(method.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0); // compute by ASM
                mv.visitEnd();
            }
        }

        /**
         * <p>
         * Code switch stetement for properties.
         * </p>
         * 
         * <pre>
         * switch (propertyId) {
         * default:
         *     throw new IllegalArgumentException();
         * }
         * </pre>
         * 
         * @return A list of {@link Label} which are assosiated with the specified property.
         */
        private final Label[] codeSwitch(int size) {
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

            // API definition
            return labels;
        }
    }

    static final List<Method> find(Class clazz) {
        List<Method> methods = new ArrayList();

        for (Method method : clazz.getMethods()) {
            if (method.getAnnotations().length != 0) {
                methods.add(method);
            }
        }

        return methods;
    }
}
