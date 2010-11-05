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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import ezbean.io.FileSystem;
import ezbean.module.external.SingletonClass;
import ezunit.ReusableRule;

/**
 * @version 2010/01/08 1:56:27
 */
public class ModuleTestRule extends ReusableRule {

    /** The full path for external package. */
    private static final String externalPath = SingletonClass.class.getPackage().getName().replace('.', '/');

    /** The directory for pseudo external classes. */
    private static final File sources = I.locate("target/test-classes/" + externalPath);

    /** The directory for pseudo external classes. */
    private static final File classes = I.locate("src/test/modules");

    /** The directory for pseudo external classes. */
    private static final File checksum = I.locate(classes, "checksum");

    /** The module registry for reuse. */
    private static final Modules registry = I.make(Modules.class);

    /** The pseudo external module. */
    private static final File DIR = I.locate(classes, "module");

    /** The pseudo external module. */
    private static final File JAR = I.locate(classes, "module.jar");

    /** The pseudo external module. */
    private static final File ZIP = I.locate(classes, "module.zip");

    /** The pseudo external module. */
    private static final File NEST = I.locate(classes, "inline.zip");

    // create external modules
    static {
        I.xml(checksum, I.make(UpdateChecker.class)).update();
    }

    /** The pseudo external module. */
    public final File dir = DIR;

    /** The pseudo external module. */
    public final File jar = JAR;

    /** The pseudo external module. */
    public final File zip = ZIP;

    /** The pseudo external module. */
    public final File nest = NEST;

    /** field access helper */
    public final List<Module> modules = registry.modules;

    /** The pre-defined module store. */
    private List<Module> stored = new ArrayList();

    /**
     * @param clazz
     * @see ezbean.Modules#load(java.lang.Class)
     */
    public void load(Class clazz) {
        registry.load(clazz);
    }

    /**
     * @param moduleFile
     * @see ezbean.Modules#load(java.io.File)
     */
    public void load(File moduleFile) {
        registry.load(moduleFile);
    }

    /**
     * @param clazz
     * @see ezbean.Modules#unload(java.lang.Class)
     */
    public void unload(Class clazz) {
        registry.unload(clazz);
    }

    /**
     * @param moduleFile
     * @see ezbean.Modules#unload(java.io.File)
     */
    public void unload(File moduleFile) {
        registry.unload(moduleFile);
    }

    /**
     * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        // store current modules
        stored.addAll(registry.modules);

        // clear current modules from registry temporary
        registry.modules.clear();
    }

    /**
     * @see ezunit.ReusableRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        // unload all modules which are added by test case
        registry.modules.clear();

        // restore stored modules
        registry.modules.addAll(stored);

        // clear stored modules
        stored.clear();
    }

    /**
     * <p>
     * Compute relative path.
     * </p>
     * 
     * @param base
     * @param file
     * @return
     */
    private static String computePath(File base, File file) {
        return base.toURI().relativize(file.toURI()).toString();
    }

    /**
     * <p>
     * Convert class file as pseudo-external-class.
     * </p>
     * 
     * @param root
     * @param directory
     */
    private static void convert(File root, File directory) {
        try {
            for (File file : directory.listFiles()) {
                if (!file.isFile()) {
                    convert(root, file);
                } else {
                    File output = I.locate(root, "external/" + computePath(sources, file));

                    // create output directory
                    output.getParentFile().mkdirs();

                    if (!file.getName().endsWith("class")) {
                        FileSystem.copy(file, output);
                    } else {
                        // setup
                        ClassWriter writer = new ClassWriter(0);

                        // convert class file
                        new ClassReader(new FileInputStream(file)).accept(new ClassConverter(writer), 0);

                        // write class file
                        FileOutputStream stream = new FileOutputStream(output);
                        stream.write(writer.toByteArray());
                        stream.close();
                    }
                }
            }
        } catch (IOException e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }

    /**
     * Helper method to conver package name.
     * 
     * @param name
     * @return
     */
    private static String convert(String name) {
        return name.replace(externalPath, "external");
    }

    /**
     * Helper method to conver package name.
     * 
     * @param names
     * @return
     */
    private static String[] convert(String[] names) {
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                names[i] = convert(names[i]);
            }
        }

