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

import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.org.apache.xml.internal.utils.DOMBuilder;

/**
 * @version 2012/02/16 15:16:19
 */
class DOMStreamer extends DOMBuilder implements XMLFilter {

    private XMLFilter filter = new XMLFilterImpl();

    /**
     * @param doc
     */
    public DOMStreamer(Document doc) {
        super(doc);

        setContentHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    public void setParent(XMLReader parent) {
        filter.setParent(parent);
    }

    /**
     * {@inheritDoc}
     */
    public XMLReader getParent() {
        return filter.getParent();
    }

    /**
     * {@inheritDoc}
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return filter.getFeature(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        filter.setFeature(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return filter.getProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        filter.setProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void setEntityResolver(EntityResolver resolver) {
        filter.setEntityResolver(resolver);
    }

    /**
     * {@inheritDoc}
     */
    public EntityResolver getEntityResolver() {
        return filter.getEntityResolver();
    }

    /**
     * {@inheritDoc}
     */
    public void setDTDHandler(DTDHandler handler) {
        filter.setDTDHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    public DTDHandler getDTDHandler() {
        return filter.getDTDHandler();
    }

    /**
     * {@inheritDoc}
     */
    public void setContentHandler(ContentHandler handler) {
        filter.setContentHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    public ContentHandler getContentHandler() {
        return filter.getContentHandler();
    }

    /**
     * {@inheritDoc}
     */
    public void setErrorHandler(ErrorHandler handler) {
        filter.setErrorHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    public ErrorHandler getErrorHandler() {
        return filter.getErrorHandler();
    }

    /**
     * {@inheritDoc}
     */
    public void parse(InputSource input) throws IOException, SAXException {
        filter.parse(input);
    }

    /**
     * {@inheritDoc}
     */
    public void parse(String systemId) throws IOException, SAXException {
        filter.parse(systemId);
    }

}
