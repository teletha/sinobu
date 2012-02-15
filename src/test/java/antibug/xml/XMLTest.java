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

/**
 * @version 2012/02/15 1:18:03
 */
public class XMLTest {

    @Test
    public void testname() throws Exception {
        XML xml = xml("<root><one/><one a='ab'/></root>");
        XML fail = xml("<root><one/><one a='aa'/></root>");

        assert xml.equals(fail);
    }
}
