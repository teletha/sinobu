/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.model;

import java.nio.file.Path;

import ezbean.I;

/**
 * @version 2011/12/25 17:29:44
 */
class CodecPath extends Codec<Path> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Path value) {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path decode(String value) {
        return I.locate(value);
    }
}
