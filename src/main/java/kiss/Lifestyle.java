/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.Supplier;

/**
 * <p>
 * Lifestyle manages the instance in the specific context. Sinobu provides four commonly used
 * lifestyles ({@link Prototype}, {@link Singleton}, {@link ThreadSpecific} and {@link Preference}).
 * </p>
 * <p>
 * There are two ways to specify {@link Lifestyle} for the class.
 * </p>
 * <p>
 * The one is {@link Manageable} annotation. This way is useful if the target class is under your
 * control. If the lifestyle is not specified, Sinobu uses {@link Prototype} lifestyle as default.
 * The following is example.
 * </p>
 * 
 * <pre>
 * &#064;Manageable(lifestyle = Singleton.class)
 * public class TargetClass {
 * }
 * </pre>
 * <p>
 * The other is defining custom {@link Lifestyle}. Sinobu recognizes it automatically if your custom
 * lifestyle class is loaded or unloaded by {@link I#load(java.nio.file.Path)} and
 * {@link I#unload(java.nio.file.Path)} methods. The following is example.
 * </p>
 * 
 * <pre>
 * public class CustomLifestyle implements Lifestyle&lt;ClassNotUnderYourControl&gt; {
 * 
 *     public ClassNotUnderYourControl get() {
 *         return new ClassNotUnderYourControl();
 *     }
 * }
 * </pre>
 * 
 * @param <M> A {@link Manageable} class.
 * @see Prototype
 * @see Singleton
 * @see ThreadSpecific
 * @see Preference
 * @see Manageable#lifestyle()
 * @version 2014/02/03 12:25:46
 */
public interface Lifestyle<M> extends Supplier<M>, Extensible {
}
