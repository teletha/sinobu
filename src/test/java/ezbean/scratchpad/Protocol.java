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

import ezbean.io.FileSystem;

/**
 * DOCUMENT.
 * 
 * @version 2008/12/08 19:57:48
 */
public interface Protocol<T> {

    /**
     * <p>
     * Resolve the given file path to the correct path. The protocol prefix is already cut off from
     * the file path. (i.e. "file:/c:/path/to/file" -> "c:/path/to/file", "protocol://path" ->
     * "path").
     * </p>
     * <p>
     * The file path uses not platform dependent separator but uniform path separator '/'. You can
     * refer it from the public static field {@link FileSystem#SEPARATOR}.
     * </p>
     * 
     * @see FileSystem
     * @param filePath A file path.
     * @return A correct path.
     */
    String link(String filePath);
}
