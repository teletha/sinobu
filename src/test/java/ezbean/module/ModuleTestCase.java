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
package ezbean.module;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import ezbean.I;
import ezbean.io.FileSystem;
import ezbean.module.external.SingletonClass;

/**
 * @version 2009/12/23 13:20:59
 */
public abstract class ModuleTestCase {

    /** The full path for external package. */
    private static final String externalPath = SingletonClass.class.getPackage().getName().replace('.', '/');

    /** The directory for pseudo external classes. */
    private static final File sources = I.locate("target/test-classes/" + externalPath);

    /** The directory for pseudo external classes. */
    private static final File classes = I.locate("src/test/modules");

    /** The pseudo external module. */
    private static final File external;

    /** The pseudo external module. */
    private static final File jar;

    /** The pseudo external module. */
    private static final File zip;

    /** The pseudo external module. */
    private static final File inline;

    static {
        external = I.locate(classes, "module");
        jar = I.locate(classes, "module.jar");
        zip = I.locate(classes, "module.zip");
        inline = I.locate(classes, "inline.zip");

        convert(external, sources);
        external.setLastModified(System.currentTimeMillis());
    }

    /** The module registry for reuse. */
    protected final ModuleRegistry registry = I.make(ModuleRegistry.class);

    /** The pre-defined module store. */
    private List<Module> modules = new ArrayList();

    @Before
    public void initialize() {
        // store current modules
        modules.addAll(registry.modules);

        // clear current modules from registry temporary
        registry.modules.clear();
    }

    @After
    public void restore() {
        // unload all modules which are added by test case
        for (Module module : registry.modules) {
            registry.unload(module.moduleFile);
        }

        // restore stored modules
        registry.modules.addAll(modules);
    }

    /**
     * <p>
     * Resolve the pseudo external module.
     * </p>
     * 
     * @return A pseudo external module directory.
     */
    protected final File resolveExternal() {
        return external;
    }

    /**
     * <p>
     * Resolve the pseudo external module.
     * </p>
     * 
     * @return A pseudo external module directory.
     */
    protected final File resolveExternalJar() {
        if (jar.lastModified() != external.lastModified()) {
            JarOutputStream stream = null;

            try {
                stream = new JarOutputStream(new FileOutputStream(jar));

                dig(resolveExternal(), stream);
            } catch (IOException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // If this exception will be thrown, it is bug of this program. So we must
                        // rethrow the wrapped error in here.
                        throw new Error(e);
                    }
                }
            }
            jar.setLastModified(external.lastModified());
        }

        // API definition
        return jar;
    }

    /**
     * <p>
     * Resolve the pseudo external module.
     * </p>
     * 
     * @return A pseudo external module directory.
     */
    protected final File resolveExternalZip() {
        if (zip.lastModified() != external.lastModified()) {
            ZipOutputStream stream = null;

            try {
                stream = new ZipOutputStream(new FileOutputStream(zip));

                dig(resolveExternal(), stream);
            } catch (IOException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // If this exception will be thrown, it is bug of this program. So we must
                        // rethrow the wrapped error in here.
                        throw new Error(e);
                    }
                }
            }
            zip.setLastModified(external.lastModified());
        }

        // API definition
        return zip;
    }

    /**
     * <p>
     * Resolve the pseudo external module.
     * </p>
     * 
     * @return A pseudo external module directory.
     */
    protected final File resolveExternalInline() {
        if (inline.lastModified() != external.lastModified()) {
            ZipOutputStream stream = null;

            try {
                stream = new ZipOutputStream(new FileOutputStream(inline));

                for (int i = 0; i < 2; i++) {
                    ZipEntry entry = new ZipEntry("inline" + i + ".zip");

                    stream.putNextEntry(entry);

                    int length = 0;
                    byte[] buffer = new byte[1024];
                    InputStream input = new BufferedInputStream(new FileInputStream(resolveExternalZip()));

                    while ((length = input.read(buffer)) != -1) {
                        stream.write(buffer, 0, length);
                    }
                    input.close();

                    stream.closeEntry();
                }
            } catch (IOException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // If this exception will be thrown, it is bug of this program. So we must
                        // rethrow the wrapped error in here.
                        throw new Error(e);
                    }
                }
            }
            inline.setLastModified(external.lastModified());
        }

        // API definition
        return inline;
    }

    /**
     * Dig file system.
     * 
     * @param directory
     * @param stream
     * @throws IOException
     */
    private void dig(File directory, ZipOutputStream stream) throws IOException {
        for (File file : directory.listFiles()) {
            if (!file.isFile()) {
                dig(file, stream);
            } else {
                ZipEntry entry = new ZipEntry(computePath(resolveExternal(), file));

                stream.putNextEntry(entry);

                int length = 0;
                byte[] buffer = new byte[1024];
                InputStream input = new BufferedInputStream(new FileInputStream(file));

                while ((length = input.read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
                input.close();

                stream.closeEntry();
            }
        }
    }

    /**
     * Dig file system.
     * 
     * @param directory
     * @param stream
     * @throws IOException
     */
    private void dig(File directory, JarOutputStream stream) throws IOException {
        for (File file : directory.listFiles()) {
            if (!file.isFile()) {
                dig(file, stream);
            } else {
                JarEntry entry = new JarEntry(computePath(resolveExternal(), file));

                stream.putNextEntry(entry);

                int length = 0;
                byte[] buffer = new byte[1024];
                InputStream input = new BufferedInputStream(new FileInputStream(file));

                while ((length = input.read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
                input.close();

                stream.closeEntry();
            }
        }
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

                    if (!FileSystem.getExtension(file).equals("class")) {
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
}
