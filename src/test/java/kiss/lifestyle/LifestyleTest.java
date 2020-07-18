/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Lifestyle;
import kiss.LoadableTestBase;
import kiss.Managed;
import kiss.Singleton;
import kiss.sample.bean.Person;
import kiss.sample.bean.Student;

/**
 * @version 2014/07/12 11:40:46
 */
public class LifestyleTest extends LoadableTestBase {

    private static final String CONSTANT = "Set by " + LifestyleTest.class.getSimpleName();

    @Test
    public void override() {
        HasCustomLifestyleInPrivateModule object = I.make(HasCustomLifestyleInPrivateModule.class);
        assert object != I.make(HasCustomLifestyleInPrivateModule.class);

        // unload module
        unloadClasses();

        object = I.make(HasCustomLifestyleInPrivateModule.class);
        assert object == I.make(HasCustomLifestyleInPrivateModule.class);
    }

    @Test
    public void unload() {
        assert WithoutLifestyle.object == I.make(Without.class);
        assert WithoutLifestyle.object == I.make(Without.class);

        // unload module
        unloadClasses();

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
        assert Student.class.isInstance(person);
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
     * External class without {@link Managed} annotation.
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
         * {@inheritDoc}
         */
        @Override
        public Without call() throws Exception {
            return object;
        }
    }

    /**
     * <p>
     * External class with {@link Managed} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    @Managed(value = Singleton.class)
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
            public HasCustomLifestyleInPrivateModule call() throws Exception {
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
            public HasCustomLifestyleInPrivateModule call() throws Exception {
                return new HasCustomLifestyleInPrivateModule();
            }
        }
    }

    @SuppressWarnings("unused")
    private static class PersonLifestyle implements Lifestyle<Person> {

        @Override
        public Person call() {
            Person person = new Person();
            person.setFirstName(CONSTANT);

            return person;
        }
    }

    @SuppressWarnings("unused")
    private static class StudentLifestyle implements Lifestyle<Student> {

        @Override
        public Student call() {
            Student person = new Student();

            person.setFirstName(CONSTANT);

            return person;
        }
    }

    static class CustomLifestyle<M> implements Lifestyle<M> {

        private static Set set = new HashSet();

        private Lifestyle<M> lifestyle;

        public CustomLifestyle(Class<M> modelClass) {
            this.lifestyle = I.prototype(modelClass);
            assert CustomClass.class.equals(modelClass);
        }

        @Override
        public M call() {
            M m = lifestyle.get();
            set.add(m);

            return m;
        }
    }

    @Managed(value = CustomLifestyle.class)
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
        public Interface call() throws Exception {
            return implementation;
        }
    }
}