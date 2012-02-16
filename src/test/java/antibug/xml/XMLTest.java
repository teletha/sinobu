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

import static antibug.xml.XML.*;

import org.junit.Test;

import antibug.AntiBug;

/**
 * @version 2012/02/16 14:42:48
 */
public class XMLTest {

    @Test
    public void elementLocalName() throws Exception {
        XML one = AntiBug.xml("<Q/>");
        XML other = AntiBug.xml("<P/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void elementURI() throws Exception {
        XML one = AntiBug.xml("<m xmlns='P'/>");
        XML other = AntiBug.xml("<m xmlns='Q'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void elementName() throws Exception {
        XML one = AntiBug.xml("<Q:m xmlns:Q='ns'/>");
        XML other = AntiBug.xml("<P:m xmlns:P='ns'/>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Prefix());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);

    }

    @Test
    public void attributeLocalName() throws Exception {
        XML one = AntiBug.xml("<m Q='value'/>");
        XML other = AntiBug.xml("<m P='value'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void attributeURI() throws Exception {
        XML one = AntiBug.xml("<m name='value' xmlns='Q'/>");
        XML other = AntiBug.xml("<m name='value' xmlns='P'/>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void attributeName() throws Exception {
        XML one = AntiBug.xml("<m Q:n='' xmlns:Q='ns'/>");
        XML other = AntiBug.xml("<m P:n='' xmlns:P='ns'/>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Prefix());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void attributes() throws Exception {
        XML one = AntiBug.xml("<m P='p' Q='q'/>");
        XML other = AntiBug.xml("<m Q='q' P='p'/>");

        assert one.isEqualTo(other);
    }

    @Test
    public void text() throws Exception {
        XML one = AntiBug.xml("<m>Q</m>");
        XML other = AntiBug.xml("<m>P</m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void whitespace() throws Exception {
        XML one = AntiBug.xml("<m></m>");
        XML other = AntiBug.xml("<m>  </m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.WhiteSpace());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void textWithWhitespace() throws Exception {
        XML one = AntiBug.xml("<m>Q</m>");
        XML other = AntiBug.xml("<m> Q </m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.WhiteSpace());
        assert !one.isEqualTo(other, Except.Comment());
        assert !one.isIdenticalTo(other);
    }

    @Test
    public void child() throws Exception {
        XML one = AntiBug.xml("<m><Q/></m>");
        XML other = AntiBug.xml("<m><P/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void childrenOrder() throws Exception {
        XML one = AntiBug.xml("<m><Q/><P/></m>");
        XML other = AntiBug.xml("<m><P/><Q/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void childrenSize() throws Exception {
        XML one = AntiBug.xml("<m><Q/></m>");
        XML other = AntiBug.xml("<m><Q/><P/></m>");

        assert !one.isEqualTo(other);
    }

    @Test
    public void comment() throws Exception {
        XML one = AntiBug.xml("<m><!-- Q --></m>");
        XML other = AntiBug.xml("<m><!-- P --></m>");

        assert one.isEqualTo(other);
        assert one.isEqualTo(other, Except.Comment());
        assert !one.isEqualTo(other, Except.Prefix());
        assert !one.isIdenticalTo(other);
    }
}
