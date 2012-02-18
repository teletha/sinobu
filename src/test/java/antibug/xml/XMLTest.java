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

import static antibug.AntiBug.*;

import org.junit.Test;

/**
 * @version 2012/02/16 14:42:48
 */
public class XMLTest {

    @Test
    public void elementLocalName() throws Exception {
        XML one = xml("<Q/>");
        XML other = xml("<P/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void elementURI() throws Exception {
        XML one = xml("<m xmlns='P'/>");
        XML other = xml("<m xmlns='Q'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void elementName() throws Exception {
        XML one = xml("<Q:m xmlns:Q='ns'/>");
        XML other = xml("<P:m xmlns:P='ns'/>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Prefix());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);

    }

    @Test
    public void attributeLocalName() throws Exception {
        XML one = xml("<m Q='value'/>");
        XML other = xml("<m P='value'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void attributeURI() throws Exception {
        XML one = xml("<m name='value' xmlns='Q'/>");
        XML other = xml("<m name='value' xmlns='P'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void attributeName() throws Exception {
        XML one = xml("<m Q:n='' xmlns:Q='ns'/>");
        XML other = xml("<m P:n='' xmlns:P='ns'/>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Prefix());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void attributes() throws Exception {
        XML one = xml("<m P='p' Q='q'/>");
        XML other = xml("<m Q='q' P='p'/>");

        assert one.isEqualTo(other);
    }

    @Test
    public void text() throws Exception {
        XML one = xml("<m>Q</m>");
        XML other = xml("<m>P</m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void whitespace() throws Exception {
        XML one = xml("<m></m>");
        XML other = xml("<m>  </m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.WhiteSpace());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void textWithWhitespace() throws Exception {
        XML one = xml("<m>Q</m>");
        XML other = xml("<m> Q </m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.WhiteSpace());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void child() throws Exception {
        XML one = xml("<m><Q/></m>");
        XML other = xml("<m><P/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void childrenOrder() throws Exception {
        XML one = xml("<m><Q/><P/></m>");
        XML other = xml("<m><P/><Q/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void childrenSize() throws Exception {
        XML one = xml("<m><Q/></m>");
        XML other = xml("<m><Q/><P/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void comment() throws Exception {
        XML one = xml("<m><!-- Q --></m>");
        XML other = xml("<m><!-- P --></m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Comment());
        assert !one.isEqualTo(other, Except.Prefix());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void xpathElement() throws Exception {
        XML xml = xml("<m><Q/><P/><Q/></m>");

        assert xml.has("m/P");
        assert xml.has("m/Q");
        assert !xml.has("m/R");
    }

    @Test
    public void xpathAttribute() throws Exception {
        XML xml = xml("<m Q='P'/>");

        assert xml.has("m/@Q");
        assert !xml.has("m/@P");
    }

    @Test
    public void xpathElementNS() throws Exception {
        XML xml = xml("<m><Q:P xmlns:Q='ns'/></m>");

        assert xml.has("m/Q:P", "Q", "ns");
        assert !xml.has("m/Q:P", "Q", "not");
    }

    @Test
    public void xpathAttributeNS() throws Exception {
        XML xml = xml("<m Q:P='P' xmlns:Q='ns'/>");

        assert xml.has("m/@Q:P", "Q", "ns");
        assert !xml.has("m/@Q:P", "Q", "not");
    }
}
