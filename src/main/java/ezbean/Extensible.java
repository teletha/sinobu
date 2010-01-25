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
package ezbean;

/**
 * <p>
 * This is a marker interface for Extension Point of your application.
 * </p>
 * <p>
 * All Extension Points are recognized automatically by Ezbean if you use
 * {@link I#load(java.io.File)} and {@link I#unload(java.io.File)} methods properly. And an
 * Extension Point will provide some Extensions.
 * </p>
 * <h2 id="ExtensionPoint">What is Extension Point?</h2>
 * <p>
 * We give a definition of <em>Extension Point</em> like the following.
 * </p>
 * <ul>
 * <li>It implements {@link Extensible} interface.</li>
 * <li>It has {@link Extensible} interface in not ancestor but parent.</li>
 * </ul>
 * 
 * <pre>
 * interface ThisIsExtensionPoint extends Extensible {
 * }
 * 
 * interface ThisIsNotExtensionPoint implements ThisIsExtensionPoint {
 * }
 * 
 * class ThisIsAlsoExtensionPoint implements Extensible {
 *     // At once Extension Point and Extension.
 * }
 * </pre>
 * 
 * <h2 id="Extension">What is Extension?</h2>
 * <p>
 * We give a definition of <em>Extension</em> like the following.
 * </p>
 * <ul>
 * <li>It implements any Extension Point or is Extension Point itself.</li>
 * <li>It must be concrete class and has a suitable constructor for Ezbean (see also
 * {@link I#make(Class)} method).</li>
 * </ul>
 * 
 * <pre>
 * class ThisIsExtension implements Extensible {
 *     // At once Extension Point and Extension.
 * }
 * 
 * class ThisIsAlsoExtension extends ThisIsExtension {
 *     // But not Extension Point.
 * }
 * 
 * class ThisIsNotExtension extends ThisIsExtension {
 * 
 *     public ThisIsNotExtension(NotInjectable object) {
 *         // invalid constructor
 *     }
 * }
 * </pre>
 * 
 * <h2 id="ExtensionKey">What is Extension Key?</h2>
 * <p>
 * You can provide <em>Extension Key</em> for each Extensions by using parameter. The key makes easy
 * finding an Extension you need (see also {@link I#find(Class, Class)}).
 * </p>
 * 
 * <pre>
 * interface ExtensionPointWithKey&lt;K&gt; extends Extensible {
 * }
 * 
 * class ExtensionWithKey implements ExtensionPointWithKey&lt;String&gt; {
 *     // Associate this Extension with String class.
 * }
 * 
 * class ExtensionWithAnotherKey implements ExtensionPointWithKey&lt;List&gt; {
 *     // Associate this Extension with List interface.
 * }
 * </pre>
 * 
 * @see I#find(Class)
 * @see I#find(Class, Class)
 * @version 2009/12/31 3:08:59
 */
public interface Extensible {
}
