/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.lang.reflect.Method;
import java.nio.file.Path;

import ezunit.ReusableRule;

/**
 * <p>
 * Tweek Ezbean environment temporary for test.
 * </p>
 * 
 * @version 2011/03/30 18:07:26
 */
public class EzbeanSetting extends ReusableRule {

    /** The temporary working directory. */
    public final Path working;

    /** The original working directory. */
    private final Path workingOriginal;

    /**
     * @param working
     */
    public EzbeanSetting(Path working) {
        this.working = working;
        this.workingOriginal = I.$working;
    }

    /**
     * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        I.$working = working;
    }

    /**
     * @see ezunit.ReusableRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        I.$working = workingOriginal;
    }
}
