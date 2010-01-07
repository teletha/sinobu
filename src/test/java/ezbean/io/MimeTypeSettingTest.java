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
package ezbean.io;

import static org.junit.Assert.*;

import javax.activation.FileTypeMap;

import org.junit.Test;

/**
 * DOCUMENT.
 * 
 * @version 2008/12/05 4:46:22
 */
public class MimeTypeSettingTest {

    @Test
    public void zip() {
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();

        assertEquals("application/zip", map.getContentType("test.zip"));
        assertEquals("application/zip", map.getContentType("test.jar"));
        assertEquals("application/zip", map.getContentType("test.war"));
        assertEquals("application/zip", map.getContentType("test.ear"));
    }

    @Test
    public void html() {
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();

        assertEquals("text/html", map.getContentType("test.html"));
        assertEquals("text/html", map.getContentType("test.htm"));
    }

    @Test
    public void jpeg() {
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();

        assertEquals("image/jpeg", map.getContentType("test.jpeg"));
        assertEquals("image/jpeg", map.getContentType("test.jpg"));
    }
}
