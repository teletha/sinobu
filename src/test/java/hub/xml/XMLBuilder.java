/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.xml;

import hub.ReusableRule;

import java.nio.file.Path;

/**
 * @version 2012/01/23 13:29:22
 */
public class XMLBuilder extends ReusableRule {

    public XMLBuilder ignoreAttributeOrder() {

        return this;
    }

    public XMLBuilder ignoreComment() {

        return this;
    }

    public XMLBuilder ignoreWhiteSpace() {
        return this;
    }

    public XML build(Path path) {
        return null;
    }
}
