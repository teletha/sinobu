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

import java.net.URL;
import java.net.URLClassLoader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import ezbean.I;
import ezbean.model.Model;

/**
 * <h1>Generated Class Naming Strategy</h1>
 * <p>
 * We adopted Suffix Naming Strategy of an autematically generated class at first (e.g.
 * SomeClass$$AuteGenerated). But this strategy has problem against to core package classes (e.g.
 * swing components). Therefore, we adopt Preffix Naming Strategy now.
 * </p>
 * 
 * @version 2009/12/30 22:37:01
 */
public class ModuleLoader extends URLClassLoader {

    /**
     * The root class loader in EasyBena environment. This {@link ModuleLoader} can access all
     * classes which are managed and enhanced by Ezbean.
     */
    private static ModuleLoader root;

    /**
     * Create ModuleLoader instance.
     * 
     * @param parent A parent class loader.
     * @param urls A list of class paths.
     */
    protected ModuleLoader(ClassLoader parent, URL... urls) {
        super(urls, parent);
    }

    /**
     * @see java.net.URLClassLoader#findResource(java.lang.String)
     */
    @Override
    public URL findResource(String name) {
        if (name.charAt(0) == '$') {
            name = name.substring(2);
        }
        return getResource(name);
    }

    /**
     * MEMO: Though we had tried to implement this functionality by using polymorphism, it was not
     * beautiful code because some methods were renamed or overrided to expose them to package or
     * subclass. So we currently implement it by using instance equivalence checking. This method
     * has a side effect too that decreases the file size.
     * 
     * @see java.net.URLClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        // check whether this class loader is root or not
        if (root != this) {
            // this is not root, we can find it normaly.
            return super.findClass(name);
        }

        // this is root, we must find the named class from children class loaders.
        for (Module module : I.make(ModuleRegistry.class).modules) {
            ModuleLoader moduleLoader = module.moduleLoader;

            if (moduleLoader == root) {
                continue;
            }

            // first, check whether the class is already loaded or not
            Class clazz = moduleLoader.findLoadedClass(name);

            if (clazz != null) {
                return clazz;
            }

            try {
                return moduleLoader.findClass(name);
            } catch (ClassNotFoundException e) {
                // continue
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Returns the automatic generated class which implements or extends the given model.
     * 
     * @param model A class information of the model.
     * @param trace If trace is <code>1</code>, the generated code will be traceable code for mock
     *            object, otherwise for normal bean object.
     * @return A generated {@link Class} object.
     */
    public synchronized Class loadClass(Model model, int trace) {
        // Compute fully qualified class name for the generated class.
        // The coder class name is prefix to distinguish enhancer type by a name and make core
        // package classes (e.g. swing components) enhance.
        // The statement "String name = coder.getName() + model.type.getName();" produces larger
        // byte code and more objects. To reduce them, we should use the method "concat".
        String name = "$".concat(String.valueOf(trace)).concat(model.type.getName());

        // find class from cache of class loader
        Class clazz = findLoadedClass(name);

        if (clazz == null) {
            // start writing byte code
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            // build interceptor chain
            ClassVisitor current = writer;

            for (Enhancer enhancer : I.find(Enhancer.class)) {
                current = enhancer.chain(current, model, name);
            }

            // write code actually
            ((Enhancer) current).write(trace);

            // retrieve byte code
            byte[] bytes = writer.toByteArray();

            // define class
            clazz = defineClass(name, bytes, 0, bytes.length, model.type.getProtectionDomain());
        }

        // API definition
        return clazz;
    }

    /**
     * <p>
     * Utility method to select the correct module class loader.
     * </p>
     * 
     * @param modelClass A target class. <code>null</code> will return the root module class loader.
     * @return A module class loader.
     */
    public static final ModuleLoader getModuleLoader(Class modelClass) {
        // check the specified class
        if (modelClass != null) {
            ClassLoader classLoader = modelClass.getClassLoader();

            if (classLoader instanceof ModuleLoader) {
                return (ModuleLoader) classLoader;
            }
        }

        synchronized (ModuleLoader.class) {
            if (root == null) {
                root = new ModuleLoader(I.getParentClassLoader());
            }

            // API definition
            return root;
        }
    }
}
