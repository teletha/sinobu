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

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
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
 * @version 2010/11/14 21:42:48
 */
public class ModulesTest {

    @Rule
    public static final PrivateModule module1 = new PrivateModule("module/external");

    @Rule
    public static final PrivateModule module2 = new PrivateModule("module/external", true, false);

    @Rule
    public static final PrivateModule module3 = new PrivateModule("module/external", true, false);

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
        assertEquals(0, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
    }

    @Test
    public void loadMultipleModules() {
        assertEquals(0, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
        modules.load(module2.path);
        assertEquals(2, modules.modules.size());
    }

    @Test
    public void loadNull() {
        assertEquals(0, modules.modules.size());
        modules.load((Path) null);
        assertEquals(0, modules.modules.size());
    }

    @Test
    public void loadNotExistModule() {
        assertEquals(0, modules.modules.size());
        modules.load(new File("not-exist").toPath());
        assertEquals(0, modules.modules.size());
    }

    @Test
    public void loadDuplicateClass() {
        assertEquals(0, modules.modules.size());
        modules.load(module2.path);
        assertEquals(1, modules.modules.size());

        Module first = modules.modules.get(0);
        List<Class<MarkerInterface1>> providers1 = first.find(MarkerInterface1.class, false);
        assertEquals(3, providers1.size());

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers1) {
            assertEquals(first, provider.getClassLoader());
        }

        // load another module which content is same
        modules.load(module3.path);
        Module second = modules.modules.get(1);
        List<Class<MarkerInterface1>> providers2 = second.find(MarkerInterface1.class, false);
        assertEquals(3, providers2.size());

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers2) {
            assertEquals(first, provider.getClassLoader());
        }

        // unload first module
        modules.unload(module2.path);
        List<Class<MarkerInterface1>> providers3 = second.find(MarkerInterface1.class, false);
        assertEquals(3, providers3.size());

        // assert class loader
        // all service providers should been loaded by second module
        for (Class provider : providers3) {
            assertEquals(second, provider.getClassLoader());
        }
    }

    @Test
    public void reload() {
        assertEquals(0, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
    }

    @Test
    public void reloadRelativePathAndAbsolutePath() {
        File relativeModule = I.locate("target/module");
        I.copy(module1.path.toFile(), relativeModule);

        try {
            assertFalse(relativeModule.isAbsolute());
            assertEquals(0, modules.modules.size());

            // as relative
            modules.load(relativeModule.toPath());
            assertEquals(1, modules.modules.size());

            // as absolute
            modules.load(relativeModule.getAbsoluteFile().toPath());
            assertEquals(1, modules.modules.size());
        } finally {
            relativeModule.delete();
        }
    }

    @Test
    public void unloadModule() {
        assertEquals(0, modules.modules.size());
        modules.load(module1.path);
        assertEquals(1, modules.modules.size());
        modules.unload(module1.path);
        assertEquals(0, modules.modules.size());
    }

    @Test
    public void unloadNull() {
        assertEquals(0, modules.modules.size());
        modules.unload(module1.path);
        assertEquals(0, modules.modules.size());
    }

    @Test
    public void unloadNotExistModule() {
        assertEquals(0, modules.modules.size());
        modules.unload(new File("not-exist").toPath());
        assertEquals(0, modules.modules.size());
    }

    @Test
    public void unloadAwaredClassCorrectly() throws Exception {
        Map<Class, String> map = Modules.aware(new HashMap());
        map.put(Person.class, "This entry will not be unloaded.");
        map.put(module2.convert(ExtendedClass1.class), "This entry will be unloaded.");

        assertEquals(2, map.size());
        module2.unload();
        assertEquals(1, map.size());
    }
}
