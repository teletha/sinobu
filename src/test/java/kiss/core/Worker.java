/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.Optional;

import kiss.I;

/**
 * @version 2015/04/19 10:09:15
 */
public class Worker extends Person {

    public final String company;

    /**
     * @param name
     * @param age
     */
    Worker(String name, int age, String company) {
        super(name, age);

        this.company = company;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Worker [company=" + company + ", name=" + name + ", age=" + age + "]";
    }

    /**
     * 
     */
    public static class WorkerOperator<T extends WorkerOperator, M extends Worker> extends PersonOperator<T, M> {

        public static final Lens<Worker, String> _company_ = new Lens<Worker, String>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Optional<String> get(Worker model) {
                return Optional.of(model.company);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Worker set(Worker model, String property) {
                return ((WorkerOperator) I.find(Operator.class, model.getClass())).copy(model)
                        .company(property)
                        .build();
            }
        };

        protected String company;

        /**
         * {@inheritDoc}
         */
        @Override
        protected T copy(M base) {
            return (T) super.copy(base).company(base.company);
        }

        public T company(String company) {
            this.company = company;
            return (T) this;
        }

        @Override
        public Worker build() {
            return new Worker(name, age, company);
        }
    }
}
