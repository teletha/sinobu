/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean;

import org.junit.Rule;
import org.junit.Test;

import ezbean.ClassCodec;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 17:03:59
 */
public class ClassCodecTest {

    @Rule
    public static final PrivateModule module = new PrivateModule(true, false);

    @Test
    public void systemClass() throws Exception {
        ClassCodec codec = new ClassCodec();
        Class clazz = codec.decode("java.lang.String");
        assert clazz != null;
        assert codec.encode(clazz).equals("java.lang.String");
    }

    @Test
    public void moduleClass() throws Exception {
        Class clazz = module.convert(Private.class);
        assert Private.class != clazz;

        ClassCodec codec = new ClassCodec();
        String fqcn = codec.encode(clazz);
        assert Private.class.getName() != fqcn;
        assert codec.decode(fqcn).equals(clazz);
    }

    /**
     * @version 2010/02/04 9:43:23
     */
    private static class Private {
    }
}
