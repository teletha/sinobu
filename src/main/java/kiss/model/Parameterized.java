/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class Parameterized implements ParameterizedType {

    private final ParameterizedType param;

    private final Type[] types;

    /**
     * @param param
     */
    Parameterized(ParameterizedType param, Type[] types) {
        this.param = param;
        this.types = types;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type[] getActualTypeArguments() {
        return types;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getRawType() {
        return param.getRawType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getOwnerType() {
        return param.getOwnerType();
    }
}