        return names;
    }

    /**
     * @version 2009/12/22 20:52:10
     */
    private static final class ClassConverter extends ClassAdapter {

        /**
         * @param visiter
         */
        public ClassConverter(ClassVisitor visiter) {
            super(visiter);
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, convert(name), signature, convert(superName), convert(interfaces));
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodConverter(super.visitMethod(access, convert(name), convert(desc), signature, convert(exceptions)));
        }

    }

    /**
     * @version 2009/12/22 22:08:58
     */
    private static final class MethodConverter extends MethodAdapter {

        /**
         * @param paramMethodVisitor
         */
        public MethodConverter(MethodVisitor paramMethodVisitor) {
            super(paramMethodVisitor);
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, convert(owner), convert(name), convert(desc));
        }
    }

    /**
     * @version 2010/01/07 23:05:16
     */
    protected class UpdateChecker {

        private long sum = -1;

        /**
         * Get the checksum property of this {@link ModuleTestCase.Setting}.
         * 
         * @return The checksum property.
         */
        public long getChecksum() {
            return sum;
        }

        /**
         * Set the checksum property of this {@link ModuleTestCase.Setting}.
         * 
         * @param checksum The checksum value to set.
         */
        public void setChecksum(long sum) {
            this.sum = sum;
        }

        /**
         * Update external modules if needed.
         */
        public void update() {
            // calculate
            long current = calculate(dir, 0);

            if (current != sum) {
                // create external module
                convert(DIR, sources);

                create(JAR);
                create(ZIP);

                // create external zip
                ZipOutputStream stream = null;

                try {
                    stream = new ZipOutputStream(new FileOutputStream(nest));

                    addEntry(classes, JAR, stream);
                    addEntry(classes, ZIP, stream);
                } catch (IOException e) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            // If this exception will be thrown, it is bug of this program. So we
                            // must
                            // rethrow the wrapped error in here.
                            throw new Error(e);
                        }
                    }
                }

                // recalculate
                sum = calculate(dir, 0);

                I.xml(this, checksum);
            }
        }

        /**
         * Calculate checksum.
         * 
         * @param directory
         * @param sum
         * @return
         */
        private long calculate(File directory, long sum) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (!file.isFile()) {
                        sum += calculate(file, sum);
                    } else {
                        sum += file.lastModified();
                    }
                }
            }

            return sum;
        }

        private void create(File location) {
            ZipOutputStream stream = null;

            try {
                if (location == JAR) {
                    stream = new JarOutputStream(new FileOutputStream(location));
                } else {
                    stream = new ZipOutputStream(new FileOutputStream(location));
                }

                addEntry(DIR, DIR, stream);
            } catch (IOException e) {
                // If this exception will be thrown, it is bug of this program. So we must
                // rethrow the wrapped error in here.
                throw new Error(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // If this exception will be thrown, it is bug of this program. So we
                        // must rethrow the wrapped error in here.
                        throw new Error(e);
                    }
                }
            }
        }

        /**
         * Add entry.
         * 
         * @param file
         * @param stream
         * @throws IOException
         */
        private void addEntry(File base, File file, ZipOutputStream stream) throws IOException {
            if (file.isFile()) {
                ZipEntry entry = new ZipEntry(computePath(base, file));

                stream.putNextEntry(entry);

                int length = 0;
                byte[] buffer = new byte[1024];
                InputStream input = new BufferedInputStream(new FileInputStream(file));

                while ((length = input.read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
                input.close();

                stream.closeEntry();
            } else {
                for (File child : file.listFiles()) {
                    addEntry(base, child, stream);
                }
            }
        }
    }
}
