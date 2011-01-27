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

/**
 * @version 2009/07/03 17:33:46
 */
public class ArrayBean {

    private String[] objects;

    private int[] primitives;

    /**
     * Get the objects property of this {@link ArrayBean}.
     * 
     * @return The objects prperty.
     */
    public String[] getObjects() {
        return objects;
    }

    /**
     * Set the objects property of this {@link ArrayBean}.
     * 
     * @param The objects value to set.
     */
    public void setObjects(String[] objects) {
        this.objects = objects;
    }

    /**
     * Get the primitives property of this {@link ArrayBean}.
     * 
     * @return The primitives prperty.
     */
    public int[] getPrimitives() {
        return primitives;
    }

    /**
     * Set the primitives property of this {@link ArrayBean}.
     * 
     * @param The primitives value to set.
     */
    public void setPrimitives(int[] primitives) {
        this.primitives = primitives;
    }

}
