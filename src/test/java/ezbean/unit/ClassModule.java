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
package ezbean.unit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import ezbean.I;
import ezbean.io.FileSystem;
import ezbean.model.ClassUtil;

/**
 * @version 2009/12/31 14:38:51
 */
public class ClassModule extends EzRule {

    /** The class module. */
    public final File moduleFile = FileSystem.createTemporary();

    /**
     * @see ezbean.unit.EzRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
        // create package directory
        String path = testcase.getPackage().getName().replace('.', '/');
        File dest = I.locate(moduleFile, path);
        dest.mkdirs();

        // copy class files
        File source = I.locate(ClassUtil.getArchive(testcase), path);

        for (File file : source.listFiles()) {
            if (file.getName().startsWith(testcase.getSimpleName() + "$")) {
                try {
                    FileSystem.copy(file, dest);
                } catch (IOException e) {
                    I.quiet(e);
                }
            }
        }
    }

    /**
     * @see ezbean.unit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        // load as module
        I.load(moduleFile);
    }

    /**
     * @see ezbean.unit.EzRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        I.unload(moduleFile);
    }
}
