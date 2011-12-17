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
 * <p>
 * Listens to the file system change notifications when a file, directory, or file in a directory,
 * changes.
 * </p>
 * 
 * @see I#observe(Path, PathListener, String...)
 * @version 2011/04/09 7:10:06
 */
public interface PathListener {

    /**
     * <p>
     * Occurs when a file or directory in the specified {@link Path} is created.
     * </p>
     * <p>
     * Some common occurrences, such as copying or moving a file or directory, do not correspond
     * directly to an event, but these occurrences do cause events to be raised. When you copy a
     * file or directory, the system raises a Created event in the directory to which the file was
     * copied, if that directory is being watched.
     * </p>
     * 
     * @param path A event source path.
     */
    void create(Path path);

    /**
     * <p>
     * Occurs when a file or directory in the specified {@link Path} is deleted.
     * </p>
     * 
     * @param path A event source path.
     */
    void delete(Path path);

    /**
     * <p>
     * Occurs when a file or directory in the specified {@link Path} is modified.
     * </p>
     * <p>
     * The Modify event is raised when changes are made to the size, system attributes, last write
     * time, last access time, or security permissions of a file or directory being monitored.
     * </p>
     * 
     * @param path A event source path.
     */
    void modify(Path path);
}