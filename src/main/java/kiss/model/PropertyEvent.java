/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.beans.PropertyChangeEvent;

/**
 * @version 2014/01/24 2:19:53
 */
@SuppressWarnings("serial")
public class PropertyEvent<T> extends PropertyChangeEvent {

    /**
     * @param source
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    public PropertyEvent(Object source, String propertyName, T oldValue, T newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNewValue() {
        return (T) super.getNewValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getOldValue() {
        return (T) super.getOldValue();
    }
}
