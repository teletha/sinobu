/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import ezbean.module.external.AnnotatedClass1;
import ezbean.module.external.AnnotatedClass2;
import ezbean.module.external.AnnotatedClass3;
import ezbean.module.external.AnnotatedExtendedClass1;
import ezbean.module.external.ExtendedClass1;
import ezbean.module.external.ExtendedClass2;
import ezbean.module.external.ExtendedClass3;
import ezbean.module.external.ExtendedClass4;
import ezbean.module.external.ExtendedClass5;
import ezbean.module.external.SingletonClass;
import ezbean.sample.ClassAnnotation;
import ezbean.sample.MarkerInterface1;
import ezbean.sample.MarkerInterface2;
import ezbean.sample.RuntimeAnnotation1;
import ezbean.sample.RuntimeAnnotation2;
import ezbean.sample.SourceAnnotation;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 16:37:20
 */
@SuppressWarnings("resource")
public class ModuleTest {

    @Rule
    public static PrivateModule external = new PrivateModule("module/external", true, false);

    @Rule
    public static PrivateModule jar = new PrivateModule("module/external", true, true);

    @Test
    public void modulePath() throws Exception {
        Module module = new Module(external.path);
        assert module != null;
        assert external.path == module.path;
    }

    @Test
    public void jarModulePath() throws Exception {
        Module module = new Module(jar.path);
        assert module != null;
        assert jar.path == module.path;
    }

    @Test
    public void moduleClassloader() throws Exception {
        Module module = new Module(external.path);
        assert module != null;
        assert I.$loader != module;
    }

    @Test
    public void jarModuleClassloader() throws Exception {
        Module module = new Module(jar.path);
        assert module != null;
        assert I.$loader != module;
    }

    @Test
    public void findProviders1() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<MarkerInterface1>> providers = module.find(MarkerInterface1.class, false);
        assert providers != null;
        assert 3 == providers.size();

        // collect names to assert
        check(providers, AnnotatedExtendedClass1.class, ExtendedClass3.class, ExtendedClass5.class);
    }

    @Test
    public void findProvidersFromJar1() throws Exception {
        Module module = new Module(jar.path);
        assert module != null;

        List<Class<MarkerInterface1>> providers = module.find(MarkerInterface1.class, false);
        assert providers != null;
        assert 3 == providers.size();

        // collect names to assert
        check(providers, AnnotatedExtendedClass1.class, ExtendedClass3.class, ExtendedClass5.class);
    }

    @Test
    public void findProviders2() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<MarkerInterface2>> providers = module.find(MarkerInterface2.class, false);
        assert providers != null;
        assert 1 == providers.size();

        // collect names to assert
        check(providers, ExtendedClass4.class);
    }

    @Test
    public void findProvidersFomJar2() throws Exception {
        Module module = new Module(jar.path);
        assert module != null;

        List<Class<MarkerInterface2>> providers = module.find(MarkerInterface2.class, false);
        assert providers != null;
        assert 1 == providers.size();

        // collect names to assert
        check(providers, ExtendedClass4.class);
    }

    /**
     * Find all classes which extends Object class. In other words, it is all service provider
     * classes.
     */
    @Test
    public void findProviders3() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<Object>> providers = module.find(Object.class, false);
        assert providers != null;
        assert 10 == providers.size();

        // collect names to assert
        check(providers, AnnotatedClass1.class, AnnotatedClass2.class, AnnotatedClass3.class, AnnotatedExtendedClass1.class, ExtendedClass1.class, ExtendedClass2.class, ExtendedClass3.class, ExtendedClass4.class, ExtendedClass5.class, SingletonClass.class);
    }

    @Test
    public void findRuntimeAnnotatedClass1() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<RuntimeAnnotation1>> providers = module.find(RuntimeAnnotation1.class, false);
        assert providers != null;
        assert 3 == providers.size();

        // collect names to assert
        check(providers, AnnotatedClass1.class, AnnotatedClass3.class, AnnotatedExtendedClass1.class);
    }

    @Test
    public void findRuntimeAnnotatedClass2() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<RuntimeAnnotation2>> providers = module.find(RuntimeAnnotation2.class, false);
        assert providers != null;
        assert 2 == providers.size();

        // collect names to assert
        check(providers, AnnotatedClass2.class, AnnotatedClass3.class);
    }

    @Test
    public void findSourceAnnotatedClass() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<SourceAnnotation>> providers = module.find(SourceAnnotation.class, false);
        assert providers != null;
        assert 0 == providers.size();
    }

    @Test
    public void findClassAnnotatedClass() throws Exception {
        Module module = new Module(external.path);
        assert module != null;

        List<Class<ClassAnnotation>> providers = module.find(ClassAnnotation.class, false);
        assert providers != null;
        assert 0 == providers.size();
    }

    @Test
    public void moduleInModule() throws Exception {
        Module module = new Module(Paths.get("src/test/resources/ezbean/inline.zip"));
        assert module != null;

        List<Class<Object>> providers = module.find(Object.class, false);
        assert providers != null;
        assert 0 == providers.size();
    }

    /**
     * Helper method to assert classes.
     * 
     * @param providers
     * @param classes
     */
    private void check(List providers, Class... classes) {
        // collect names to assert
        Set names = new HashSet();

        for (Object provider : providers) {
            names.add(((Class) provider).getName());
        }

        for (Class clazz : classes) {
            assert names.contains(external.convert(clazz).getName());
        }
    }
}
