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
package ezbean.model;

import java.io.File;

import ezbean.I;

/**
 * TODO Can you merge this class into {@link Codec}?
 * 
 * @version 2008/06/21 12:39:13
 */
class CodecFile extends Codec<File> {

    /**
     * @see ezbean.model.Codec#decode(java.lang.String)
     */
    public File decode(String value) {
        return I.locate(value);
    }

    /**
     * @see ezbean.model.Codec#encode(java.lang.Object)
     */
    public String encode(File value) {
        // java.io.File#toString() method may return a relative path name. So we must cast to
        // ezbean.io.File. And we can't use java.io.File#getAbsolutePath() method to
        // retrive literal expression because we want the normalized literal expression.
        return I.locate(value.getPath()).toString();
    }
}
