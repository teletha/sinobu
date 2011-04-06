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

import java.nio.file.Path;

/**
 * @version 2011/04/06 17:36:41
 */
public interface FileListener {

    /**
     * <p>
     * Listen file system event.
     * </p>
     * 
     * @param path
     */
    void create(Path path);

    /**
     * <p>
     * Listen file system event.
     * </p>
     * 
     * @param path
     */
    void delete(Path path);

    /**
     * <p>
     * Listen file system event.
     * </p>
     * 
     * @param path
     */
    void modify(Path path);
}