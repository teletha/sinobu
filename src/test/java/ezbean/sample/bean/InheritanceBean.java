/**
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.sample.bean;

import ezbean.sample.bean.invalid.OnlyGetter;
import ezbean.sample.bean.invalid.OnlySetter;
import ezbean.sample.bean.invalid.PackagePrivateAccessor;


/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: InheritanceBean.java,v 1.0 2006/12/21 17:57:44 Teletha Exp $
 */
public interface InheritanceBean extends OnlyGetter, OnlySetter, PackagePrivateAccessor {
}
