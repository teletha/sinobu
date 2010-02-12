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
package ezunit;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ezbean.I;

/**
 * <p>
 * Super flexible security manager for test environment. You can safely change settings at runtime
 * and settings are restorable for each test.
 * </p>
 * 
 * @version 2010/02/09 13:13:43
 */
public class Sandbox extends EzRule {

    /** The internal value. */
    private static final int read = 0;

    /** The external value. */
    public static final int READ = 1 << read;

    /** The internal value. */
    private static final int write = 1;

    /** The external value. */
    public static final int WRITE = 1 << write;

    /** The internal value. */
    private static final int classloader = 2;

    /** The external value. */
    public static final int CLASSLOADER = 1 << classloader;

    /** The top level security manager for this sandbox. */
    protected final Security security;

    /** The platform original security manager. */
    protected final SecurityManager platform = System.getSecurityManager();

    /** The flag whether test is running or not. */
    private boolean whileTest = false;

    /**
     * <p>
     * Create sandbox environment of the default {@link SecurityManager}.
     * </p>
     */
    public Sandbox() {
        this(0);
    }

    /**
     * <p>
     * Create sandbox environment of the default {@link SecurityManager}.
     * </p>
     */
    public Sandbox(int permissions) {
        this(null, permissions);
    }

    /**
     * <p>
     * Create sandbox environment of the specified {@link SecurityManager}.
     * </p>
     * 
     * @param security A class of security manger.
     */
    public Sandbox(Class<? extends SecurityManager> manager) {
        this(manager, 0);
    }

    /**
     * <p>
     * Create sandbox environment of the specified {@link SecurityManager}.
     * </p>
     * 
     * @param manager
     * @param permissions
     */
    protected Sandbox(Class<? extends SecurityManager> manager, int permissions) {
        this.security = new Security(manager == null ? System.getSecurityManager() : I.make(manager), permissions);
    }

    /**
     * @see ezunit.EzRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
    }

    /**
     * @see ezunit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        security.reset();
        whileTest = true;

        System.setSecurityManager(security);
    }

    /**
     * @see ezunit.EzRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        whileTest = false;

        System.setSecurityManager(platform);
    }

    /**
     * @see ezunit.EzRule#afterClass()
     */
    @Override
    protected void afterClass() {
        System.setSecurityManager(platform);
    }

    /**
     * <p>
     * Set permission whether you can read file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to read.
     */
    public void readable(boolean allow) {
        if (allow) {
            security.runtime.clear(read);
        } else {
            security.runtime.set(read);
        }
    }

    /**
     * <p>
     * Set permission whether you can write file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called in test
     * method.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to write.
     */
    public void readable(boolean allow, String... paths) {
        File[] files = new File[paths.length];

        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        writable(allow, files);
    }

    /**
     * <p>
     * Set permission whether you can write file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called in test
     * method.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to write.
     */
    public void readable(boolean allow, File... files) {
        for (File file : files) {
            if (file != null) {
                security.readables.add(file.getAbsolutePath(), allow, whileTest);
            }
        }
    }

    /**
     * <p>
     * Set permission whether you can read file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to read.
     */
    public void writable(boolean allow) {
        if (whileTest) {
            security.runtime.set(write);
        } else {
            security.permissions.set(write);
        }
    }

    /**
     * <p>
     * Set permission whether you can write file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called in test
     * method.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to write.
     */
    public void writable(boolean allow, String... paths) {
        File[] files = new File[paths.length];

        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        writable(allow, files);
    }

    /**
     * <p>
     * Set permission whether you can write file or not.
     * </p>
     * <p>
     * This permission is effective only in the test method by which this method is called in test
     * method.
     * </p>
     * 
     * @param allow <code>true</code> if you allow to write.
     */
    public void writable(boolean allow, File... files) {
        for (File file : files) {
            if (file != null) {
                security.writables.add(file.getAbsolutePath(), allow, whileTest);
            }
        }
    }

    /**
     * @version 2010/02/09 10:26:03
     */
    private static class Security extends SecurityManager {

        /** The base permission state. */
        private final BitSet permissions = new BitSet();

        /** The runtime permission state. */
        private final BitSet runtime = new BitSet();

        /** The parent security manager. */
        private final SecurityManager manager;

        /** The file permissions. */
        private final Warranties readables = new Warranties();

        /** The file permissions. */
        private final Warranties writables = new Warranties();

        /**
         * @param permissions
         */
        private Security(SecurityManager manager, int permissions) {
            this.manager = manager;

            if ((permissions & READ) != 0) {
                this.permissions.set(read);
            }

            if ((permissions & WRITE) != 0) {
                this.permissions.set(write);
            }

            if ((permissions & CLASSLOADER) != 0) {
                this.permissions.set(classloader);
            }

            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            readClassPath(runtime.getBootClassPath());
            readClassPath(runtime.getClassPath());
            readClassPath(runtime.getLibraryPath());

            // initialize
            reset();
        }

