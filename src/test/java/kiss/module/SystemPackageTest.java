/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.JFrame;

import org.junit.Test;

import kiss.I;

/**
 * @version 2011/03/22 17:05:52
 */
public class SystemPackageTest {

    @Test
    public void defineClassInCoreSystemPackage1() throws Exception {
        Date date = I.make(Date.class);
        assert date != null;
        assert Date.class != date.getClass();
    }

    @Test
    public void defineClassInCoreSystemPackage2() throws Exception {
        Dimension dimension = I.make(Dimension.class);
        assert dimension != null;
        assert Dimension.class != dimension.getClass();
    }

    @Test
    public void defineClassInExtendedSystemPackage() throws Exception {
        JFrame frame = I.make(JFrame.class);
        assert frame != null;
        assert JFrame.class != frame.getClass();
    }
}
