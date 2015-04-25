/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean.invalid;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: OnlySetter.java,v 1.0 2006/12/21 17:34:25 Teletha Exp $
 */
public interface OnlySetter {

    /**
     * Operation.
     */
    void setString(String value);

    /**
     * Operation.
     */
    void setInt(int value);

    /**
     * Operation.
     */
    void setMyself(OnlySetter value);
}
