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
package ezbean.scratchpad;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @version 2010/02/04 1:31:09
 */
public class NCATest {

    /**
     * <p>
     * Retrieve the nearest common ancestor class of the given classes.
     * </p>
     * 
     * @param <X>
     * @param classes A set of classes.
     * @return A nearest common ancestor class.
     */
    public static <X> Class getNCA(X... classes) {
        return classes.getClass().getComponentType();
    }

    @Test
    public void testname() throws Exception {
        assertEquals(Number.class, getNCA(1, 2d, 3f));
        assertEquals(String.class, getNCA("test"));
        assertEquals(Exception.class, getNCA(new IOException(), new SAXException()));

        // Class AbstractStringBuilder = getNCA(new StringBuffer(), new StringBuilder());
    }

}
