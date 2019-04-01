/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.Supplier;

/**
 * 
 */
public interface Narrow<Assigned, Assigner, Preassigned, Preassigner> {
    /**
     * <p>
     * Apply head parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned assign(Assigner param) {
        return assign(Variable.of(param));
    }

    /**
     * <p>
     * Apply head parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned assign(Supplier<Assigner> param) {
        return I.make(this, Narrow.class, 0, args -> {
            return ((Flexible) this).invoke(I.array(args, param.get()));
        });
    }

    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Preassigned preassign(Preassigner param) {
        return preassign(Variable.of(param));
    }

    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Preassigned preassign(Supplier<Preassigner> param) {
        return I.make(this, Narrow.class, 0, args -> {
            return ((Flexible) this).invoke(I.array(new Object[] {param.get()}, args));
        });
    }

    // public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
    // WiseBiConsumer<String, String> base = (p, q) -> {
    // System.out.println("Consume " + p + " " + q);
    // };
    //
    // WiseConsumer<String> consumer = base.preassign("ok");
    // consumer.accept("kokok");
    // Variable<String> vari = Variable.of("def");
    // WiseRunnable run = consumer.preassign(vari);
    // vari.set("change");
    // run.run();
    // }
}
