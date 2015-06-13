/*
 * Copyright (C) 2015 Nameless Production Committee
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
 * @version $ Id: PackagePrivateAccessor.java,v 1.0 2006/12/21 17:50:57 Teletha Exp $
 */
public interface PackagePrivateAccessor {

    /**
     * Non accessor method.
     */
    void attack();

    /**
     * Non accessor method.
     */
    int getHealth(String name, int type, int version);

    /**
     * Non accessor method.
     */
    void setHealth(String name, int type, int version, int value);
}
