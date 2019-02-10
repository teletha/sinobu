/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.scan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import kiss.Manageable;

public class Indexer extends AbstractProcessor {

    /** The indexable class manager in main source directory. */
    private Map<String, Set<String>> main = new HashMap<>();

    /** Type utility. */
    private Types types;

    /** File utility. */
    private Filer filer;

    /** Error utility. */
    private Messager messager;

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedOptions() {
        return Collections.EMPTY_SET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        types = env.getTypeUtils();
        filer = env.getFiler();
        messager = env.getMessager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        try {
            for (Element element : round.getRootElements()) {
                if (element instanceof TypeElement) {
                    element.accept(new ElementScanner8<Void, Void>() {

                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public Void visitType(TypeElement element, Void o) {
                            if (!inTestClass(element)) {
                                checkIndexableType(element, element);
                            }
                            return super.visitType(element, o);
                        }
                    }, null);
                }
            }

            if (!round.processingOver()) {
                return false;
            }

            // write index file
            for (Entry<String, Set<String>> entry : main.entrySet()) {
                String fileName = "META-INF/services/" + entry.getKey();
                Set<String> classNames = entry.getValue();
                FileObject file = null;

                try {
                    file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", fileName);

                    // read class names from the existing file
                    try (BufferedReader reader = new BufferedReader(file.openReader(true))) {
                        String className;

                        while ((className = reader.readLine()) != null) {
                            classNames.add(className);
                        }
                    }
                } catch (IOException e) {
                    // file is not found
                    file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
                }

                // write actually
                try (Writer writer = file.openWriter()) {
                    for (String className : classNames) {
                        writer.write(className);
                        System.out.println(className);
                        writer.write("\n");
                    }
                    writer.close();
                }
            }
        } catch (Throwable e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[Indexer] Internal error: " + e.getMessage());
        }
        return false;
    }

    /**
     * <p>
     * Check whether the specified element is indexable or not.
     * </p>
     * 
     * @param current A target element to check.
     * @param target A fully qualified class name to register.
     */
    private void checkIndexableType(TypeElement current, TypeElement target) {
        Manageable indexable = current.getAnnotation(Manageable.class);

        if (indexable == null) {
            // check super types
            for (TypeMirror superMirror : types.directSupertypes(current.asType())) {
                checkIndexableType((TypeElement) types.asElement(superMirror), target);
            }
        } else {
            if (current != target) {
                main.computeIfAbsent(fqcn(current), key -> new TreeSet()).add(fqcn(target));
            }
        }

    }

    private boolean inTestClass(TypeElement element) {
        switch (element.getNestingKind()) {
        case TOP_LEVEL:
            return element.getQualifiedName().toString().endsWith("Test");

        case MEMBER:
            Element enclosingElement = element.getEnclosingElement();

            if (enclosingElement instanceof TypeElement) {
                return inTestClass((TypeElement) enclosingElement);
            }
            return false;

        default:
            return false;
        }
    }

    /**
     * Compute fully qualified class name.
     * 
     * @param element
     * @return
     */
    private String fqcn(TypeElement element) {
        switch (element.getNestingKind()) {
        case TOP_LEVEL:
            return element.getQualifiedName().toString();

        case MEMBER:
            Element enclosingElement = element.getEnclosingElement();

            if (enclosingElement instanceof TypeElement) {
                String enclosingName = fqcn(((TypeElement) enclosingElement));

                if (enclosingName != null) {
                    return enclosingName + '$' + element.getSimpleName().toString();
                }
            }
            return null;

        default:
            return null;
        }
    }
}
