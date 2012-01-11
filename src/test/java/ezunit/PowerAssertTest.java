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

import org.junit.Rule;
import org.junit.Test;

import ezunit.PowerAssert.PowerAssertionContext;
import ezunit.PowerAssert.PowerAssertionError;

/**
 * @version 2012/01/10 9:53:52
 */
public class PowerAssertTest {

    @Rule
    public static final PowerAssert test = new PowerAssert(true);

    @Test
    public void intConstantAndVariable() throws Exception {
        int value = 2;

        test.willCapture("1", 1);
        test.willCapture("value", value);
        assert 1 == value;
    }

    @Test
    public void longConstantAndVariable() throws Exception {
        long value = 2;

        test.willCapture("1", 1L);
        test.willCapture("value", value);
        assert 1L == value;
    }

    @Test
    public void floatConstantAndVariable() throws Exception {
        float value = 2;

        test.willCapture("1.0", 1f);
        test.willCapture("value", value);
        assert 1f == value;
    }

    @Test
    public void doubleConstantAndVariable() throws Exception {
        double value = 2;

        test.willCapture("1.0", 1d);
        test.willCapture("value", value);
        assert 1d == value;
    }

    @Test
    public void shortConstantAndVariable() throws Exception {
        short value = 2;

        test.willCapture("1", (short) 1);
        test.willCapture("value", value);
        assert (short) 1 == value;
    }

    public void asm() {
        PowerAssertionContext context = new PowerAssertionContext();
        short value = 2;
        context.add((short) 1);
        context.addVariable(value, "value");
        context.addExpression("==");

        throw new PowerAssertionError(context);
    }
}
