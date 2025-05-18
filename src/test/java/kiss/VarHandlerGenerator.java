/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;

import kiss.HolderGenerator.Person;
import net.bytebuddy.jar.asm.Type;

public class VarHandlerGenerator {

    public static void main(String... xxx) {
        Class<?> targetClass = Person.class;
        String propertyName = "name";
        Class<?> propertyType = String.class;

        String modelTypeName = Type.getInternalName(targetClass);
        String propertyTypeName = Type.getInternalName(propertyType);

        // クラス名とフィールドの型情報
        ClassDesc thisClass = ClassDesc.of(targetClass.getName() + "$$" + propertyName);
        ClassDesc targetClassDesc = ClassDesc.of(targetClass.getName());
        ClassDesc propertyTypeDesc = ClassDesc.of(propertyType.getName());
        ClassDesc varHandleDesc = ClassDesc.of("java.lang.invoke.VarHandle");
        ClassDesc lookupDesc = ClassDesc.of("java.lang.invoke.MethodHandles$Lookup");
        ClassDesc methodHandlesDesc = ClassDesc.of("java.lang.invoke.MethodHandles");

        // クラスファイル生成
        byte[] classBytes = ClassFile.of().build(thisClass, classBuilder -> {
            classBuilder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL)
                    .withInterfaceSymbols(ClassDesc.of("java.util.function.Function"), ClassDesc.of("java.util.function.BiConsumer"))
                    .withField("h", varHandleDesc, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL)

                    // <clinit> : privateフィールドに対応した VarHandle を初期化
                    .withMethodBody("<clinit>", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_STATIC, code -> {
                        code.ldc(targetClassDesc); // target class
                        code.invokestatic(methodHandlesDesc, "lookup", MethodTypeDesc.of(lookupDesc)); // MethodHandles.lookup()
                        code.invokestatic(methodHandlesDesc, "privateLookupIn", MethodTypeDesc
                                .of(lookupDesc, ConstantDescs.CD_Class, lookupDesc)); // privateLookupIn(targetClass,
                                                                                      // lookup)

                        code.dup(); // lookup for findVarHandle

                        code.ldc(targetClassDesc); // TargetClass.class
                        code.ldc(propertyName); // property name
                        code.ldc(propertyTypeDesc); // PropertyType.class

                        code.invokevirtual(lookupDesc, "findVarHandle", MethodTypeDesc
                                .of(varHandleDesc, ConstantDescs.CD_Class, ConstantDescs.CD_String, ConstantDescs.CD_Class));

                        code.putstatic(thisClass, "h", varHandleDesc);
                        code.return_();
                    })

                    // コンストラクタ
                    .withMethodBody("<init>", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC, code -> {
                        code.aload(0);
                        code.invokespecial(ConstantDescs.CD_Object, "<init>", MethodTypeDesc.of(ConstantDescs.CD_void));
                        code.return_();
                    })

                    // BiConsumer.accept(Object, Object) : handle.set(t, u)
                    .withMethodBody("accept", MethodTypeDesc
                            .of(ConstantDescs.CD_void, ConstantDescs.CD_Object, ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
                                code.getstatic(thisClass, "h", varHandleDesc);
                                code.aload(1);
                                code.aload(2);
                                code.invokevirtual(varHandleDesc, "set", MethodTypeDesc
                                        .of(ConstantDescs.CD_void, ConstantDescs.CD_Object, ConstantDescs.CD_Object));
                                code.return_();
                            })

                    // Function.apply(Object) : return handle.get(t)
                    .withMethodBody("apply", MethodTypeDesc
                            .of(ConstantDescs.CD_Object, ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
                                code.getstatic(thisClass, "h", varHandleDesc);
                                code.aload(1);
                                code.invokevirtual(varHandleDesc, "get", MethodTypeDesc
                                        .of(ConstantDescs.CD_Object, ConstantDescs.CD_Object));
                                code.areturn();
                            });
        });

        String bytes = literalize(classBytes).replaceAll(literalize(propertyName), "U")
                .replaceAll(literalize(propertyTypeName), "I")
                .replaceAll(literalize(modelTypeName), "S");

        System.out.println('"' + bytes + '"');
    }

    private static String literalize(String value) {
        return literalize(value.getBytes());
    }

    private static String literalize(byte[] bytes) {
        String value = Arrays.toString(bytes).replaceAll(" ", "");
        return value.substring(1, value.length() - 1);
    }
}
