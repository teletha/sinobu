/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.scratchpad;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle.Control;

import org.junit.Rule;
import org.junit.Test;

import ezbean.Extensible;
import ezbean.I;
import ezbean.Manageable;
import ezbean.Singleton;
import ezbean.unit.ClassModule;

/**
 * @version 2010/01/05 19:51:10
 */
public class I18NTest {

    @Rule
    public static final ClassModule module = new ClassModule();

    @Test
    public void i18n() throws Exception {
        assertEquals("message1", i18n(MessageBundle.class).message());
    }

    /** The locale name resolver. */
    private static final Control control = Control.getControl(Control.FORMAT_CLASS);

    /**
     * <p>
     * </p>
     * 
     * @param <M>
     * @param bundleClass
     * @return
     */
    public static <M extends Extensible> M i18n(Class<M> bundleClass) {
        List<Locale> locales = control.getCandidateLocales("", I.make(Locale.class));
        List<M> bundles = I.find(bundleClass);

        for (int i = 0; i < locales.size() - 1; i++) {
            String name = control.toBundleName(bundleClass.getSimpleName(), locales.get(i));

            for (int j = 0; j < bundles.size(); j++) {
                if (bundles.get(j).getClass().getSimpleName().equals(name)) {
                    return bundles.get(j);
                }
            }
        }
        return bundles.get(0);
    }

    /**
     * @version 2010/01/05 19:55:34
     */
    @Manageable(lifestyle = Singleton.class)
    private static class MessageBundle implements Extensible {

        public String message() {
            return "message1";
        }

        public String messageWithParam(int number) {
            return "message" + number;
        }
    }

    /**
     * @version 2010/01/05 19:55:37
     */
    private static class MessageBundle_ja extends MessageBundle {

        public String message() {
            return "メッセージ1";
        }

        public String messageWithParam(int number) {
            return "メッセージ" + number;
        }
    }
}
