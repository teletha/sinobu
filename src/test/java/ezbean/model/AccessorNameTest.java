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
package ezbean.model;

import org.junit.Test;

import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;

/**
 * @version 2011/03/22 17:05:12
 */
public class AccessorNameTest {

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void normal() throws Exception {
        Model model = Model.load(Person.class);
        assert model != null;

        Property property = model.getProperty("age");
        assert property.accessors[0].getName().equals("getAge");
        assert property.accessors[1].getName().equals("setAge");
    }

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void primitive() throws Exception {
        Model model = Model.load(Primitive.class);
        assert model != null;

        Property property = model.getProperty("boolean");
        assert property.accessors[0].getName().equals("isBoolean");
        assert property.accessors[1].getName().equals("setBoolean");

        property = model.getProperty("int");
        assert property.accessors[0].getName().equals("getInt");
        assert property.accessors[1].getName().equals("setInt");
    }
}
