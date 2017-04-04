/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.tree;

import java.io.File;

import org.junit.Test;

import kiss.I;
import kiss.XML;

/**
 * @version 2017/04/02 21:59:13
 */
public class TreeWalkerTest {
    @Test
    public void walk() throws Exception {
        I.signal(new File("src/main"), e -> e.flatArray(File::listFiles)).to(e -> {
            System.out.println(e);
        });
    }

    public void xml() throws Exception {
        I.signal(I.xml("<x><a><o/><p/></a><b/><c><q/><r/></c></x>"), e -> e.flatIterable(XML::children).skip(a -> a.name().equals("c")))
                .to(e -> {
                    System.out.println(e.name());
                });
    }
}
