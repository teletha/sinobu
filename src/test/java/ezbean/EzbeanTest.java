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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.sample.bean.FinalBean;
import ezbean.sample.bean.Primitive;
import ezbean.sample.modifier.Abstract;
import ezbean.sample.modifier.Final;
import ezbean.sample.modifier.Nested.PublicStatic;
import ezbean.sample.modifier.Public;
import ezbean.xml.Encloser;

/**
 * @version 2011/12/09 20:31:05
 */
public class EzbeanTest {

    /**
     * Test public class. (top-level class)
     */
    @Test
    public void testInstantiate01() throws Exception {
        assert I.make(Public.class) != null;
    }

    /**
     * Test package private class.(top-level class)
     */
    @Test
    public void testInstantiate02() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested public static class. (static member type)
     */
    @Test
    public void testInstantiate03() throws Exception {
        assert I.make(PublicStatic.class) != null;
    }

    /**
     * Test Nested protected static class. (static member type)
     */
    @Test
    public void testInstantiate04() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$ProtectedStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested package private static class. (static member type)
     */
    @Test
    public void testInstantiate05() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PackagePrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested private static class. (static member type)
     */
    @Test
    public void testInstantiate06() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested public class. (non-static member type)
     */
    @Test
    public void testInstantiate07() throws Exception {
        assert I.make(ezbean.sample.modifier.Nested.Public.class) != null;
    }

    /**
     * Test Nested protected class. (non-static member type)
     */
    @Test
    public void testInstantiate08() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$Protected");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested package private class. (non-static member type)
     */
    @Test
    public void testInstantiate09() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested private class. (non-static member type)
     */
    @Test
    public void testInstantiate10() throws Exception {
        Class clazz = Class.forName("ezbean.sample.modifier.Nested$Private");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    public void instatiateFinal() throws Exception {
        assert I.make(Final.class) != null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void instatiateFinalBean() throws Exception {
        I.make(FinalBean.class);
    }

    @Test(expected = InstantiationException.class)
    public void instatiateAbstract() throws Exception {
        I.make(Abstract.class);
    }

    /**
     * Test runtime exception.
     */
    @Test(expected = RuntimeExceptionClass.E.class)
    public void instatiateRuntimeExceptionThrower() throws Exception {
        I.make(RuntimeExceptionClass.class);
    }

    /**
     * Test error.
     */
    @Test(expected = ErrorClass.E.class)
    public void instatiateErrorThrower() throws Exception {
        I.make(ErrorClass.class);
    }

    /**
     * Test exception.
     */
    @Test(expected = ExceptionClass.E.class)
    public void instatiateExceptionThrower() throws Exception {
        I.make(ExceptionClass.class);
    }

    /**
     * Test self circular reference.
     */
    @Test
    public void testSelfCircularReferenceInStaticInitializer() {
        SelfCircularReference instance = I.make(SelfCircularReference.class);
        assert instance != null;
        assert SelfCircularReference.instance != null;
        assert instance != SelfCircularReference.instance;
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
        assert instance != null;
        assert SingletonSelfCircularReference.instance != null;
        assert instance == SingletonSelfCircularReference.instance;
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
        assert I.make(List.class) != null;
    }

    /**
     * Test Map.
     */
    @Test
    public void testCollection02() throws Exception {
        assert I.make(Map.class) != null;
    }

    /**
     * Test int name.
     */
    @Test
    public void testReservedName01() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0 == primitive.getInt();

        primitive.setInt(100);
        assert 100 == primitive.getInt();
    }

    /**
     * Test long name.
     */
    @Test
    public void testReservedName02() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0L == primitive.getLong();

        primitive.setLong(100);
        assert 100L == primitive.getLong();
    }

    /**
     * Test boolean name.
     */
    @Test
    public void testReservedName03() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert false == primitive.isBoolean();

        primitive.setBoolean(true);
        assert true == primitive.isBoolean();
    }

    /**
     * Test method with invalid argument.
     */
    @Test
    public void testLoadWithNull() {
        I.load((Path) null);
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
    @Test(expected = NullPointerException.class)
    public void testParseWithNullInputSource() throws IOException {
        I.parse((InputSource) null);
    }

    @Test(expected = NullPointerException.class)
    public void testParseWithNullPath() throws IOException {
        I.parse((Path) null);
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
            assert e instanceof ClassNotFoundException;
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
