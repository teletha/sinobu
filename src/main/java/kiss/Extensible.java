/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * <p>
 * This is a marker interface for Extension Point of your application.
 * </p>
 * <p>
 * All Extension Points are recognized automatically by Sinobu if you use {@link I#load(Class)} or
 * {@link I#load(java.net.URL)} methods properly. And an Extension Point will provide some
 * Extensions.
 * </p>
 * <h2 id="ExtensionPoint">What is Extension Point?</h2>
 * <p>
 * We give a definition of <em>Extension Point</em> like the following.
 * </p>
 * <ul>
 * <li>It implements {@link Extensible} interface.</li>
 * <li>It has {@link Extensible} interface in not ancestor but parent.</li>
 * </ul>
 * {@snippet lang = java :
 * interface ThisIsExtensionPoint extends Extensible {
 * }
 * 
 * interface ThisIsNotExtensionPoint implements ThisIsExtensionPoint {
 * }
 * 
 * class ThisIsAlsoExtensionPoint implements Extensible {
 *     // This is both Extension Point and Extension.
 * }
 * }
 * <h2 id="Extension">What is Extension?</h2>
 * <p>
 * We give a definition of <em>Extension</em> like the following.
 * </p>
 * <ul>
 * <li>It implements any Extension Point or is Extension Point itself.</li>
 * <li>It must be concrete class and has a suitable constructor for Sinobu (see also
 * {@link I#make(Class)} method).</li>
 * </ul>
 * {@snippet lang = java :
 * class ThisIsExtension implements Extensible {
 *     // This is both Extension Point and Extension.
 * }
 * 
 * class ThisIsAlsoExtension extends ThisIsExtension {
 *     // But not Extension Point.
 * }
 * 
 * class ThisIsNotExtension extends ThisIsExtension {
 * 
 *     public ThisIsNotExtension(NotInjectable object) {
 *         // because of invalid constructor
 *     }
 * }
 * }
 * <h2 id="ExtensionKey">What is Extension Key?</h2>
 * <p>
 * You can provide <em>Extension Key</em> for each Extensions by using parameter. The key makes easy
 * finding an Extension you need (see also {@link I#find(Class, Class)}).
 * </p>
 * {@snippet lang = java :
 * interface ExtensionPointWithKey<K> extends Extensible {
 * }
 * 
 * class ExtensionWithKey implements ExtensionPointWithKey<String> {
 *     // Associate this Extension with String class.
 * }
 * 
 * class ExtensionWithAnotherKey implements ExtensionPointWithKey<List> {
 *     // Associate this Extension with List interface.
 * }
 * }
 * 
 * @see I#find(Class)
 * @see I#find(Class, Class)
 */
public interface Extensible {
}