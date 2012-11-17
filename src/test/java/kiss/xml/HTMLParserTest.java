/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import kiss.I;

import org.junit.Test;
import org.w3c.dom.Document;

import antibug.AntiBug;
import antibug.util.Note;

/**
 * @version 2012/11/17 1:41:59
 */
public class HTMLParserTest {

    @Test
    public void html() throws Exception {
        Note note = AntiBug.note("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html class=\"root\"><link href='aa'><script>var as ='<html></html>';</script><body><p class=\"aaa\">first<p style='margin:12em;'>1v3とか(゜ェ゜；　三　；゜ェ゜) ヒイイイィィ<div><img src='src' editable></div></body></html>");
        // System.out.println(note);
        Document doc = Parser.parse(note.toString());
        System.out.println(I.xml(doc));
    }
}
