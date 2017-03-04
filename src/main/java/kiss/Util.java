/*
 * Copyright (C) 2016 Nameless Production Committee
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
 * @version 2017/03/04 19:11:00
 */
class Util extends ClassVariable<Lifestyle> implements Decoder<Class>, Encoder<Class>, Lifestyle<Locale> {

    /**
     * Avoid construction
     */
    Util() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class decode(String value) {
        return I.type(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Class value) {
        return value.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale get() {
        return Locale.getDefault();
    }
}
