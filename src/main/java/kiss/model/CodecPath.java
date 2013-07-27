/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.io.File;
import java.nio.file.Path;

import kiss.I;

/**
 * @version 2012/11/11 14:55:05
 */
class CodecPath extends Codec<Path> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Path value) {
        return value.toString().replace(File.separatorChar, '/');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path decode(String value) {
        return I.locate(value);
    }
}
