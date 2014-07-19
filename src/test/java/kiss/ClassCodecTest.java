/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;

/**
 * @version 2013/07/27 3:53:40
 */
public class ClassCodecTest {

    @Rule
    public static final PrivateModule module = new PrivateModule(true, false);

    @Test
    public void systemClass() throws Exception {
        Modules codec = new Modules();
        Class clazz = codec.decode("java.lang.String");
        assert clazz != null;
        assert codec.encode(clazz).equals("java.lang.String");
    }

    @Test
    public void systemClassArray() throws Exception {
        Modules codec = new Modules();
        Class clazz = codec.decode("[Ljava.lang.String;");
        assert clazz != null;
        assert codec.encode(clazz).equals("[Ljava.lang.String;");
    }

    @Test
    public void moduleClass() throws Exception {
        Class clazz = module.convert(Private.class);
        assert Private.class != clazz;

        Modules codec = new Modules();
        String fqcn = codec.encode(clazz);
        assert Private.class.getName() != fqcn;
        assert codec.decode(fqcn).equals(clazz);
    }

    @Test
    public void moduleClassArray() throws Exception {
        Class clazz = module.convert(Private[].class);
        assert Private[].class != clazz;

        Modules codec = new Modules();
        String fqcn = codec.encode(clazz);
        assert Private[].class.getName() != fqcn;
        assert codec.decode(fqcn).equals(clazz);
    }

    /**
     * @version 2010/02/04 9:43:23
     */
    private static class Private {
    }
}
