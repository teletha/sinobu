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

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import javax.activation.FileTypeMap;

import ezbean.ClassLoadListener;
import ezbean.I;
import ezbean.Listeners;
import ezbean.Manageable;
import ezbean.Singleton;

/**
 * <p>
 * Utility class to provide methods to manipulate {@link File}.
 * </p>
 * 
 * @version 2010/01/21 0:23:58
 */
@Manageable(lifestyle = Singleton.class)
public final class FileSystem implements ClassLoadListener<Archiver> {

    /** The temporary directory for the current processing JVM. */
    private static File temporary;

    /** The repository for archiver. */
    private static final Listeners<String, Class<Archiver>> archivers = new Listeners();

    /** The root temporary directory for Ezbean. */
    static File temporaries;

    /**
     * Helper method to find the suitable {@link Archiver} for the given file name. This method is
     * specialized in file name. If the suitable one is not found, <code>null</code> will be
     * returned.
     * 
     * @param name A file name. The <code>null</code> is not accepted.
     * @return An archiver class or <code>null</code>.
     */
    static Class<Archiver> findArchiver(String name) {
        return archivers.find(FileTypeMap.getDefaultFileTypeMap().getContentType(name));
    }

    // initialize
    static {
        try {
            temporaries = new File(System.getProperty("java.io.tmpdir"), "Ezbean").getCanonicalFile();
            temporaries.mkdirs();

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (File file : temporaries.listFiles()) {
                if (file.getName().startsWith("temporary")) {
                    // create a file to represent the lock and test
                    RandomAccessFile lock = new RandomAccessFile(new File(file, "lock"), "rw");

                    // delete the contents of the temporary directory since it can retrieve a
                    // exclusive lock
                    if (lock.getChannel().tryLock() != null) {
                        // release lock at first
                        lock.close();

                        // delete actually
                        delete(file);
                    }
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = File.createTempFile("temporary", null, temporaries);

            // Delete the file if one was automatically created by the JVM. We are going to use the
            // name of the file as a directory name, so we do not want the file laying around.
            temporary.delete();

            // Create a temporary directory which will be used for all future temporary file
            // requests.
            temporary.mkdirs();

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(new File(temporary, "lock"), "rw").getChannel().tryLock();
        } catch (SecurityException e) {
            temporary = null;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Avoid construction.
     */
    private FileSystem() {
        // Ezbean user always use ezbean.jar. If we don't register ZIP archiver at here, user
        // can't decompress ezbean.jar to collect and retrieve information from its jar.
        load(Zip.class);
    }

    /**
     * @see ezbean.ClassLoadListener#load(java.lang.Class)
     */
    public void load(Class clazz) {
        archivers.push("application/" + clazz.getSimpleName().toLowerCase().replace('_', '-'), clazz);
    }

    /**
     * @see ezbean.ClassLoadListener#unload(java.lang.Class)
     */
    public void unload(Class clazz) {
        archivers.pull("application/" + clazz.getSimpleName().toLowerCase().replace('_', '-'), clazz);
    }

    /**
     * Locate the specified file path and return the plain {@link File} object.
     * 
     * @param filePath
     * @return
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public File locate(String filePath) {
        // remove file protocol prefix
        if (filePath.startsWith("file:")) {
            filePath = filePath.substring(5);
        }

        // normalize separator
        if (File.separatorChar != '/') {
            filePath = filePath.replace(File.separatorChar, '/');
        }

        String[] paths = filePath.split("/");
        StringBuilder path = new StringBuilder();
        ezbean.io.File archive = null;

        for (int i = 0; i < paths.length - 1; i++) {
            // add the current path
            path.append(paths[i]);

            // find archiver
            if (findArchiver(paths[i]) != null) {
                // create archive
                archive = new ezbean.io.File(path.toString(), archive);
                archive.list(); // force to unpack the archive

                // rebuild actual path
                path = new StringBuilder(archive.getJunction().getPath());
            }

            // close the current path
            path.append(File.separator);
        }

        // build File from the current path
        return new ezbean.io.File(path.append(paths[paths.length - 1]).toString(), archive);
    }

    /**
     * Generic method to copy a input {@link File} to an output {@link File}.
     * 
     * @param input A input {@link File} object which can be file or directory.
     * @param output An outout {@link File} object which can be file or directory.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws IOException If an I/O error occurs.
     * @throws FileNotFoundException If the specified input file is not found. If the input file is
     *             directory and the output file is <em>not</em> directory.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(File input, File output) {
        copy(input, output, null);
    }

    /**
     * Generic method to copy a input {@link File} to an output {@link File}.
     * 
     * @param input A input {@link File} object which can be file or directory.
     * @param output An outout {@link File} object which can be file or directory.
     * @param filter A file filter to copy. If <code>null</code> is specified, all file will be
     *            accepted. This filter is used only when the input is directory.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws IOException If an I/O error occurs.
     * @throws FileNotFoundException If the specified input file is not found. If the input file is
     *             directory and the output file is <em>not</em> directory.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(File input, File output, FileFilter filter) {
        // uncast
        input = new File(input.getPath());
        output = new File(output.getPath());

        if (input.isDirectory()) {
            copyD2D(input, output, filter);
        } else {
            // If the input is file, output can accept file or directory.
            if (output.isDirectory()) {
                copyF2F(input, new File(output, input.getName()));
            } else {
                copyF2F(input, output);
            }
        }
    }

    /**
     * Copies a directory to within another directory preserving the file dates. This method copies
     * the source directory and all its contents to a directory of the same name in the specified
     * destination directory.
     * 
     * @param input A source directory.
     * @param output A destination directory.
     * @throws IOException If an I/O error occurs.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    private static void copyD2D(File input, File output, FileFilter filter) {
        // copy
        File dirctory = new File(output, input.getName());
        dirctory.mkdir();

        for (File child : input.listFiles(filter)) {
            if (child.isDirectory()) {
                copyD2D(child, dirctory, filter);
            } else {
                copyF2F(child, new File(dirctory, child.getName()));
            }
        }

        // We must copy last modified date at the end.
        dirctory.setLastModified(input.lastModified());
    }

    /**
     * Helper method to copy a file to a new location preserving the file date. This method copies
     * the contents of the specified source file to the specified destination file.
     * 
     * @param input A source file.
     * @param output A destination file.
     * @throws IOException If an I/O error occurs.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    private static void copyF2F(File input, File output) {
        // assure that the parent directories exist
        output.getParentFile().mkdirs();

        FileChannel in = null;
        FileChannel out = null;

        try {
            in = new FileInputStream(input).getChannel();
            out = new FileOutputStream(output).getChannel();

            // copy data actually
            in.transferTo(0, in.size(), out);

        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            close(in);
            close(out);
        }

        // We must copy last modified date at the end.
        output.setLastModified(input.lastModified());
    }

    /**
     * <p>
     * Note : This method closes both input and output stream carefully.
     * </p>
     * <p>
     * Copy bytes from a {@link InputStream} to an {@link OutputStream}. This method buffers the
     * input internally, so there is no need to use a buffered stream.
     * </p>
     * 
     * @param input A {@link InputStream} to read from.
     * @param output An {@link OutputStream} to write to.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the input or output is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(InputStream input, OutputStream output) {
        int size = 0;
        byte[] buffer = new byte[8192];

        try {
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            close(input);
            close(output);
        }
    }

    /**
     * <p>
     * Close the specified object quietly if it is {@link Closeable}. Equivalent to
     * {@link Closeable#close()}, except any exceptions will be ignored. This is typically used in
     * finally block like the following.
     * </p>
     * 
     * <pre>
     * Closeable input = null;
     * 
     * try {
     *     // some IO action
     * } catch (IOException e) {
     *     throw e;
     * } finally {
     *     FileSystem.close(input);
     * }
     * </pre>
     * 
     * @param closeable Any object you want to close. <code>null</code> is acceptable.
     */
    public static void close(Object closeable) {
        if (closeable instanceof Closeable) {
            try {
                ((Closeable) closeable).close();
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    // /**
    // * <p>
    // * Note : This method closes both input and output stream carefully.
    // * </p>
    // * <p>
    // * Copy bytes from a {@link InputStream} to an {@link OutputStream}. This method buffers the
    // * input internally, so there is no need to use a buffered stream.
    // * </p>
    // *
    // * @param input A {@link InputStream} to read from.
    // * @param output An {@link OutputStream} to write to.
    // * @throws IOException If an I/O error occurs.
    // * @throws NullPointerException If the input or output is null.
    // */
    // public static void copy(ReadableByteChannel input, WritableByteChannel output) throws
    // IOException {
    // ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
    //
    // try {
    // while (input.read(buffer) != -1) {
    // buffer.flip();
    //
    // output.write(buffer);
    //
    // buffer.clear();
    // }
    // } finally {
    // input.close();
    // output.close();
    // }
    // }

    /**
     * <p>
     * Creates a new abstract file somewhere beneath the system's temporary directory (as defined by
     * the <code>java.io.tmpdir</code> system property).
     * </p>
     * <p>
     * </p>
     * 
     * @return A newly created temporary file which is not exist yet.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static File createTemporary() {
        try {
            File file = File.createTempFile("temporary", null, temporary);

            // Delete the file if one was automatically created by the JVM. We may use the name of
            // the file as a directory name, so we do not want the file laying around.
            file.delete();

            // API definition
            return file;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Clear the content of the target without deleting it. If the target is file, it's content data
     * is cleared. If the target is directory, all files and directories are recursively deleted.
     * The directory is no need to be empty in order to be deleted.
     * </p>
     * 
     * @param target A target file or directory.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkDelete(String)} method denies delete access to the
     *             target file and its contents.
     */
    public static void clear(File target) {
        // check null
        if (target != null && target.exists()) {
            if (target.isDirectory()) {
                // directory pattern
                for (File child : target.listFiles()) {
                    delete(child); // recursively
                }
            } else {
                // file pattern
                long modified = target.lastModified();

                try {
                    // delete once
                    delete(target);

                    // rebuild
                    target.createNewFile();

                    // maintain modified time
                    target.setLastModified(modified);
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            }
        }
    }

    /**
     * <p>
     * Delete the target file or directory. If the target is file, it's file will be deleted. If the
     * target is directory, all files, all directories and itself will be recursively deleted. The
     * directory is no need to be empty in order to be deleted.
     * </p>
     * 
     * @param target A target file or directory.
     * @return <code>true</code> if and only if the file or directory is successfully deleted,
     *         <code>false</code> otherwise.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkDelete(String)} method denies delete access to the
     *             target file and its contents.
     */
    public static boolean delete(File target) {
        // check null
        if (target == null || !target.exists()) {
            return false;
        }

        boolean result = true;

        // use not isDirectory but !isFile
        if (!target.isFile()) {
            for (File child : target.listFiles()) {
                // recursively
                if (!delete(child)) {
                    result = false;
                }
            }
        }

        if (result && !target.delete()) {
            result = false;
        }
        return result;
    }

    // public static String[] construe(File file) {
    // String[] values = {"", ""};
    //
    // if (file == null) {
    // return values;
    // }
    //
    // String name = file.getName();
    // int index = name.lastIndexOf('.');
    //
    // if (index == -1) {
    // values[0] = name;
    // } else {
    // values[0] = name.substring(0, index);
    // values[1] = name.substring(index + 1);
    // }
    // return values;
    // }

    /**
     * Helper method to retrieve a file base name of the file.
     * 
     * @param file A target file.
     * @return A filen base name.
     */
    public static String getName(File file) {
        // check null
        if (file == null) {
            return "";
        }

        String name = file.getName();
        int index = name.lastIndexOf('.');

        return (index == -1) ? name : name.substring(0, index);
    }

    /**
     * Helper method to retrieve a extension name of the file.
     * 
     * @param file A target file.
     * @return A extension name.
     */
    public static String getExtension(File file) {
        // check null
        if (file == null) {
            return "";
        }

        String name = file.getName();
        int index = name.lastIndexOf('.');

        return (index == -1) ? "" : name.substring(index + 1);
    }
}