        /**
         * Helper method to make classpath readable.
         * 
         * @param classPaths
         */
        private void readClassPath(String classPaths) {
            for (String classPath : classPaths.split(File.pathSeparator)) {
                // clean head
                if (classPath.charAt(0) == '/') {
                    classPath = classPath.substring(1);
                }

                // clean tail
                if (classPath.charAt(classPath.length() - 1) == '/') {
                    classPath = classPath.substring(0, classPath.length() - 2);
                }

                // Change to platform native separator.
                readables.add(classPath.replace('/', File.separatorChar), true, false);
            }
        }

        /**
         * Reset permission state.
         */
        private void reset() {
            runtime.clear();
            runtime.or(permissions);

            readables.clear();
            writables.clear();
        }

        /**
         * @see java.lang.SecurityManager#checkAccept(java.lang.String, int)
         */
        @Override
        public void checkAccept(String host, int port) {
            if (manager != null) manager.checkAccept(host, port);
        }

        /**
         * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
         */
        @Override
        public void checkAccess(Thread t) {
            if (manager != null) manager.checkAccess(t);
        }

        /**
         * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
         */
        @Override
        public void checkAccess(ThreadGroup g) {
            if (manager != null) manager.checkAccess(g);
        }

        /**
         * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
         */
        @Override
        public void checkAwtEventQueueAccess() {
            if (manager != null) manager.checkAwtEventQueueAccess();
        }

        /**
         * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
         */
        @Override
        public void checkConnect(String host, int port, Object context) {
            if (manager != null) manager.checkConnect(host, port, context);
        }

        /**
         * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
         */
        @Override
        public void checkConnect(String host, int port) {
            if (manager != null) manager.checkConnect(host, port);
        }

        /**
         * @see java.lang.SecurityManager#checkCreateClassLoader()
         */
        @Override
        public void checkCreateClassLoader() {
            if (runtime.get(classloader)) {
                throw new AccessControlException("Disallow to create new class loader.");
            }

            if (manager != null) manager.checkCreateClassLoader();
        }

        /**
         * @see java.lang.SecurityManager#checkDelete(java.lang.String)
         */
        @Override
        public void checkDelete(String file) {
            if (writables.reject(file, runtime.get(write))) {
                throw new AccessControlException("Disallow to delete file. " + file);
            }

            if (manager != null) manager.checkDelete(file);
        }

        /**
         * @see java.lang.SecurityManager#checkExec(java.lang.String)
         */
        @Override
        public void checkExec(String cmd) {
            if (manager != null) manager.checkExec(cmd);
        }

        /**
         * @see java.lang.SecurityManager#checkExit(int)
         */
        @Override
        public void checkExit(int status) {
            if (manager != null) manager.checkExit(status);
        }

        /**
         * @see java.lang.SecurityManager#checkLink(java.lang.String)
         */
        @Override
        public void checkLink(String lib) {
            if (manager != null) manager.checkLink(lib);
        }

        /**
         * @see java.lang.SecurityManager#checkListen(int)
         */
        @Override
        public void checkListen(int port) {
            if (manager != null) manager.checkListen(port);
        }

        /**
         * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
         */
        @Override
        public void checkMemberAccess(Class<?> clazz, int which) {
            if (manager != null) manager.checkMemberAccess(clazz, which);
        }

        /**
         * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
         */
        @Override
        public void checkMulticast(InetAddress maddr) {
            if (manager != null) manager.checkMulticast(maddr);
        }

        /**
         * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
         */
        @Override
        public void checkPackageAccess(String pkg) {
            if (manager != null) manager.checkPackageAccess(pkg);
        }

        /**
         * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
         */
        @Override
        public void checkPackageDefinition(String pkg) {
            if (manager != null) manager.checkPackageDefinition(pkg);
        }

        /**
         * @see java.lang.SecurityManager#checkPermission(java.security.Permission,
         *      java.lang.Object)
         */
        @Override
        public void checkPermission(Permission perm, Object context) {
            if (manager != null) manager.checkPermission(perm, context);
        }

        /**
         * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
         */
        @Override
        public void checkPermission(Permission perm) {
            if (manager != null) manager.checkPermission(perm);
        }

        /**
         * @see java.lang.SecurityManager#checkPrintJobAccess()
         */
        @Override
        public void checkPrintJobAccess() {
            if (manager != null) manager.checkPrintJobAccess();
        }

        /**
         * @see java.lang.SecurityManager#checkPropertiesAccess()
         */
        @Override
        public void checkPropertiesAccess() {
            if (manager != null) manager.checkPropertiesAccess();
        }

        /**
         * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
         */
        @Override
        public void checkPropertyAccess(String key) {
            if (manager != null) manager.checkPropertyAccess(key);
        }

