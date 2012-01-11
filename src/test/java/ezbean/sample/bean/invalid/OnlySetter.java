/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.sample.bean.invalid;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: OnlySetter.java,v 1.0 2006/12/21 17:34:25 Teletha Exp $
 */
public interface OnlySetter {

    /**
     * Setter.
     * 
     * @param value
     */
    void setString(String value);

    /**
     * Setter.
     * 
     * @param value
     */
    void setInt(int value);

    /**
     * Setter.
     * 
     * @param value
     */
    void setMyself(OnlySetter value);
}
