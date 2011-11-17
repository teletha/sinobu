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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;

/**
 * @version 2011/03/22 16:56:52
 */
public class PropertyComparatorTest {

    /**
     * Test order.
     */
    @Test
    public void testCompare() {
        List<Property> properties = new ArrayList();

        properties.add(new Property(Model.load(School.class), "aaa"));
        properties.add(new Property(Model.load(String.class), "string"));
        properties.add(new Property(Model.load(int.class), "int"));
        properties.add(new Property(Model.load(Person.class), "abc"));
        properties.add(new Property(Model.load(String.class), "first"));

        // create innocuous BeanModel as property comparator and sort above properties by it
        Collections.sort(properties);

        assert "first" == properties.get(0).name;
        assert "int" == properties.get(1).name;
        assert "string" == properties.get(2).name;
        assert "aaa" == properties.get(3).name;
        assert "abc" == properties.get(4).name;
    }
}
