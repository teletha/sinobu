/*
 * Copyright (C) 2019 Nameless Production Committee
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
 * Lifestyle manages the instance in the specific context. Sinobu provides three commonly used
 * lifestyles ({@link Prototype}, {@link Singleton} and {@link ThreadSpecific}).
 * </p>
 * <p>
 * There are two ways to specify {@link Lifestyle} for the class.
 * </p>
 * <p>
 * The one is {@link Managed} annotation. This way is useful if the target class is under your
 * control. If the lifestyle is not specified, Sinobu uses {@link Prototype} lifestyle as default.
 * The following is example.
 * </p>
 * <pre>
 * &#064;Manageable(lifestyle = Singleton.class)
 * public class TargetClass {
 * }
 * </pre>
 * <p>
 * The other is defining custom {@link Lifestyle}. Sinobu recognizes it automatically if your custom
 * lifestyle class is loaded or unloaded by {@link I#loadE(java.nio.file.Path)} and
 * {@link I#unload(java.nio.file.Path)} methods. The following is example.
 * </p>
 * <pre>
 * public class CustomLifestyle implements Lifestyle&lt;ClassNotUnderYourControl&gt; {
 * 
 *     public ClassNotUnderYourControl get() {
 *         return new ClassNotUnderYourControl();
 *     }
 * }
 * </pre>
 * 
 * @param <M> A {@link Managed} class.
 * @see Prototype
 * @see Singleton
 * @see ThreadSpecific
 * @see Managed#value()
 * @version 2017/03/29 11:16:17
 */
public interface Lifestyle<M> extends WiseSupplier<M>, Extensible {
}
