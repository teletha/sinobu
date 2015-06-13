/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>
 * This lifestyle guarantees that only one instance of the specific class exists in Sinobu and all
 * its properties are paersisted automatically as user configuration file.
 * </p>
 * <p>
 * When the instance is initialized, Sinobu restores all properties form the persisted user
 * configuration file. When any instance's property is changed, Sinobu automatically stores it to
 * the user configuration file.
 * </p>
 * 
 * @param <M> A {@link Manageable} class.
 * @see Prototype
 * @see Singleton
 * @see ThreadSpecific
 * @version 2012/10/20 16:46:09
 */
public class Preference<M> extends Singleton<M> implements Runnable {

    /** The automatic saving location. */
    protected final Path path;

    /**
     * Create Preference instance.
     * 
     * @param modelClass A target class.
     */
    protected Preference(Class<M> modelClass) {
        super(modelClass);

        this.path = I.$working.resolve("preferences").resolve(modelClass.getName().concat(".xml"));

        try {
            if (Files.exists(path) && Files.size(path) != 0) {
                I.read(path, instance);
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        I.write(instance);
    }
}
