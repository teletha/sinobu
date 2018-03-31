/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.extension;

import java.io.Serializable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import kiss.Disposable;
import kiss.Extensible;
import kiss.I;
import kiss.model.Model;

/**
 * @version 2018/03/31 23:13:29
 */
public class RegistrationOrderTest {

    private Disposable disposer = Disposable.empty();

    @AfterEach
    public void after() {
        disposer.dispose();
    }

    @Test
    public void root_parent_child() throws Exception {
        register(Root.class, Parent.class, Child.class);

        assert I.find(ExtensionPoint.class, Object.class) instanceof Root;
        assert I.find(ExtensionPoint.class, Number.class) instanceof Parent;
        assert I.find(ExtensionPoint.class, Integer.class) instanceof Child;
    }

    @Test
    public void child_parent_root() throws Exception {
        register(Child.class, Parent.class, Root.class);

        assert I.find(ExtensionPoint.class, Object.class) instanceof Root;
        assert I.find(ExtensionPoint.class, Number.class) instanceof Parent;
        assert I.find(ExtensionPoint.class, Integer.class) instanceof Child;
    }

    @Test
    public void parent_root() throws Exception {
        register(Parent.class, Root.class);

        assert I.find(ExtensionPoint.class, Object.class) instanceof Root;
        assert I.find(ExtensionPoint.class, Number.class) instanceof Parent;
        assert I.find(ExtensionPoint.class, Integer.class) instanceof Parent;
    }

    @Test
    public void parent() throws Exception {
        register(Parent.class);

        assert I.find(ExtensionPoint.class, Object.class) == null;
        assert I.find(ExtensionPoint.class, Number.class) instanceof Parent;
        assert I.find(ExtensionPoint.class, Integer.class) instanceof Parent;
    }

    @Test
    public void parentInterface_childInterface() throws Exception {
        register(ParentInterface.class, ChildInterface.class);

        assert I.find(ExtensionPoint.class, Object.class) == null;
        assert I.find(ExtensionPoint.class, Number.class) instanceof ParentInterface;
        assert I.find(ExtensionPoint.class, Integer.class) instanceof ParentInterface;
        assert I.find(ExtensionPoint.class, Serializable.class) instanceof ParentInterface;
        assert I.find(ExtensionPoint.class, Comparable.class) instanceof ChildInterface;
    }

    private void register(Class<? extends ExtensionPoint>... e) {
        for (Class<? extends ExtensionPoint> extension : e) {
            Class type = (Class) Model.collectParameters(extension, ExtensionPoint.class)[0];
            disposer.add(I.load(ExtensionPoint.class, type, () -> I.make(extension)));
        }
    }

    private static interface ExtensionPoint<E> extends Extensible {
    }

    private static class Root implements ExtensionPoint<Object> {
    }

    private static class Parent implements ExtensionPoint<Number> {
    }

    private static class ParentInterface implements ExtensionPoint<Serializable> {
    }

    private static class Child implements ExtensionPoint<Integer> {
    }

    private static class ChildInterface implements ExtensionPoint<Comparable> {
    }
}