        /**
         * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
         */
        @Override
        public void checkRead(FileDescriptor fd) {
            if (readables.reject(fd.toString(), runtime.get(read))) {
                throw new AccessControlException("Disallow to read file. " + fd);
            }

            manager.checkRead(fd);
        }

        /**
         * @see java.lang.SecurityManager#checkRead(java.lang.String, java.lang.Object)
         */
        @Override
        public void checkRead(String file, Object context) {
            if (readables.reject(file, runtime.get(read))) {
                throw new AccessControlException("Disallow to read file. " + file);
            }

            if (manager != null) manager.checkRead(file, context);
        }

        /**
         * @see java.lang.SecurityManager#checkRead(java.lang.String)
         */
        @Override
        public void checkRead(String file) {
            if (readables.reject(file, runtime.get(read))) {
                throw new AccessControlException("Disallow to read file. " + file);
            }

            if (manager != null) manager.checkRead(file);
        }

        /**
         * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
         */
        @Override
        public void checkSecurityAccess(String target) {
            if (manager != null) manager.checkSecurityAccess(target);
        }

        /**
         * @see java.lang.SecurityManager#checkSetFactory()
         */
        @Override
        public void checkSetFactory() {
            if (manager != null) manager.checkSetFactory();
        }

        /**
         * @see java.lang.SecurityManager#checkSystemClipboardAccess()
         */
        @Override
        public void checkSystemClipboardAccess() {
            if (manager != null) manager.checkSystemClipboardAccess();
        }

        /**
         * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
         */
        @Override
        public boolean checkTopLevelWindow(Object window) {
            if (manager != null) {
                return manager.checkTopLevelWindow(window);
            } else {
                return super.checkTopLevelWindow(window);
            }
        }

        /**
         * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
         */
        @Override
        public void checkWrite(FileDescriptor fd) {
            if (writables.reject(fd.toString(), runtime.get(write))) {
                throw new AccessControlException("Disallow to write file. " + fd);
            }

            if (manager != null) manager.checkWrite(fd);
        }

        /**
         * @see java.lang.SecurityManager#checkWrite(java.lang.String)
         */
        @Override
        public void checkWrite(String file) {
            if (writables.reject(file, runtime.get(write))) {
                throw new AccessControlException("Disallow to write file. " + file);
            }

            if (manager != null) manager.checkWrite(file);
        }

        /**
         * @see java.lang.SecurityManager#getClassContext()
         */
        @Override
        protected Class[] getClassContext() {
            return super.getClassContext();
        }

        /**
         * @see java.lang.SecurityManager#getSecurityContext()
         */
        @Override
        public Object getSecurityContext() {
            if (manager != null) {
                return manager.getSecurityContext();
            } else {
                return super.getSecurityContext();
            }
        }

        /**
         * @see java.lang.SecurityManager#getThreadGroup()
         */
        @Override
        public ThreadGroup getThreadGroup() {
            if (manager != null) {
                return manager.getThreadGroup();
            } else {
                return super.getThreadGroup();
            }
        }
    }

    /**
     * @version 2010/02/10 22:53:41
     */
    private static class Warranties {

        /** The base permissions. */
        private List<Warranty> permissions = new ArrayList();

        /** The runtime permissions. */
        private List<Warranty> runtime = new ArrayList();

        /**
         * Register permmision.
         * 
         * @param path
         * @param accessible
         * @param isRuntime
         */
        private void add(String path, boolean accessible, boolean isRuntime) {
            Warranty warranty = new Warranty(path, accessible);

            if (isRuntime) {
                runtime.add(warranty);
            } else {
                permissions.add(warranty);
            }
        }

        /**
         * Check whether the specified path is acceptable or not.
         * 
         * @param path
         * @return A result.
         */
        private boolean reject(String path, boolean defaultValue) {
            path = new File(path).getAbsolutePath();

            // runtime
            for (int i = runtime.size() - 1; 0 <= i; i--) {
                Warranty warranty = runtime.get(i);

                if (path.startsWith(warranty.path)) {
                    return !warranty.accessible;
                }
            }

            // base
            for (int i = permissions.size() - 1; 0 <= i; i--) {
                Warranty warranty = permissions.get(i);

                if (path.startsWith(warranty.path)) {
                    return !warranty.accessible;
                }
            }

            // API definition
            return defaultValue;
        }

        /**
         * Clear runtime permissions.
         */
        private void clear() {
            runtime.clear();
        }
    }

    /**
     * @version 2010/02/10 22:53:39
     */
    private static class Warranty {

        /** The location path. */
        private final String path;

        /** The permission. */
        private final boolean accessible;

        /**
         * @param path
         * @param accessible
         */
        private Warranty(String path, boolean accessible) {
            this.path = new File(path).getAbsolutePath();
            this.accessible = accessible;
        }
    }
}
