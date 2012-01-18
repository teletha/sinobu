/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

/**
 * @version 2012/01/18 0:47:32
 */
interface Recoder {

    /**
     * <p>
     * Clear current context.
     * </p>
     */
    void clear();

    /**
     * <p>
     * Recode constant value.
     * </p>
     * 
     * @param constant
     */
    void constant(Object constant);

    /**
     * <p>
     * Recode local variable value.
     * </p>
     * 
     * @param id A local variable id.
     * @param variable A value.
     */
    void localVariable(int id, Object variable);

    /**
     * <p>
     * Recode field access.
     * </p>
     * 
     * @param expression
     * @param variable
     */
    void field(String expression, Object variable);

    /**
     * <p>
     * Recode static field access.
     * </p>
     * 
     * @param expression
     * @param variable
     */
    void staticField(String expression, Object variable);

    /**
     * <p>
     * Recode operator.
     * </p>
     * 
     * @param expression
     */
    void operator(String operator);

    /**
     * <p>
     * Recode increment operation.
     * </p>
     * 
     * @param id A local variable id.
     * @param increment A increment value.
     */
    void recodeIncrement(int id, int increment);

    /**
     * <p>
     * Recode negative value operation.
     * </p>
     */
    void negative();

    /**
     * <p>
     * Recode instanceof operation.
     * </p>
     * 
     * @param className
     */
    void instanceOf(String className);

    /**
     * <p>
     * Recode method call.
     * </p>
     * 
     * @param name A method name.
     * @param description A method parameter size.
     * @param value A returned value.
     */
    void method(String name, String description, Object value);

    /**
     * <p>
     * Recode static method call.
     * </p>
     * 
     * @param name A method name.
     * @param description A method parameter size.
     * @param value A returned value.
     */
    void staticMethod(String name, String description, Object value);

    /**
     * <p>
     * Recode constructor call.
     * </p>
     * 
     * @param name A constructor name.
     * @param description A constructor parameter size.
     * @param value A constructed value.
     */
    void constructor(String name, String description, Object value);

    /**
     * <p>
     * Recode array index acess.
     * </p>
     */
    void arrayIndex(Object value);
}