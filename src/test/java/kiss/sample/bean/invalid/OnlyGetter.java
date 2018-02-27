/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.invalid;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: OnlyGetter.java,v 1.0 2006/12/21 17:32:43 Teletha Exp $
 */
public interface OnlyGetter {

    /**
     * Getter.
     * 
     * @return A value.
     */
    String getString();

    /**
     * Getter.
     * 
     * @return A value.
     */
    int getInt();

    /**
     * Getter.
     * 
     * @return A value.
     */
    OnlyGetter getMyself();
}
