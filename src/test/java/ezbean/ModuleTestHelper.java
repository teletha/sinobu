/*
 * Copyright (C) 2010 Nameless Production Committee.
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

import ezbean.model.Model;

/**
 * <p>
 * Expose non-accessible methods and fields to test easily.
 * </p>
 * 
 * @version 2010/11/05 18:25:00
 */
public class ModuleTestHelper {

    public static Model loadModel(String fqcn) {
        return Model.load(Modules.load(fqcn));
    }
}
