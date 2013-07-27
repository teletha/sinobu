/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * <p>
 * This interface is a listener for class loading and unloading event. These events are arisen
 * whenever the class type that you want to observe is loaded into or unloaded from the JVM for the
 * first time. However it is not that this listener can listen to all events caused by any dynamic
 * class loading against JVM. This listenr can listen to only event caused by the following two
 * methods ( {@link I#load(java.nio.file.Path)} and {@link I#unload(java.nio.file.Path)}).
 * </p>
 * <p>
 * You can specify the class type that you want to observe in type parameter. For instance, when you
 * want to observe the classes which impement the specified interface, as follows is done.
 * </p>
 * 
 * <pre>
 * public class Listener implements ClassLoadListener&lt;SpecifiedInterface&gt; {
 * 
 *     public void load(Class&lt;SpecifiedInterface&gt; clazz) {
 *         // do something
 * 
 *         assertTrue(SpecifiedInterface.isAssignableFrom(clazz));
 *     }
 * 
 *     public void unload(Class&lt;SpecifiedInterface&gt; clazz) {
 *         // do something
 * 
 *         assertTrue(SpecifiedInterface.isAssignableFrom(clazz));
 *     }
 * }
 * 
 * public class ThisClassWillBeLoaded implements SpecifiedInterface {
 * }
 * </pre>
 * <p>
 * {@link ClassListener} accepts not only so-called class (concreate class, abstract class and
 * interface) but also annotation. In the event that ClassLoadListener is parameterized by
 * annotation class, each methods are invoked whenever a class which is annotated by the parameter'
 * annotation is loaded.
 * </p>
 * 
 * <pre>
 * public class Listener implements ClassLoadListener&lt;SpecifiedAnnotation&gt; {
 * 
 *     public void load(Class&lt;SpecifiedAnnotation&gt; clazz) {
 *         // do something 
 *         SpecifiedAnnotation annotation = clazz.getAnnotation(SpecifiedAnnotation.class);
 * 
 *         assertNotNull(annotation);
 *     }
 * 
 *     public void unload(Class&lt;SpecifiedAnnotation&gt; clazz) {
 *         // do something
 *         SpecifiedAnnotation annotation = clazz.getAnnotation(SpecifiedAnnotation.class);
 * 
 *         assertNotNull(annotation);
 *     }
 * }
 * 
 * &#064;SpecifiedAnnotation
 * public class ThisClassWillBeLoaded {
 * }
 * </pre>
 * <p>
 * If you don't use any parameter, {@link ClassListener} assumes that {@link Object} class is
 * specified. In other words, the listener will be able to listen to event at any classes.
 * </p>
 * 
 * @param <T> A class type that you want to observe.
 * @see I#load(java.nio.file.Path)
 * @see I#unload(java.nio.file.Path)
 * @version 2008/12/05 13:46:19
 */
public interface ClassListener<T> {

    /**
     * <p>
     * This method is invoked whenever Sinobu loads a specified class.
     * </p>
     * 
     * @param clazz A loaded class. <code>null</code> is never passed.
     */
    void load(Class<T> clazz);

    /**
     * <p>
     * This method is invoked whenever Sinobu unloads a specified class.
     * </p>
     * 
     * @param clazz An unloaded class. <code>null</code> is never passed.
     */
    void unload(Class<T> clazz);
}
