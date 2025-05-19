package kiss;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class HolderGenerator {

    /**
     * VarHandleを保持する静的クラスを動的に生成します（privateフィールド対応）
     */
    public static Object generateVarHandleClass(Class<?> targetClass, String propertyName, Class<?> propertyType) throws Throwable {
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

        // Hidden Classとして定義（NestMateにすることでパッケージ制限回避）
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> generatedClass = lookup.defineHiddenClass(classBytes, true, MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
        return generatedClass.getConstructor().newInstance();
    }

    // 使用例
    public static void main(String[] args) throws Throwable {
        Object handle = generateVarHandleClass(Person.class, "name", String.class);

        Person person = new Person();
        person.name = "Takina";

        String value = ((Function<Person, String>) handle).apply(person);
        System.out.println("Name: " + value);

        ((BiConsumer<Person, String>) handle).accept(person, "Chisato");
        System.out.println("Updated: " + person.name);
    }

    public static class Person {
        private String name;
    }
}
