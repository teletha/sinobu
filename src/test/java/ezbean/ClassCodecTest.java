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
