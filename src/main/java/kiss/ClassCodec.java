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

import java.util.Locale;

/**
 * <p>
 * This is dual-purpose implementation class. One is codec for {@link Class}. The other is lifestyle
 * for {@link Locale}.
 * </p>
 * <p>
 * This class locates Sinobu package to access {@link Modules}'s package-private field.
 * </p>
 * 
 * @version 2013/07/27 4:11:17
 */
class ClassCodec implements Codec<Class> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class decode(String value) {
        for (Module module : I.modules.modules) {
            try {
                return Class.forName(value, false, module.loader);
            } catch (ClassNotFoundException e) {
                // continue
            }
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Class value) {
        return value.getName();
    }
}
