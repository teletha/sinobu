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

import java.io.Serializable;
import java.lang.reflect.Executable;

/**
 * @version 2017/02/02 12:12:04
 */
public interface EnhancedLambda<V> extends Serializable {

    /**
     * <p>
     * Retrieve the referenced {@link Executable}.
     * </p>
     * 
     * @return An {@link Executable} reference.
     */
    default Executable reference() {
        return I.lambda(this);
    }

    /**
     * <p>
     * Compute the specified parameter name.
     * </p>
     * 
     * @param index A parameter index.
     * @return A name of parameter.
     */
    default String parameterName(int index) {
        return reference().getParameters()[index].getName();
    }
}
