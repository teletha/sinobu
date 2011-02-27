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

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.sample.bean.Primitive;
import ezbean.sample.modifier.Abstract;
import ezbean.sample.modifier.Nested.PublicStatic;
import ezbean.sample.modifier.Public;
import ezbean.xml.Encloser;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/18 10:42:20
 */
public class EzbeanTest {

    /**
     * Test public class. (top-level class)
     */
    @Test
    public void testInstantiate01() throws Exception {
        assertNotNull(I.make(Public.class));
    }

    /**
     * Test package private class.(top-level class)
     */
    @Test
    public void testInstantiate02() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.PackagePrivate");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested public static class. (static member type)
     */
    @Test
    public void testInstantiate03() throws Exception {
        assertNotNull(I.make(PublicStatic.class));
    }

    /**
     * Test Nested protected static class. (static member type)
     */
    @Test
    public void testInstantiate04() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$ProtectedStatic");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested package private static class. (static member type)
     */
    @Test
    public void testInstantiate05() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PackagePrivateStatic");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested private static class. (static member type)
     */
    @Test
    public void testInstantiate06() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PrivateStatic");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested public class. (non-static member type)
     */
    @Test
    public void testInstantiate07() throws Exception {
        assertNotNull(I.make(ezbean.sample.modifier.Nested.Public.class));
    }

