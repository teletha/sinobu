/*
 * Copyright (C) 2012 Nameless Production Committee.
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
package ezunit;

import java.util.List;

import org.junit.Rule;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ezunit.Agent.Translator;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert extends ReusableRule {

    @Rule
    private final Agent agent = new Agent(new PowerAssertTranslator());

    /** The caller class. */
    private final Class caller;

    /** The internal name of caller class. */
    private final String internalName;

    /**
     * Assertion Utility.
     */
    public PowerAssert() {
        this.caller = UnsafeUtility.getCaller(1);
        this.internalName = caller.getName().replace('.', '/');

        // force to transform
        agent.transform(caller);
    }

    /**
     * @version 2012/01/10 11:51:03
     */
    private final class PowerAssertTranslator implements Translator {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canTranslate(String name) {
            return name.equals(internalName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void translate(ClassNode ast) {
            for (MethodNode node : (List<MethodNode>) ast.methods) {
                System.out.println(node.localVariables);
            }
        }
    }

    public static final void validate() {

    }
}
