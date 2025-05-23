package kiss;

import static java.lang.classfile.ClassFile.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

public class HolderGenerator2 {

    public static Object bypass(Class model, String name, Class p) throws Throwable {
        ClassDesc thiz = ClassDesc.of(model.getName().concat("$$").concat(name));
        ClassDesc m = type(model);
        ClassDesc mh = type(MethodHandle.class);

        byte[] bytes = ClassFile.of().build(thiz, classBuilder -> {
            classBuilder.withFlags(ACC_PUBLIC | ClassFile.ACC_FINAL)
                    .withInterfaceSymbols(type(WiseFunction.class), type(WiseBiFunction.class))
                    .withField("getter", mh, ACC_PUBLIC | ACC_STATIC | ACC_FINAL)
                    .withField("setter", mh, ACC_PUBLIC | ACC_STATIC | ACC_FINAL)

                    // static initializer
                    .withMethodBody("<clinit>", MethodTypeDesc.of(CD_void), ACC_STATIC, code -> {
                        code.ldc(m)
                                .invokestatic(CD_MethodHandles, "lookup", MethodTypeDesc.of(type(Lookup.class)))
                                .invokestatic(CD_MethodHandles, "privateLookupIn", MethodTypeDesc
                                        .of(type(Lookup.class), CD_Class, type(Lookup.class)))
                                .dup()

                                // exact setter
                                .ldc(m)
                                .ldc(name)
                                .ldc(type(p))
                                .invokevirtual(type(Lookup.class), "findSetter", MethodTypeDesc.of(mh, CD_Class, CD_String, CD_Class))
                                .putstatic(thiz, "setter", mh)

                                // exact getter
                                .ldc(m)
                                .ldc(name)
                                .ldc(type(p))
                                .invokevirtual(type(Lookup.class), "findGetter", MethodTypeDesc.of(mh, CD_Class, CD_String, CD_Class))
                                .putstatic(thiz, "getter", mh)
                                .return_();
                    })

                    // constructor
                    .withMethodBody("<init>", MethodTypeDesc.of(CD_void), ACC_PUBLIC, code -> {
                        code.aload(0).invokespecial(CD_Object, "<init>", MethodTypeDesc.of(CD_void)).return_();
                    })

                    // implement WiseFunction
                    .withMethodBody("APPLY", MethodTypeDesc.of(CD_Object, CD_Object), ACC_PUBLIC, code -> {
                        code.getstatic(thiz, "getter", mh)
                                .aload(1)
                                .checkcast(m)
                                .invokevirtual(mh, "invokeExact", MethodTypeDesc.of(type(p), m));
                        if (p.isPrimitive()) {
                            code.invokestatic(type(I.wrap(p)), "valueOf", MethodTypeDesc.of(type(I.wrap(p)), type(p)));
                        }
                        code.areturn();
                    })

                    // implement WiseBiFunction
                    .withMethodBody("APPLY", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object), ACC_PUBLIC, code -> {
                        code.getstatic(thiz, "setter", mh).aload(1).checkcast(m).aload(2).checkcast(type(I.wrap(p)));
                        if (p.isPrimitive()) {
                            code.invokevirtual(type(I.wrap(p)), p.getName().concat("Value"), MethodTypeDesc.of(type(p)));
                        }
                        code.invokevirtual(mh, "invokeExact", MethodTypeDesc.of(CD_void, m, type(p))).aload(1).areturn();
                    });
        });

        return MethodHandles.privateLookupIn(model, MethodHandles.lookup()).defineClass(bytes).getConstructor().newInstance();
    }

    public static Object bypassVar(Class model, String name, Class p) throws Throwable {
        ClassDesc thiz = ClassDesc.of(model.getName().concat("$$").concat(name));
        ClassDesc m = type(model);
        ClassDesc vh = type(VarHandle.class);

        byte[] bytes = ClassFile.of().build(thiz, classBuilder -> {
            classBuilder.withFlags(ACC_PUBLIC | ClassFile.ACC_FINAL)
                    .withInterfaceSymbols(type(WiseFunction.class), type(WiseBiFunction.class))
                    .withField("h", vh, ACC_PUBLIC | ACC_STATIC | ACC_FINAL)

                    // static initializer
                    .withMethodBody("<clinit>", MethodTypeDesc.of(CD_void), ACC_STATIC, code -> {
                        code.ldc(m)
                                .invokestatic(CD_MethodHandles, "lookup", MethodTypeDesc.of(type(Lookup.class)))
                                .invokestatic(CD_MethodHandles, "privateLookupIn", MethodTypeDesc
                                        .of(type(Lookup.class), CD_Class, type(Lookup.class)))
                                .ldc(m)
                                .ldc(name)
                                .ldc(type(p))
                                .invokevirtual(type(Lookup.class), "findVarHandle", MethodTypeDesc.of(vh, CD_Class, CD_String, CD_Class))
                                .putstatic(thiz, "h", vh);
                        code.return_();
                    })

                    // constructor
                    .withMethodBody("<init>", MethodTypeDesc.of(CD_void), ACC_PUBLIC, code -> {
                        code.aload(0).invokespecial(CD_Object, "<init>", MethodTypeDesc.of(CD_void)).return_();
                    })

                    // implement Function
                    .withMethodBody("APPLY", MethodTypeDesc.of(CD_Object, CD_Object), ACC_PUBLIC, code -> {
                        code.getstatic(thiz, "h", vh);
                        code.aload(1).checkcast(m);
                        code.invokevirtual(vh, "get", MethodTypeDesc.of(type(p), m));
                        if (p.isPrimitive()) {
                            code.invokestatic(type(I.wrap(p)), "valueOf", MethodTypeDesc.of(type(I.wrap(p)), type(p)));
                        }
                        code.areturn();
                    })

                    // implement WiseBiFunction
                    .withMethodBody("APPLY", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object), ACC_PUBLIC, code -> {
                        code.getstatic(thiz, "h", vh).aload(1).checkcast(m).aload(2).checkcast(type(I.wrap(p)));
                        if (p.isPrimitive()) {
                            code.invokevirtual(type(I.wrap(p)), p.getName().concat("Value"), MethodTypeDesc.of(type(p)));
                        }
                        code.invokevirtual(vh, "set", MethodTypeDesc.of(CD_void, m, type(p))).aload(1).areturn();
                    });
        });

        return MethodHandles.privateLookupIn(model, MethodHandles.lookup())
                .defineHiddenClass(bytes, true)
                .lookupClass()
                .getConstructor()
                .newInstance();
    }

    private static ClassDesc type(Class type) {
        return ClassDesc.ofDescriptor(type.descriptorString());
    }
}
