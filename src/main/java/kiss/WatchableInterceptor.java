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

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyEvent;

/**
 * @version 2014/01/23 22:21:06
 */
class WatchableInterceptor extends Interceptor<Watchable> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object invoke(Object... params) {
        Model model = Model.load(that.getClass());
        Property property = model.getProperty(Introspector.decapitalize(name.substring(3)));

        try {
            // Retrieve old value.
            Object old = property.accessor(true).invoke(that);

            Object result = super.invoke(params);

            PropertyEvent event = new PropertyEvent(that, property.name, old, params[0]);

            for (Observer observer : Interceptor.context(that).get(property.name)) {
                observer.onNext(event);
            }

            return result;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
