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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
        try {
            MethodHandle get = MethodHandles.lookup().findVirtual(Supplier.class, "get", MethodType.genericMethodType(0));

            return I.make2(this, Narrow.class, (m, h) -> {
                System.out.println(h + "        " + get + "         " + m);
                h = MethodHandles.insertArguments(h, h.type().parameterCount() - 1, param);
                System.out.println(h);
                h = MethodHandles.filterArguments(h, h.type().parameterCount() - 1, get);

                return h;
            });
        } catch (NoSuchMethodException e) {
            throw I.quiet(e);
        } catch (IllegalAccessException e) {
            throw I.quiet(e);
        }
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
        return I.make2(this, Narrow.class, (m, h) -> MethodHandles.insertArguments(h, 0, param.get()));
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        WiseBiConsumer<String, String> base = (p, q) -> {
            System.out.println("Consume " + p + "  " + q);
        };

        WiseConsumer<String> consumer = base.assign("ok");
        consumer.accept("kokok");
        Variable<String> vari = Variable.of("def");
        WiseRunnable run = consumer.assign(vari);
        vari.set("change");
        run.run();
    }
}
