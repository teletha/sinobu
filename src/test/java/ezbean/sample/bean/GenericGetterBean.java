/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.sample.bean;

import java.util.List;
import java.util.Map;

/**
 * @version 2009/07/17 14:52:13
 */
public class GenericGetterBean extends GenericBean<String> {

    /**
     * @see ezbean.sample.bean.GenericBean#getGeneric()
     */
    @Override
    public String getGeneric() {
        return super.getGeneric();
    }

    /**
     * @see ezbean.sample.bean.GenericBean#getGenericList()
     */
    @Override
    public List<String> getGenericList() {
        return super.getGenericList();
    }

    /**
     * @see ezbean.sample.bean.GenericBean#getGenericMap()
     */
    @Override
    public Map<String, String> getGenericMap() {
        return super.getGenericMap();
    }

}
