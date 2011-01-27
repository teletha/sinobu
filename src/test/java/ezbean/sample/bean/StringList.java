/**
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
package ezbean.sample.bean;

import java.util.List;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: StringList.java,v 1.0 2007/01/16 19:08:54 Teletha Exp $
 */
public class StringList {

    private List<String> list;

    /**
     * Get the list property of this {@link StringList}.
     * 
     * @return The list prperty.
     */
    public List<String> getList() {
        return list;
    }

    /**
     * Set the list property of this {@link StringList}.
     * 
     * @param list The list value to set.
     */
    public void setList(List<String> list) {
        this.list = list;
    }

}
