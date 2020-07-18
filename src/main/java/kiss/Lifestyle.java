/*
 * Copyright (C) 2020 Nameless Production Committee
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
 * Lifestyle manages the instance in the specific context. Sinobu provides two commonly used
 * lifestyles ({@link I#prototype(Class)} and {@link Singleton}.
 * </p>
 * <p>
 * There are two ways to specify {@link Lifestyle} for the class.
 * </p>
 * <p>
 * The one is {@link Managed} annotation. This way is useful if the target class is under your
 * control. If the lifestyle is not specified, Sinobu uses {@link I#prototype(Class)} lifestyle as
 * default. The following is example.
 * </p>
 * <pre>
 * &#064;Manageable(lifestyle = Singleton.class)
 * public class TargetClass {
 * }
 * </pre>
 * <p>
 * The other is defining custom {@link Lifestyle}. Sinobu recognizes it automatically if your custom
 * lifestyle class is loaded or unloaded by {@link I#load(Class)} and
 * {@link Disposable#dispose()}methods. The following is example.
 * </p>
 * <pre>
 * public class CustomLifestyle implements Lifestyle&lt;ClassNotUnderYourControl&gt; {
 * 
 *     public ClassNotUnderYourControl call() {
 *         return new ClassNotUnderYourControl();
 *     }
 * }
 * </pre>
 * 
 * @param <M> A {@link Managed} class.
 * @see I#prototype(Class)
 * @see Singleton
 * @see Managed#value()
 */
public interface Lifestyle<M> extends WiseSupplier<M>, Extensible {
}