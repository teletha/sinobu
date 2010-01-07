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

import ezbean.Extensible;

/**
 * @version 2010/01/05 19:51:10
 */
public class I18N {

    public static void main(String[] args) {
        i18n(MessageBundle.class).message();
    }

    public static <M> M i18n(Class<M> bundle) {
        return null;
    }

    /**
     * @version 2010/01/05 19:55:34
     */
    protected static class MessageBundle<T> implements Extensible {

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
    protected static class MessageBundle_ja extends MessageBundle<ja> {

        public String message() {
            return "メッセージ1";
        }

        public String messageWithParam(int number) {
            return "メッセージ" + number;
        }
    }

    protected static class ja {

    }
}
