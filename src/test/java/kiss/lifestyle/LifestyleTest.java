/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lifestyle;

import java.util.HashSet;
import java.util.Set;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;
import kiss.I;
import kiss.Lifestyle;
import kiss.Manageable;
import kiss.Prototype;
import kiss.Singleton;
import kiss.sample.bean.Person;
import kiss.sample.bean.Student;

/**
 * @version 2014/07/12 11:40:46
 */
public class LifestyleTest {

    @Rule
    @ClassRule
    public static final PrivateModule module = new PrivateModule();

    private static final String CONSTANT = "Set by " + LifestyleTest.class.getSimpleName();

    @Test
    public void override() {
        HasCustomLifestyleInPrivateModule object = I.make(HasCustomLifestyleInPrivateModule.class);
        assert object != I.make(HasCustomLifestyleInPrivateModule.class);

        // unload module
        module.unload();

        object = I.make(HasCustomLifestyleInPrivateModule.class);
        assert object == I.make(HasCustomLifestyleInPrivateModule.class);
    }

    @Test
    public void unload() {
        assert WithoutLifestyle.object == I.make(Without.class);
        assert WithoutLifestyle.object == I.make(Without.class);

        // unload module
        module.unload();

        Without object = I.make(Without.class);
        assert WithoutLifestyle.object != object;
        assert object != I.make(Without.class);
    }

    // @Test
    // public void unloadOverridden() {
    // assert Locale.ROOT.equals(I.make(Locale.class));
    //
    // // unload lifestyle definition
    // I.make(I.class).unload(LocalLifestyle.class);
    //
    // assert Locale.getDefault().equals(I.make(Locale.class));
    // }

    @Test
    public void extendPrototype() {
        Person person = I.make(Person.class);
        assert CONSTANT.equals(person.getFirstName());
    }

    @Test
    public void extendPrototypeWithClassParameter() {
        Student person = I.make(Student.class);
        assert person instanceof Student;
        assert CONSTANT.equals(person.getFirstName());
    }

    @Test
    public void customLifestyle() {
        assert WithoutLifestyle.object == I.make(Without.class);
    }

    @Test
    public void customPrototype() {
        CustomClass instance = I.make(CustomClass.class);
        assert CustomLifestyle.set.contains(instance);
    }

    @Test
    public void interfaceLifestyle() throws Exception {
        Interface resolved = I.make(Interface.class);
        assert resolved == InterfaceLifestyle.implementation;
    }

    /**
     * <p>
     * External class without {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    private static class Without {
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    private static class WithoutLifestyle implements Lifestyle<Without> {

        private static final Without object = new Without();

        /**
         * @see kiss.Lifestyle#get()
         */
        @Override
        public Without get() {
            return object;
        }
    }

    /**
     * <p>
     * External class with {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    @Manageable(lifestyle = Singleton.class)
    private static class HasCustomLifestyleInPrivateModule {

        /**
         * Custom lifestyle.
         * 
         * @version 2010/01/15 18:54:13
         */
        @SuppressWarnings("unused")
        private static class Custom1 implements Lifestyle<HasCustomLifestyleInPrivateModule> {

            /**
             * @see kiss.Lifestyle#get()
             */
            @Override
            public HasCustomLifestyleInPrivateModule get() {
                return new HasCustomLifestyleInPrivateModule();
            }
        }

        /**
         * Custom lifestyle.
         * 
         * @version 2010/01/15 18:54:13
         */
        @SuppressWarnings("unused")
        private static class Custom2 implements Lifestyle<HasCustomLifestyleInPrivateModule> {

            /**
             * @see kiss.Lifestyle#get()
             */
            @Override
            public HasCustomLifestyleInPrivateModule get() {
                return new HasCustomLifestyleInPrivateModule();
            }
        }
    }

    /**
     * @version 2010/01/16 13:10:29
     */
    @SuppressWarnings("unused")
    private static class PersonLifestyle extends Prototype<Person> {

        public PersonLifestyle() {
            super(Person.class);
        }

        /**
         * @see kiss.Prototype#get()
         */
        @Override
        public Person get() {
            Person person = super.get();

            person.setFirstName(CONSTANT);

            return person;
        }
    }

    /**
     * @version 2010/01/16 13:10:29
     */
    @SuppressWarnings("unused")
    private static class StudentLifestyle extends Prototype<Student> {

        public StudentLifestyle() {
            super(Student.class);
        }

        /**
         * @see kiss.Prototype#get()
         */
        @Override
        public Student get() {
            Student person = super.get();

            person.setFirstName(CONSTANT);

            return person;
        }
    }

    /**
     * @version 2010/01/22 1:20:16
     */
    private static class CustomLifestyle<M> extends Prototype<M> {

        private static Set set = new HashSet();

        public CustomLifestyle(Class<M> modelClass) {
            super(modelClass);
            assert CustomClass.class.equals(modelClass);
        }

        /**
         * @see kiss.Prototype#get()
         */
        @Override
        public M get() {
            M m = super.get();

            set.add(m);

            return m;
        }
    }

    @Manageable(lifestyle = CustomLifestyle.class)
    private static class CustomClass {

    }

    /**
     * @version 2011/04/11 13:05:55
     */
    private static interface Interface {
    }

    /**
     * @version 2011/04/11 13:06:14
     */
    private static class InterfaceImplementation implements Interface {
    }

    /**
     * @version 2011/04/11 13:06:55
     */
    private static class InterfaceLifestyle implements Lifestyle<Interface> {

        private static InterfaceImplementation implementation = new InterfaceImplementation();

        /**
         * @see kiss.Lifestyle#get()
         */
        @Override
        public Interface get() {
            return implementation;
        }
    }
}