    /**
     * Test Nested protected class. (non-static member type)
     */
    @Test
    public void testInstantiate08() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$Protected");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested package private class. (non-static member type)
     */
    @Test
    public void testInstantiate09() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PackagePrivate");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested private class. (non-static member type)
     */
    @Test
    public void testInstantiate10() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$Private");
        assertNotNull(clazz);
        assertNotNull(I.make(clazz));
    }

    /**
     * Test Nested public class.
     */
    @Test(expected = TypeNotPresentException.class)
    public void testInstantiate11() throws Exception {
        assertNotNull(I.make(Abstract.class));
    }

    /**
     * Test runtime exception.
     */
    @Test(expected = RuntimeExceptionClass.E.class)
    public void testInstantiate12() throws Exception {
        I.make(RuntimeExceptionClass.class);
    }

    /**
     * Test error.
     */
    @Test(expected = ErrorClass.E.class)
    public void testInstantiate13() throws Exception {
        I.make(ErrorClass.class);
    }

    /**
     * Test exception.
     */
    @Test(expected = ExceptionClass.E.class)
    public void testInstantiate14() throws Exception {
        I.make(ExceptionClass.class);
    }

    /**
     * Test self circular reference.
     */
    @Test
    public void testSelfCircularReferenceInStaticInitializer() {
        SelfCircularReference instance = I.make(SelfCircularReference.class);
        assertNotNull(instance);
        assertNotNull(SelfCircularReference.instance);
        assertNotSame(instance, SelfCircularReference.instance);
    }

    /**
     * @version 2008/11/03 21:44:17
     */
    private static class SelfCircularReference {

        private static final SelfCircularReference instance;

        static {
            instance = I.make(SelfCircularReference.class);
        }
    }

    /**
     * Test self circular reference for singleton.
     */
    @Test
    public void testSingletonSelfCircularReference() {
        SingletonSelfCircularReference instance = I.make(SingletonSelfCircularReference.class);
        assertNotNull(instance);
        assertNotNull(SingletonSelfCircularReference.instance);
        assertEquals(instance, SingletonSelfCircularReference.instance);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/11/03 21:44:17
     */
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonSelfCircularReference {

        private static final SingletonSelfCircularReference instance;

        static {
            instance = I.make(SingletonSelfCircularReference.class);
        }
    }

    /**
     * Test self circular reference.
     */
    @Test(expected = ClassCircularityError.class)
    public void testSelfCircularReferenceInConstructor() {
        I.make(SelfCircularReferenceInConstructor.class);
    }

    /**
     * @version 2009/05/16 10:23:40
     */
    private static class SelfCircularReferenceInConstructor {

        private SelfCircularReferenceInConstructor(SelfCircularReferenceInConstructor self) {
            // something
        }
    }

    /**
     * Test self circular reference.
     */
    @Test(expected = ClassCircularityError.class)
    public void testSingletonSelfCircularReferenceInConstructor() {
        I.make(SingletonSelfCircularReferenceInConstructor.class);
    }

    /**
     * @version 2009/05/16 10:23:40
     */
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonSelfCircularReferenceInConstructor {

        private SingletonSelfCircularReferenceInConstructor(SingletonSelfCircularReferenceInConstructor self) {
            // something
        }
    }

    /**
     * Test List.
     */
    @Test
    public void testCollection01() throws Exception {
        assertNotNull(I.make(List.class));
    }

    /**
     * Test Map.
     */
    @Test
    public void testCollection02() throws Exception {
        assertNotNull(I.make(Map.class));
    }

    /**
     * Test int name.
     */
    @Test
    public void testReservedName01() {
        Primitive primitive = I.make(Primitive.class);
        assertNotNull(primitive);
        assertEquals(0, primitive.getInt());

        primitive.setInt(100);
        assertEquals(100, primitive.getInt());
    }

    /**
     * Test long name.
     */
    @Test
    public void testReservedName02() {
        Primitive primitive = I.make(Primitive.class);
        assertNotNull(primitive);
        assertEquals(0L, primitive.getLong());

        primitive.setLong(100);
        assertEquals(100L, primitive.getLong());
    }

    /**
     * Test boolean name.
     */
    @Test
    public void testReservedName03() {
        Primitive primitive = I.make(Primitive.class);
        assertNotNull(primitive);
        assertEquals(false, primitive.isBoolean());

        primitive.setBoolean(true);
        assertEquals(true, primitive.isBoolean());
    }

    /**
     * Test method with invalid argument.
     */
    @Test
    public void testLoadWithNull() {
        I.load((File) null);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/11/10 20:02:15
     */
    private static class RuntimeExceptionClass {

        /**
         * Create RuntimeExceptionClass instance.
         */
        private RuntimeExceptionClass() {
            throw new E();
        }

        /**
         * DOCUMENT.
         * 
         * @version 2007/11/10 20:06:49
         */
        private static class E extends RuntimeException {

            private static final long serialVersionUID = 6965448734007115961L;
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/11/10 20:02:15
     */
    private static class ErrorClass {

        /**
         * Create ErrorClass instance.
         */
        private ErrorClass() {
            throw new E();
        }

        /**
         * DOCUMENT.
         * 
         * @version 2007/11/10 20:06:49
         */
        private static class E extends Error {

            private static final long serialVersionUID = 219714084165765163L;
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/11/10 20:02:15
     */
    private static class ExceptionClass {

        /**
         * Create ExceptionClass instance.
         */
        private ExceptionClass() throws E {
            throw new E();
        }

        /**
         * DOCUMENT.
         * 
         * @version 2007/11/10 20:06:49
         */
        private static class E extends Exception {

            private static final long serialVersionUID = 5333091127457345270L;
        }
    }

    // ===============================================================
    // Test Parse Method
    // ===============================================================

    /**
     * Parse with <code>null</code> source.
     */
    @Test(expected = NullPointerException.class)
    public void testParseWithNull() throws IOException {
        I.parse(null);
    }

    /**
     * Parse with <code>null</code> filter.
     */
    @Test(expected = NullPointerException.class)
    public void testParseWithNullFilter() throws IOException {
        I.parse(locateSource("xml/scanner/test001.xml"), (XMLFilter) null);
    }

    /**
     * Parse with <code>null</code> filters.
     */
    @Test(expected = NullPointerException.class)
    public void testParseWithNullFilters() throws IOException {
        I.parse(locateSource("xml/scanner/test001.xml"), (XMLFilter[]) null);
    }

    /**
     * Parse without filter.
     */
    @Test
    public void testParseWithoutFilter() throws IOException {
        I.parse(locateSource("xml/scanner/test001.xml"));
    }

    /**
     * Parse with filter.
     */
    @Test
    public void testParseWithFilter() throws IOException {
        I.parse(locateSource("xml/scanner/test001.xml"), new XMLFilterImpl());
    }

    /**
     * Parse without filter.
     */
    @Test(expected = SAXException.class)
    public void testParseWithInvalidSource() throws IOException {
        I.parse(locateSource("empty.txt"));
    }

    /**
     * Parse with filters.
     */
    @Test
    public void testParse() {
        assertXMLIdentical("xml/scanner/expected004.xml", "xml/scanner/test004.xml", new Encloser("first"), new Encloser("second"), new Encloser("third"));
    }

    // ===============================================================
    // Test Quiet Method
    // ===============================================================
    /**
     * Quiet with <code>null</code> exception.
     */
    @Test
    public void testQuietWithNull() {
        I.quiet(null);
    }

    /**
     * Test checked exception.
     */
    @Test(expected = ClassNotFoundException.class)
    public void testExceptionQuietly() {
        I.quiet(new ClassNotFoundException());
    }

    /**
     * Test checked exception.
     */
    @Test
    public void testCatchException() {
        try {
            throwError();
        } catch (Exception e) {
            assertTrue(e instanceof ClassNotFoundException);
        }
    }

    /**
     * Throw error.
     */
    private void throwError() {
        try {
            throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Test unchecked exception.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRuntimeExceptionQuietly() {
        I.quiet(new UnsupportedOperationException());
    }

    /**
     * Test error.
     */
    @Test(expected = LinkageError.class)
    public void testErrorQuietly() {
        I.quiet(new LinkageError());
    }

}
