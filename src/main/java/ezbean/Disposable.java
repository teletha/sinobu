/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean;

/**
 * <p>
 * The Disposable interface is used when components need to deallocate and dispose resources prior
 * to their destruction.
 * </p>
 * 
 * @version 2008/06/10 12:20:54
 */
public interface Disposable {

    /**
     * <p>
     * The dispose operation is called at the end of a components lifecycle. Components use this
     * method to release and destroy any resources that the Component owns.
     * </p>
     */
    void dispose();
}
