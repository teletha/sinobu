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
package ezbean.io;

import java.io.File;
import java.io.IOException;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

/**
 * <p>
 * Archiver can decompress the archive of the specific format. All archivers are tied to a single
 * format, and MIME Type decides it. Ezbean provides only an archiver of application/zip.
 * </p>
 * <h1>How to associate MIME Type with {@link Archiver}</h1>
 * <p>
 * Ezbean uses a simple class name of the implementation as a sub-part of MIME Type. At first, we
 * translate upper characters into lower characters. Then, we replace all underscores with hyphen.
 * The actual code is like a following.
 * </p>
 * 
 * <pre>
 * "application/" + archiverClass.getSimpleName().toLowerCase().replace('_', '-');
 * </pre>
 * <p>
 * For example, <code>Zip</code> archiver class will be translated into "application/zip" and
 * <code>X_Gzip</code> archiver class will be translated into "application/x-gzip".
 * </p>
 * <h1>How to associate MiME Type with File Extensions</h1>
 * <p>
 * Ezbean uses {@link FileTypeMap} which is retrieved by the method
 * {@link FileTypeMap#getDefaultFileTypeMap()}. If you don't provide any {@link FileTypeMap}
 * implementation, {@link MimetypesFileTypeMap} will be used by system.
 * </p>
 * 
 * @see FileSystem
 * @see FileTypeMap
 * @see MimetypesFileTypeMap
 * @version 2008/12/10 18:23:05
 */
public interface Archiver {

    /**
     * <p>
     * Unpack the sepcified archive file into the destination file.
     * </p>
     * 
     * @param archive A sepcified archive file.
     * @param destination A destination file that the sepcified archive will be unpacked to.
     * @throws IOException If an I/O error occurs during unpacking.
     */
    void unpack(File archive, File destination) throws IOException;
}
