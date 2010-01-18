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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ezbean.Accessible;
import ezbean.I;
import ezbean.Listeners;

/**
 * Implement {@link Accessible} interface to delegate {@link ezbean.model.Model} load process to
 * {@link java.io.File}.
 * 
 * @version 2010/01/19 1:47:55
 */
@SuppressWarnings("serial")
class File extends java.io.File implements Accessible {

    /** The digest algorithm instance. */
    private static final MessageDigest digest;

    // initialization
    static {
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /** The affiliated archive for this file. */
    private final File archive;

    /** The archiver for the affiliated archive. */
    private final Class<? extends Archiver> archiver;

    /** The junction file for the archive. */
    private java.io.File junction;

    /**
     * Create File instance.
     * 
     * @param path A path to this file. The <code>null</code> is not accepted.
     * @param archive An affiliated archive of this file. The <code>null</code> is accepted.
     */
    File(String path, File archive) {
        super(path);

        this.archive = archive;
        this.archiver = FileSystem.findArchiver(getName());
    }

    /**
     * Retrieve junction point for this archive file.
     * 
     * @return A junction directory.
     */
    java.io.File getJunction() {
        // check cache
        if (junction == null) {
            // calculate digest
            synchronized (digest) {
                // Environment (not only OS but also Way of Execution) influences character equality
                // of file path (e.g. "c:/test.txt" in one environment, "C:/Test.txt" in other
                // environment). So we must normalize it.
                digest.update(getAbsolutePath().toLowerCase().getBytes());

                // dump byte array into hexadecimal digit sequence.
                StringBuilder builder = new StringBuilder();

                for (byte b : digest.digest()) {
                    builder.append(Integer.toHexString(b & 0xff));
                }

                // store junction directory
                junction = new java.io.File(FileSystem.temporaries, builder.toString());
            }
        }

        // API definition
        return junction;
    }

    /**
     * @see java.io.File#createNewFile()
     */
    @Override
    public boolean createNewFile() throws IOException {
        return (archive == null) ? super.createNewFile() : false;
    }

    /**
     * @see java.io.File#delete()
     */
    @Override
    public boolean delete() {
        if (archive == null) {
            // delete a junction file if this file is archive and it has created a junction file
            // already
            FileSystem.delete(junction);

            // delete this file
            return super.delete();
        }
        return false;
    }

    /**
     * @see java.io.File#deleteOnExit()
     */
    @Override
    public void deleteOnExit() {
        if (archive == null) {
            super.deleteOnExit();
        }
    }

    /**
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return (archiver == null) ? super.isDirectory() : exists();
    }

    /**
     * @see java.io.File#getAbsoluteFile()
     */
    @Override
    public java.io.File getAbsoluteFile() {
        return I.locate(getAbsolutePath());
    }

    /**
     * @see java.io.File#getCanonicalFile()
     */
    @Override
    public java.io.File getCanonicalFile() throws IOException {
        return I.locate(getCanonicalPath());
    }

    /**
     * @see java.io.File#getParent()
     */
    @Override
    public String getParent() {
        String path = super.getParent();
        return (archive == null || !archive.getJunction().getPath().equals(path)) ? path : archive.getPath();
    }

    /**
     * @see java.io.File#getParentFile()
     */
    @Override
    public java.io.File getParentFile() {
        String path = super.getParent();

        if (path == null) {
            return null;
        }
        return (archive != null && archive.getJunction().getPath().equals(path)) ? archive : new File(path, archive);
    }

    /**
     * @see java.io.File#list()
     */
    @Override
    public synchronized String[] list() {
        if (archiver == null) {
            return super.list();
        } else {
            // check diff
            long modified = lastModified();

            if (getJunction().lastModified() != modified) {
                FileSystem.delete(junction);

                try {
                    I.make(archiver).unpack(this, getJunction());

                    // you must set last modified date at the last
                    getJunction().setLastModified(modified);
                } catch (IOException e) {
                    return null; // API definition
                }
            }
            return getJunction().list();
        }
    }

    /**
     * @see java.io.File#listFiles()
     */
    @Override
    public java.io.File[] listFiles() {
        String[] names = list();

        if (names == null) {
            return null;
        }

        int size = names.length;
        java.io.File[] files = new java.io.File[size];

        for (int i = 0; i < size; i++) {
            if (archiver == null) {
                files[i] = new File(getPath() + separator + names[i], archive);
            } else {
                files[i] = new File(getJunction() + separator + names[i], this);
            }
        }
        return files;
    }

    /**
     * @see java.io.File#mkdir()
     */
    @Override
    public boolean mkdir() {
        return (archive == null) ? super.mkdir() : false;
    }

    /**
     * @see java.io.File#mkdirs()
     */
    @Override
    public boolean mkdirs() {
        return (archive == null) ? super.mkdirs() : false;
    }

    /**
     * @see java.io.File#renameTo(java.io.File)
     */
    @Override
    public boolean renameTo(java.io.File dest) {
        return (archive == null) ? super.renameTo(dest) : false;
    }

    /**
     * @see java.io.File#setLastModified(long)
     */
    @Override
    public boolean setLastModified(long time) {
        return (archive == null) ? super.setLastModified(time) : false;
    }

    /**
     * @see java.io.File#toURI()
     */
    @Override
    public URI toURI() {
        if (archiver == null) {
            return super.toURI();
        } else {
            String uri = super.toURI().toString();

            try {
                return new URI(uri.substring(0, uri.length() - 1));
            } catch (URISyntaxException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            }
        }
    }

    /**
     * @see java.io.File#toURL()
     */
    @Override
    public URL toURL() throws MalformedURLException {
        if (archiver == null) {
            return super.toURI().toURL();
        } else {
            String url = super.toURI().toURL().toString();
            return new URL(url.substring(0, url.length() - 1));
        }
    }

    /**
     * @see java.io.File#toString()
     */
    @Override
    public String toString() {
        String filePath = super.toString();

        if (archive != null) {
            filePath = archive + filePath.substring(archive.getJunction().getPath().length());
        }
        return filePath.replace(separatorChar, FileSystem.SEPARATOR);
    }

    /**
     * @see ezbean.Accessible#ezAccess(int, java.lang.Object)
     */
    public Object ezAccess(int id, Object params) {
        return null; // do nothing
    }

    /**
     * @see ezbean.Accessible#ezCall(int, java.lang.Object[])
     */
    public Object ezCall(int id, Object... params) {
        return null; // do nothing
    }

    /**
     * @see ezbean.Accessible#ezContext()
     */
    public Listeners ezContext() {
        return null; // do nothing
    }

}
