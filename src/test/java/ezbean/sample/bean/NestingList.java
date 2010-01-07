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

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: NestingList.java,v 1.0 2007/01/16 19:01:10 Teletha Exp $
 */
public class NestingList {

    private List<List<Integer>> nesting;

    /**
     * Get the nesting property of this {@link NestingList}.
     * 
     * @return The nesting prperty.
     */
    public List<List<Integer>> getNesting() {
        return nesting;
    }

    /**
     * Set the nesting property of this {@link NestingList}.
     * 
     * @param nesting The nesting value to set.
     */
    public void setNesting(List<List<Integer>> nesting) {
        this.nesting = nesting;
    }

}
