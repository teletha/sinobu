/*
 * Copyright (C) 2010 Nameless Production Committee.
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

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: Address.java,v 1.0 2006/12/23 21:11:12 Teletha Exp $
 */
public class Address {

    private String country;

    private String city;

    /**
     * Get the city property of this {@link Address}.
     * 
     * @return The city prperty.
     */
    public String getCity() {
        return city;
    }

    /**
     * Set the city property of this {@link Address}.
     * 
     * @param city The city value to set.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the country property of this {@link Address}.
     * 
     * @return The country prperty.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Set the country property of this {@link Address}.
     * 
     * @param country The country value to set.
     */
    public void setCountry(String country) {
        this.country = country;
    }

}
