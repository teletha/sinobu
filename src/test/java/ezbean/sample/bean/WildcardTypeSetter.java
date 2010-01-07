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

import java.io.Serializable;
import java.util.List;

/**
 * DOCUMENT.
 * 
 * @version 2007/05/31 23:44:43
 */
public class WildcardTypeSetter {

    public List<Serializable> getList() {
        return null;
    }

    public void setList(List<? extends Serializable> list) {
    }
}
