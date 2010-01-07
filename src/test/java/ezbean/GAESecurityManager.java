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
package ezbean;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.AccessControlException;
import java.security.Permission;

/**
 * @version 2009/06/16 15:19:03
 */
public class GAESecurityManager extends SecurityManager {

    /**
     * @see java.lang.SecurityManager#checkAccept(java.lang.String, int)
     */
    @Override
    public void checkAccept(String host, int port) {

    }

    /**
     * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
     */
    @Override
    public void checkAccess(Thread t) {

    }

    /**
     * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
     */
    @Override
    public void checkAccess(ThreadGroup g) {

    }

    /**
     * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
     */
    @Override
    public void checkAwtEventQueueAccess() {

    }

    /**
     * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
     */
    @Override
    public void checkConnect(String host, int port, Object context) {

    }

    /**
     * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
     */
    @Override
    public void checkConnect(String host, int port) {

    }

    /**
     * @see java.lang.SecurityManager#checkCreateClassLoader()
     */
    @Override
    public void checkCreateClassLoader() {

    }

    /**
     * @see java.lang.SecurityManager#checkDelete(java.lang.String)
     */
    @Override
    public void checkDelete(String file) {

    }

    /**
     * @see java.lang.SecurityManager#checkExec(java.lang.String)
     */
    @Override
    public void checkExec(String cmd) {

    }

    /**
     * @see java.lang.SecurityManager#checkExit(int)
     */
    @Override
    public void checkExit(int status) {

    }

    /**
     * @see java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @Override
    public void checkLink(String lib) {

    }

    /**
     * @see java.lang.SecurityManager#checkListen(int)
     */
    @Override
    public void checkListen(int port) {

    }

    /**
     * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
     */
    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {

    }

    /**
     * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
     */
    @Override
    public void checkMulticast(InetAddress maddr) {

    }

    /**
     * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
     */
    @Override
    public void checkPackageAccess(String pkg) {

    }

    /**
     * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
     */
    @Override
    public void checkPackageDefinition(String pkg) {

    }

    /**
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
     */
    @Override
    public void checkPermission(Permission perm, Object context) {

    }

    /**
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    @Override
    public void checkPermission(Permission perm) {

    }

    /**
     * @see java.lang.SecurityManager#checkPrintJobAccess()
     */
    @Override
    public void checkPrintJobAccess() {

    }

    /**
     * @see java.lang.SecurityManager#checkPropertiesAccess()
     */
    @Override
    public void checkPropertiesAccess() {

    }

    /**
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     */
    @Override
    public void checkPropertyAccess(String key) {

    }

    /**
     * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
     */
    @Override
    public void checkRead(FileDescriptor fd) {
        throw new AccessControlException(fd.toString());
    }

    /**
     * @see java.lang.SecurityManager#checkRead(java.lang.String, java.lang.Object)
     */
    @Override
    public void checkRead(String file, Object context) {
        throw new AccessControlException(file);
    }

    /**
     * @see java.lang.SecurityManager#checkRead(java.lang.String)
     */
    @Override
    public void checkRead(String file) {
        throw new AccessControlException(file);
    }

    /**
     * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
     */
    @Override
    public void checkSecurityAccess(String target) {

    }

    /**
     * @see java.lang.SecurityManager#checkSetFactory()
     */
    @Override
    public void checkSetFactory() {

    }

    /**
     * @see java.lang.SecurityManager#checkSystemClipboardAccess()
     */
    @Override
    public void checkSystemClipboardAccess() {

    }

    /**
     * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
     */
    @Override
    public boolean checkTopLevelWindow(Object window) {
        return false;
    }

    /**
     * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
     */
    @Override
    public void checkWrite(FileDescriptor fd) {

    }

    /**
     * @see java.lang.SecurityManager#checkWrite(java.lang.String)
     */
    @Override
    public void checkWrite(String file) {

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
        return super.getSecurityContext();
    }

    /**
     * @see java.lang.SecurityManager#getThreadGroup()
     */
    @Override
    public ThreadGroup getThreadGroup() {
        return super.getThreadGroup();
    }
}
