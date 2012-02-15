/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import antibug.powerassert.PowerAssertRenderer;

/**
 * @version 2012/02/15 11:55:46
 */
final class AttributeRenderer extends PowerAssertRenderer<Attr> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String render(Attr attr) {
        Element value = attr.getOwnerElement();

        StringBuilder builder = new StringBuilder();
        builder.append('<').append(value.getNodeName());
        builder.append(' ').append(attr.getName()).append("=\"").append(attr.getValue()).append('"');

        if (!value.hasChildNodes()) {
            builder.append('/');
        }
        builder.append('>');

        return builder.toString();
    }
}