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

import com.sun.org.apache.xerces.internal.util.DOMUtil;

/**
 * @version 2012/02/15 11:55:46
 */
final class ElementRenderer extends PowerAssertRenderer<Element> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String render(Element value) {
        StringBuilder builder = new StringBuilder();
        builder.append('<').append(value.getNodeName());

        for (Attr attribute : DOMUtil.getAttrs(value)) {
            builder.append(' ').append(attribute.getName()).append("=\"").append(attribute.getValue()).append('"');
        }
        if (!value.hasChildNodes()) {
            builder.append('/');
        }
        builder.append('>');

        return builder.toString();
    }
}