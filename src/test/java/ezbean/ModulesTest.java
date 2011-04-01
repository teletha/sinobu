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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ezbean.module.external.ExtendedClass1;
import ezbean.sample.MarkerInterface1;
import ezbean.sample.bean.Person;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 16:36:07
 */
public class ModulesTest {

    @Rule
    public static final PrivateModule module1 = new PrivateModule("module/external");

    @Rule
    public static final PrivateModule module2 = new PrivateModule("module/external", true, false);

    @Rule
    public static final PrivateModule module3 = new PrivateModule("module/external", true, false);

    @Rule
    public static final PrivateModule module4 = new PrivateModule("module/external", true, true);

    /** The clean and empty module repository for test. */
    private Modules modules;

    @Before
    public void before() {
        modules = new Modules();
        modules.modules.clear();
        modules.types.clear();
    }

    @Test
    public void loadModule() {
        assert 0 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
    }

    @Test
    public void loadJarModule() {
        assert 0 == modules.modules.size();
        modules.load(module4.path);
        assert 1 == modules.modules.size();
    }

    @Test
    public void loadMultipleModules() {
        assert 0 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
        modules.load(module2.path);
        assert 2 == modules.modules.size();
    }

    @Test
    public void loadNull() {
        assert 0 == modules.modules.size();
        modules.load((Path) null);
        assert 0 == modules.modules.size();
    }

    @Test
    public void loadNotExistModule() {
        assert 0 == modules.modules.size();
        modules.load(Paths.get("not-exist"));
        assert 0 == modules.modules.size();
    }

    @Test
    public void loadDuplicateClass() {
        assert 0 == modules.modules.size();
        modules.load(module2.path);
        assert 1 == modules.modules.size();

        Module first = modules.modules.get(0);
        List<Class<MarkerInterface1>> providers1 = first.find(MarkerInterface1.class, false);
        assert 3 == providers1.size();

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers1) {
            assert first == provider.getClassLoader();
        }

        // load another module which content is same
        modules.load(module3.path);
        Module second = modules.modules.get(1);
        List<Class<MarkerInterface1>> providers2 = second.find(MarkerInterface1.class, false);
        assert 3 == providers2.size();

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers2) {
            assert first == provider.getClassLoader();
        }

        // unload first module
        modules.unload(module2.path);
        List<Class<MarkerInterface1>> providers3 = second.find(MarkerInterface1.class, false);
        assert 3 == providers3.size();

        // assert class loader
        // all service providers should been loaded by second module
        for (Class provider : providers3) {
            assert second == provider.getClassLoader();
        }
    }

    @Test
    public void reload() {
        assert 0 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
    }

    @Test
    public void reloadRelativePathAndAbsolutePath() throws Exception {
        Path relativeModule = Paths.get("target/module");
        I.copy(module1.path, relativeModule);

        try {
            assert !relativeModule.isAbsolute();
            assert 0 == modules.modules.size();

            // as relative
            modules.load(relativeModule);
            assert 1 == modules.modules.size();

            // as absolute
            modules.load(relativeModule.toAbsolutePath());
            assert 1 == modules.modules.size();
        } finally {
            I.delete(relativeModule);
        }
    }

    @Test
    public void unloadModule() {
        assert 0 == modules.modules.size();
        modules.load(module1.path);
        assert 1 == modules.modules.size();
        modules.unload(module1.path);
        assert 0 == modules.modules.size();
    }

    @Test
    public void unloadJarModule() {
        assert 0 == modules.modules.size();
        modules.load(module4.path);
        assert 1 == modules.modules.size();
        modules.unload(module4.path);
        assert 0 == modules.modules.size();
    }

    @Test
    public void unloadNull() {
        assert 0 == modules.modules.size();
        modules.unload(module1.path);
        assert 0 == modules.modules.size();
    }

    @Test
    public void unloadNotExistModule() {
        assert 0 == modules.modules.size();
        modules.unload(Paths.get("not-exist"));
        assert 0 == modules.modules.size();
    }

    @Test
    public void unloadAwaredClassCorrectly() throws Exception {
        Map<Class, String> map = I.aware(new HashMap());
        map.put(Person.class, "This entry will not be unloaded.");
        map.put(module2.convert(ExtendedClass1.class), "This entry will be unloaded.");

        assert 2 == map.size();
        module2.unload();
        assert 1 == map.size();
    }
}
