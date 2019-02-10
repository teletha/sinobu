/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import kiss.sample.bean.invalid.OnlyGetter;
import kiss.sample.bean.invalid.OnlySetter;
import kiss.sample.bean.invalid.PackagePrivateAccessor;


/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: InheritanceBean.java,v 1.0 2006/12/21 17:57:44 Teletha Exp $
 */
public interface InheritanceBean extends OnlyGetter, OnlySetter, PackagePrivateAccessor {
}
