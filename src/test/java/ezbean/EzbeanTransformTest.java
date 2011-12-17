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

import org.junit.Test;

/**
 * @version 2011/03/22 16:50:58
 */
public class EzbeanTransformTest {

    @Test
    public void inputNull() throws Exception {
        assert I.transform(null, int.class) == null;
        assert I.transform(null, String.class) == null;
    }

    @Test(expected = NullPointerException.class)
    public void outputNull() throws Exception {
        assert I.transform("1", null) == null;
    }

    @Test
    public void transformInt() {
        assert I.transform("1", int.class) == 1;
        assert I.transform(1, String.class).equals("1");
    }

    @Test
    public void transformLong() {
        assert I.transform("1", long.class) == 1L;
        assert I.transform(1L, String.class).equals("1");
    }

    @Test
    public void transformChar() {
        assert I.transform("1", char.class) == '1';
        assert I.transform('1', String.class).equals("1");
    }

    @Test
    public void transformFloat() {
        assert I.transform("1.3", float.class) == 1.3f;
        assert I.transform(1.3f, String.class).equals("1.3");
    }

    @Test
    public void transformDouble() {
        assert I.transform("1.3", double.class) == 1.3d;
        assert I.transform(1.3d, String.class).equals("1.3");
    }

    @Test
    public void transformBoolean() {
        assert I.transform("true", boolean.class);
        assert I.transform(true, String.class).equals("true");
    }

    @Test
    public void transformBean() {
        BeanA a = I.make(BeanA.class);
        a.setValue(10);

        // initial value
        assert 10 == a.getValue();

        // transform
        BeanB b = I.transform(a, BeanB.class);

        // initial value
        assert 10 == a.getValue();
        assert b.getValue().equals("10");
    }

    /**
     * @version 2011/03/15 15:09:16
     */
    protected static class BeanA {

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
     * @version 2011/03/15 15:09:20
     */
    protected static class BeanB {

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
