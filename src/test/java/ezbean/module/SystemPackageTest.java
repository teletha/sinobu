/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import static org.junit.Assert.*;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.JFrame;

import org.junit.Test;

import ezbean.I;

/**
 * @version 2010/10/26 23:54:09
 */
public class SystemPackageTest {

    @Test
    public void defineClassInCoreSystemPackage1() throws Exception {
        Date date = I.make(Date.class);
        assertNotNull(date);
        assertNotSame(Date.class, date.getClass());
    }

    @Test
    public void defineClassInCoreSystemPackage2() throws Exception {
        Dimension dimension = I.make(Dimension.class);
        assertNotNull(dimension);
        assertNotSame(Dimension.class, dimension.getClass());
    }

    @Test
    public void defineClassInExtendedSystemPackage() throws Exception {
        JFrame frame = I.make(JFrame.class);
        assertNotNull(frame);
        assertNotSame(JFrame.class, frame.getClass());
    }
}
