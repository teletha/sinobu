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
package ezbean.unit;

import ezbean.I;

/**
 * @version 2010/02/06 12:29:38
 */
public class Sandbox extends EzRule {

    /** The original security manager. */
    private SecurityManager original;

    /** The security manager. */
    private SecurityManager manager;

    /**
     * <p>
     * Create sandbox environment of the specified {@link SecurityManager}.
     * </p>
     * 
     * @param security A class of security manger.
     */
    public Sandbox(Class<? extends SecurityManager> security) {
        this.manager = I.make(security);
    }

    /**
     * @see ezbean.unit.EzRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
        original = System.getSecurityManager();

        System.setSecurityManager(manager);
    }

    /**
     * @see ezbean.unit.EzRule#afterClass()
     */
    @Override
    protected void afterClass() {
        System.setSecurityManager(original);
    }

}
