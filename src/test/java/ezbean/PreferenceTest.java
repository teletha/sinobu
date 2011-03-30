/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;
import ezunit.PrivateModule;

/**
 * @version 2011/03/30 9:46:04
 */
public class PreferenceTest {

    @Rule
    public static final PrivateModule module = new PrivateModule();

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Rule
    public static final EzbeanSetting config = new EzbeanSetting(room.root);

    @Test
    public void preference() throws Exception {
        UserPreference preference = I.make(UserPreference.class);
        preference.setId(1);
        preference.setName("Chall Dunois");
        assert preference.id == 1;
        assert preference.name.equals("Chall Dunois");

        // save
        preference.store();

        // change
        preference.setId(2);
        preference.setName("Cecilia Alcott");
        assert preference.id == 2;
        assert preference.name.equals("Cecilia Alcott");

        // load
        preference.restore();
        assert preference.id == 1;
        assert preference.name.equals("Chall Dunois");
    }

    @Test
    public void reset() throws Exception {
        UserPreference preference = I.make(UserPreference.class);
        preference.setId(1);
        preference.setName("Chall Dunois");
        assert preference.id == 1;
        assert preference.name.equals("Chall Dunois");

        // reset
        preference.reset();
        assert preference.id == 0;
        assert preference.name == null;
    }

    /**
     * @version 2011/03/30 9:47:37
     */
    protected static class UserPreference extends Preference {

        /** The user id. */
        private long id;

        /** The use name. */
        private String name;

        /**
         * Get the id property of this {@link PreferenceTest.UserPreference}.
         * 
         * @return The id property.
         */
        public long getId() {
            return id;
        }

        /**
         * Set the id property of this {@link PreferenceTest.UserPreference}.
         * 
         * @param id The id value to set.
         */
        public void setId(long id) {
            this.id = id;
        }

        /**
         * Get the name property of this {@link PreferenceTest.UserPreference}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link PreferenceTest.UserPreference}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * @version 2011/03/30 13:00:31
     */
    protected static class ISPreference extends Preference {

        /** The name of IS. */
        private String name;

        /**
         * Get the name property of this {@link PreferenceTest.ISPreference}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link PreferenceTest.ISPreference}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
