/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.zip.CRC32;

import kiss.I;

/**
 * <p>
 * Support for simultaneous operations of Path.
 * </p>
 * 
 * @version 2015/06/26 8:33:12
 */
public class SynchronizedTreeValidator {

    /** The one. */
    private Path one;

    /** The other. */
    private Path other;

    /**
     * <p>
     * Create instance of {@link SynchronizedTreeValidator}.
     * </p>
     * 
     * @param one
     * @param other
     * @return
     */
    public static SynchronizedTreeValidator of(Path one, Path other) {
        return new SynchronizedTreeValidator(one, other);
    }

    /**
     * Hide constructor.
     */
    private SynchronizedTreeValidator(Path one, Path other) {
        this.one = one;
        this.other = other;
    }

    /**
     * <p>
     * Resolve path by name.
     * </p>
     * 
     * @param path A file path.
     * @return A self instance to chain API.
     */
    public SynchronizedTreeValidator sibling(String path) {
        one = one.resolveSibling(path);
        other = other.resolveSibling(path);

        // API definition
        return this;
    }

    /**
     * <p>
     * Resolve path by name.
     * </p>
     * 
     * @param path A file path.
     * @return A self instance to chain API.
     */
    public SynchronizedTreeValidator child(String path) {
        one = one.resolve(path);
        other = other.resolve(path);

        // API definition
        return this;
    }

    public SynchronizedTreeValidator exists(boolean one, boolean other) {
        if (one) {
            if (Files.notExists(this.one)) {
                throw new AssertionError("'" + this.one + "' must exist.");
            }
        } else {
            if (Files.exists(this.one)) {
                throw new AssertionError("'" + this.one + "' must not exist.");
            }
        }

        if (other) {
            if (Files.notExists(this.other)) {
                throw new AssertionError("'" + this.other + "' must exist.");
            }
        } else {
            if (Files.exists(this.other)) {
                throw new AssertionError("'" + this.other + "' must not exist.");
            }
        }

        // API definition
        return this;
    }

    public SynchronizedTreeValidator areSameDirectory() {
        assertPath(one, other, true, false, true);

        // API definition
        return this;
    }

    public SynchronizedTreeValidator areNotSameDirectory() {
        assertPath(one, other, true, false, false);

        // API definition
        return this;
    }

    public SynchronizedTreeValidator areSameFile() {
        assertPath(one, other, true, true, true);

        // API definition
        return this;
    }

    public SynchronizedTreeValidator areNotSameFile() {
        assertPath(one, other, true, true, false);

        // API definition
        return this;
    }

    /**
     * <p>
     * Helper method to check various file attributes.
     * </p>
     * 
     * @param one
     * @param other
     */
    private void assertPath(Path one, Path other, boolean exist, boolean file, boolean same) {
        try {
            if (!exist) {
                if (!Files.notExists(one)) {
                    throw new AssertionError("'" + one + "' must not exist.");
                }

                if (!Files.notExists(other)) {
                    throw new AssertionError("'" + other + "' must not exist.");
                }
                return; // end
            } else {
                if (!Files.exists(one)) {
                    throw new AssertionError("'" + one + "' must exist.");
                }

                if (!Files.exists(other)) {
                    throw new AssertionError("'" + other + "' must exist.");
                }
            }

            BasicFileAttributes oneAttributes = Files.readAttributes(one, BasicFileAttributes.class);
            BasicFileAttributes otherAttributes = Files.readAttributes(other, BasicFileAttributes.class);

            if (file) {
                if (!oneAttributes.isRegularFile()) {
                    throw new AssertionError("'" + one + "' must be file.");
                }
            } else {
                if (!oneAttributes.isDirectory()) {
                    throw new AssertionError("'" + one + "' must be directory.");
                }
            }

            // FileTime
            // FileTime oneTime = oneAttributes.creationTime();
            // FileTime otherTime = otherAttributes.creationTime();
            //
            // if (!oneTime.equals(otherTime)) {
            // throw new AssertionError("CreationTime: '" + one + "' is " + oneTime + ", but '" +
            // other + "' is " +
            // otherTime + ".");
            // }
            //
            // oneTime = oneAttributes.lastAccessTime();
            // otherTime = otherAttributes.lastAccessTime();
            //
            // if (!oneTime.equals(otherTime)) {
            // throw new AssertionError("LastAccessTime: '" + one + "' is " + oneTime + ", but '" +
            // other + "' is " +
            // otherTime + ".");
            // }

            FileTime oneTime = oneAttributes.lastModifiedTime();
            FileTime otherTime = otherAttributes.lastModifiedTime();

            if (!oneTime.equals(otherTime)) {
                throw new AssertionError("LastModifiedTime: '" + one + "' is " + oneTime + ", but '" + other + "' is " + otherTime + ".");
            }

            // Directory
            if (oneAttributes.isDirectory()) {
                if (!otherAttributes.isDirectory()) {
                    throw new AssertionError("'" + one + "' is directory, but '" + other + "' is not directory.");
                }
            } else {
                if (otherAttributes.isDirectory()) {
                    throw new AssertionError("'" + one + "' is not directory, but '" + other + "' is directory.");
                }
            }

            // File
            if (oneAttributes.isRegularFile()) {
                if (!otherAttributes.isRegularFile()) {
                    throw new AssertionError("'" + one + "' is file, but '" + other + "' is not file.");
                }

                if (oneAttributes.size() != otherAttributes.size()) {
                    throw new AssertionError("FileSize: '" + one + "' is " + oneAttributes
                            .size() + ", but '" + other + "' is " + otherAttributes.size() + ".");
                }

                CRC32 oneHash = new CRC32();
                CRC32 otherHash = new CRC32();

                oneHash.update(Files.readAllBytes(one));
                otherHash.update(Files.readAllBytes(other));

                if (oneHash.getValue() != otherHash.getValue()) {
                    throw new AssertionError("'" + one + "' is different from '" + other + "'.");
                }
            } else {
                if (otherAttributes.isRegularFile()) {
                    throw new AssertionError("'" + one + "' is not file, but '" + other + "' is file.");
                }
            }

            // SymbolicLink
            if (oneAttributes.isSymbolicLink()) {
                if (!otherAttributes.isSymbolicLink()) {
                    throw new AssertionError("'" + one + "' is symbolic link, but '" + other + "' is not symbolic link.");
                }
            } else {
                if (otherAttributes.isSymbolicLink()) {
                    throw new AssertionError("'" + one + "' is not symbolic link, but '" + other + "' is symbolic link.");
                }
            }

            // Unknown
            if (oneAttributes.isOther()) {
                if (!otherAttributes.isOther()) {
                    throw new AssertionError("'" + one + "' is unknown, but '" + other + "' is not unknown.");
                }
            } else {
                if (otherAttributes.isOther()) {
                    throw new AssertionError("'" + one + "' is not unknown, but '" + other + "' is unknown.");
                }
            }
        } catch (IOException e) {
            throw I.quiet(e);
        } catch (AssertionError e) {
            if (same) {
                throw e;
            }
            return;
        }

        if (!same) {
            throw new AssertionError("'" + one + "' is equal to '" + other + "'.");
        }
    }
}
