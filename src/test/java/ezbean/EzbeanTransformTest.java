/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/13 10:51:09
 */
public class EzbeanTransformTest {

    /**
     * <code>int</code> and {@link String}
     */
    @Test
    public void testTransform01() {
        assertEquals(1, (int) I.transform("1", int.class));
        assertEquals("1", I.transform(1, String.class));
    }

    /**
     * <code>long</code> and {@link String}
     */
    @Test
    public void testTransform02() {
        assertEquals(1L, (long) I.transform("1", long.class));
        assertEquals("1", I.transform(1L, String.class));
    }

    /**
     * <code>char</code> and {@link String}
     */
    @Test
    public void testTransform03() {
        assertEquals('1', (char) I.transform("1", char.class));
        assertEquals("1", I.transform('1', String.class));
    }

    /**
     * <code>float</code> and {@link String}
     */
    @Test
    public void testTransform04() {
        assertEquals(1.3f, (float) I.transform("1.3", float.class), 0);
        assertEquals("1.3", I.transform(1.3f, String.class));
    }

    /**
     * <code>double</code> and {@link String}
     */
    @Test
    public void testTransform05() {
        assertEquals(1.3d, (double) I.transform("1.3", double.class), 0);
        assertEquals("1.3", I.transform(1.3d, String.class));
    }

    /**
     * <code>boolean</code> and {@link String}
     */
    @Test
    public void testTransform06() {
        assertTrue((boolean) I.transform("true", boolean.class));
        assertEquals("true", I.transform(true, String.class));
    }

    /**
     * Bean and Bean
     */
    @Test
    public void testTransform20() {
        BeanA a = I.make(BeanA.class);
        a.setValue(10);

        // initial value
        assertEquals(10, a.getValue());

        // transform
        BeanB b = I.transform(a, BeanB.class);

        // initial value
        assertEquals(10, a.getValue());
        assertEquals("10", b.getValue());
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/08/13 10:51:03
     */
    public static class BeanA {

        /** The property. */
        private int value;

        /**
         * Get the value property of this {@link EzbeanTransformTest.BeanA1}.
         * 
         * @return The value property.
         */
        public int getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link EzbeanTransformTest.BeanA1}.
         * 
         * @param value The value value to set.
         */
        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/08/13 10:50:59
     */
    public static class BeanB {

        /** The property. */
        private String value;

        /**
         * Get the value property of this {@link EzbeanTransformTest.BeanA1}.
         * 
         * @return The value property.
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link EzbeanTransformTest.BeanA1}.
         * 
         * @param value The value value to set.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
