/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import kiss.I;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import antibug.util.Note;
import antibug.xml.XML;

/**
 * <p>
 * Antibug is highly testable utility, which can manipulate tester objects as a extremely-condensed
 * facade.
 * </p>
 * 
 * @version 2012/02/16 15:56:29
 */
public class AntiBug {

    /**
     * <p>
     * Search caller testcase class.
     * </p>
     * 
     * @return A testcase class.
     */
    public static final Class getCaller() {
        // caller
        Exception e = new Exception();
        StackTraceElement[] elements = e.getStackTrace();

        for (StackTraceElement element : elements) {
            String name = element.getClassName();

            if (name.endsWith("Test")) {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException classNotFoundException) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(classNotFoundException);
                }
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error("Testcas is not found.");
    }

    /**
     * <p>
     * Locate the specified FilePath name.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located {@link File}.
     */
    public static final Path locate(String filePath) {
        return locateFileFromCaller(filePath);
    }

    /**
     * <p>
     * Locate a package directory that the specified class exists.
     * </p>
     * 
     * @param clazz A class to resolve location.
     * @return A located package directory.
     * @throws NullPointerException If the class is <code>null</code>.
     */
    public static final Path locatePackage(Class clazz) {
        try {
            return Paths.get(clazz.getResource("").toURI());
        } catch (URISyntaxException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Locate the specified FilePath name with the context which is located by the caller class.
     * </p>
     * 
     * @param filePath A FilePath name to resolve location.
     * @return A located file.
     */
    private static Path locateFileFromCaller(String filePath) {
        Class caller = getCaller();
        URL url = caller.getResource(filePath);

        if (url == null) {
            throw new AssertionError("The resource is not found. [" + filePath + "]");
        }

        // resolve FilePath location
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Take a note.
     * </p>
     * 
     * @param contents
     * @return
     */
    public static Note note(String... contents) {
        return new Note(contents);
    }

    /**
     * <p>
     * Reads all characters from a FilePath into a {@link String}, using the given character set or
     * {@link I#getEncoding()}.
     * </p>
     * 
     * @param FilePath A FilePath to read from.
     * @param charset A character set used when reading the file.
     * @return A string containing all the characters from the file.
     */
    public static String read(Path path, Charset... charset) {
        StringBuilder builder = new StringBuilder();

        for (String line : readLines(path, charset)) {
            builder.append(line).append(File.separatorChar);
        }

        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * <p>
     * Reads the first line from a file. The line does not include line-termination characters, but
     * does include other leading and trailing whitespace.
     * </p>
     * 
     * @param FilePath A FilePath to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the FilePath is empty
     * @throws IOException if an I/O error occurs
     */
    public static final String readLine(Path path, Charset... charset) {
        List<String> lines = readLines(path, option(charset, I.$encoding), false);

        return lines.size() == 0 ? "" : lines.get(0);
    }

    /**
     * <p>
     * Reads all of the lines from a file. The lines do not include line-termination characters, but
     * do include other leading and trailing whitespace.
     * </p>
     * 
     * @param FilePath A FilePath to read from
     * @param charset A character set used when writing the file. If you don't specify, Otherwise
     *            {@link I#getEncoding()}.
     * @return the first line, or null if the FilePath is empty
     * @throws IOException if an I/O error occurs
     */
    public static final List<String> readLines(Path path, Charset... charset) {
        return readLines(path, option(charset, I.$encoding), true);
    }

    /**
     * Helper method to decide option.
     * 
     * @param <T>
     * @param option
     * @param defaultValue
     * @return
     */
    private static <T> T option(T[] option, T defaultValue) {
        return option != null && option.length != 0 && option[0] != null ? option[0] : defaultValue;
    }

    /**
     * Read FilePath contents actually.
     * 
     * @param path
     * @param charset
     * @param all
     * @return
     */
    private static List<String> readLines(Path path, Charset charset, boolean all) {
        try {
            // convert to native path
            path = Paths.get(path.toString());

            List<String> lines = Files.readAllLines(path, charset);

            return all ? lines : lines.isEmpty() ? Collections.EMPTY_LIST : Collections.singletonList(lines.get(0));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param text A xml text.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(String text, XMLFilter... filters) {
        return XML.xml(text, filters);
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param path A xml source path.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(Path path, XMLFilter... filters) {
        return XML.xml(path, filters);
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param source A xml source.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(InputSource source, XMLFilter... filters) {
        return XML.xml(source, filters);
    }

    /**
     * <p>
     * Wrap xml document.
     * </p>
     * 
     * @param doc A parsed document.
     * @return
     */
    public static XML xml(Document doc) {
        return XML.xml(doc);
    }

    /**
     * <p>
     * Wrap xml document.
     * </p>
     * 
     * @param filter
     * @return
     */
    public static XML xml(XMLFilter filter) {
        return XML.xml(filter);
    }
}
