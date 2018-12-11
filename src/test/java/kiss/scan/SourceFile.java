/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.tools.SimpleJavaFileObject;

/**
 * @version 2017/04/22 11:51:35
 */
public class SourceFile extends SimpleJavaFileObject {

    /** The path to source file. */
    private final Path path;

    /**
     * @param uri
     * @param kind
     */
    private SourceFile(Path path) {
        super(path.toUri(), Kind.SOURCE);

        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        StringBuilder builder = new StringBuilder();
        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            builder.append(line).append("\r\n");
        }
        return builder;
    }

    /**
     * <p>
     * Resolve as source file.
     * </p>
     * 
     * @param clazz
     * @return
     */
    public static SourceFile of(Class clazz) {
        Path path = Paths.get("src/test/java/" + clazz.getName().replaceAll("\\.", "/") + ".java");

        if (Files.notExists(path)) {
            throw new Error("Source file [" + clazz + "] is not found.");
        }

        return new SourceFile(path);
    }
}
