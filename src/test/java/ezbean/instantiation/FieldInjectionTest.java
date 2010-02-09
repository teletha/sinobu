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
package ezbean.instantiation;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import ezbean.I;

/**
 * @version 2010/02/09 20:39:41
 */
public class FieldInjectionTest {

    /**
     * Public field injection.
     */
    @Test
    public void publicField() {
        PublicFieldInjection clazz = I.make(PublicFieldInjection.class);
        assertNotNull(clazz);
        assertNotNull(clazz.injectedClass);
    }

    /**
     * Protected field injection.
     */
    @Test
    public void protectedField() {
        ProtectedFieldInjection clazz = I.make(ProtectedFieldInjection.class);
        assertNotNull(clazz);
        assertNotNull(clazz.injectedClass);
    }

    /**
     * Package private field injection.
     */
    @Test
    public void packagePrivateField() {
        PackagePrivateFieldInjection clazz = I.make(PackagePrivateFieldInjection.class);
        assertNotNull(clazz);
        assertNotNull(clazz.injectedClass);
    }

    /**
     * Private field injection.
     */
    @Test
    public void privateField() {
        PrivateFieldInjection clazz = I.make(PrivateFieldInjection.class);
        assertNotNull(clazz);
        assertNotNull(clazz.injectedClass);
    }

    /**
     * Private field injection.
     */
    @Test
    public void finalField() {
        FinalFieldInjection clazz = I.make(FinalFieldInjection.class);
        assertNotNull(clazz);
        assertNotNull(clazz.injectedClass);
    }

    /**
     * Test field injection with circular dependencies.
     */
    @Test(expected = ClassCircularityError.class)
    public void circularDependencies() {
        I.make(CircularDependencyA.class);
    }

    /**
     * @version 2010/02/09 20:39:47
     */
    private static class PublicFieldInjection {

        /** The dependency field. */
        @Resource
        public InjectedClass injectedClass;
    }

    /**
     * @version 2010/02/09 20:39:51
     */
    private static class ProtectedFieldInjection {

        /** The dependency field. */
        @Resource
        protected InjectedClass injectedClass;
    }

    /**
     * @version 2008/06/03 5:18:46
     */
    private static class PackagePrivateFieldInjection {

        /** The dependency field. */
        @Resource
        InjectedClass injectedClass;
    }

    /**
     * @version 2008/06/03 5:18:46
     */
    private static class PrivateFieldInjection {

        /** The dependency field. */
        @Resource
        private InjectedClass injectedClass;
    }

    /**
     * @version 2008/06/03 5:18:46
     */
    private static class FinalFieldInjection {

        /** The dependency field. */
        @Resource
        public final InjectedClass injectedClass = null;
    }

    /**
     * @version 2008/06/03 5:18:30
     */
    private static class InjectedClass {
    }

    /**
     * @version 2008/06/03 8:40:07
     */
    private static class CircularDependencyA {

        @Resource
        @SuppressWarnings("unused")
        public CircularDependencyB dependencyB;
    }

    /**
     * @version 2008/06/03 8:40:11
     */
    private static class CircularDependencyB {

        @Resource
        @SuppressWarnings("unused")
        public CircularDependencyA dependencyA;
    }
}
