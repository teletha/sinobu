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
package ezbean.scratchpad;

import ezbean.Accessible;
import ezbean.Interceptor;
import ezbean.Listeners;

/**
 * @version 2009/12/27 17:21:35
 */
public class ASMfierDebugBean extends ASMfierDebugBase implements Accessible {

    @SuppressWarnings("unused")
    private Listeners ezContext;

    /**
     * @see ezbean.scratchpad.ASMfierDebugBase#getAge()
     */
    @Override
    public int getAge() {
        return super.getAge();
    }

    /**
     * @see ezbean.scratchpad.ASMfierDebugBase#getName()
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * @see ezbean.scratchpad.ASMfierDebugBase#setAge(int)
     */
    @Override
    public void setAge(int age) {
        super.setAge(age);
    }

    /**
     * @see ezbean.scratchpad.ASMfierDebugBase#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        if (ezContext == null) {
            super.setName(name);
        } else {
            Interceptor.invoke(this, 11, "name", name);
        }
    }

    /**
     * @see ezbean.Accessible#ezAccess(int, java.lang.Object)
     */
    public Object ezAccess(int id, Object params) {
        switch (id) {
        default:
            throw new IllegalArgumentException();

        case 0:
            return getAge();

        case 1:
            setAge((Integer) params);
            return null;

        case 2:
            return getName();

        case 3:
            setName((String) params);
            return null;

        case 4:
            System.out.println("super");
            super.setName((String) params);
            return null;
        }
    }

    /**
     * @see ezbean.Accessible#ezCall(int, boolean, java.lang.Object[])
     */
    public Object ezCall(int id, Object... params) {
        switch (id) {
        default:
            throw new IllegalArgumentException();

        case 0:
            return super.getAge();

        case 1:
            super.setAge((Integer) params[0]);
            return null;

        case 2:
            return getName();

        case 3:
            super.setName((String) params[0]);
            return null;

        case 4:
            return super.isHungry();

        case 5:
            super.talk((String) params[0], (Integer) params[1]);
            return null;
        }
    }

    /**
     * @see ezbean.Accessible#ezContext()
     */
    public Listeners ezContext() {
        return null;
    }
}
