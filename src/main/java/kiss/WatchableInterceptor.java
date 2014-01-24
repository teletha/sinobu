/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Objects;

import javax.jws.Oneway;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2014/01/23 22:21:06
 */
class WatchableInterceptor extends Interceptor<Oneway> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object invoke(Object... params) {
        Property property = Model.load(that.getClass()).getProperty(Introspector.decapitalize(name.substring(3)));
        List<Observer> list = Interceptor.context(that).get(property.name);

        if (list == null) {
            return super.invoke(params);
        }

        try {
            // Retrieve old value.
            Object old = property.accessor(true).invoke(that);

            Object result = super.invoke(params);

            if (!Objects.equals(old, params[0])) {
                PropertyChangeEvent event = new PropertyChangeEvent(that, property.name, old, params[0]);

                for (Observer observer : Interceptor.context(that).get(property.name)) {
                    observer.onNext(event);
                }
            }

            return result;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
