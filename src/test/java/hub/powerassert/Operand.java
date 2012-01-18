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
 * @version 2012/01/11 14:11:46
 */
class Operand {

    /** The human redable expression. */
    String name;

    /** The actual value. */
    Object value;

    /** The constant flag. */
    boolean constant;

    /**
     * 
     */
    Operand(Object value) {
        if (value instanceof String) {
            this.name = "\"" + value + "\"";
        } else if (value instanceof Class) {
            this.name = ((Class) value).getSimpleName() + ".class";
        } else {
            this.name = String.valueOf(value);
        }
        this.value = value;
        this.constant = true;
    }

    /**
     * 
     */
    Operand(String name, Object value) {
        this.name = name;
        this.value = value;
        this.constant = false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Operand other = (Operand) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (value == null) {
            if (other.value != null) return false;
        } else if (!value.equals(other.value)) return false;
        return true;
    }

    /**
     * <p>
     * Compute human-readable expression of value.
     * </p>
     * 
     * @return
     */
    String toValueExpression() {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Class) {
            return ((Class) value).getSimpleName() + ".class";
        } else if (value instanceof Enum) {
            Enum enumration = (Enum) value;
            return enumration.getDeclaringClass().getSimpleName() + '.' + enumration.name();
        } else {
            return value.toString();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}