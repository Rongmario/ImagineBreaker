# Imagine Breaker [幻想殺し]

<img src="docs/images/he_punches_women.png" align="right" width="40%">

### *I will destroy that illusion of yours.*

With the love of ~~JNI~~ Unsafe & MethodHandles/VarHandles API from Java 9-26, this provides:

1. Public-facing `sun.misc.Unsafe` API instance & maximum privilege **(TRUSTED)** `MethodHandles$Lookup` instance.
2. Breaking down the Java Platform Module System (JPMS) to open up any specified module
3. Disguise as any specified Module, in order to fool `@CallerSensitive` methods
4. Removal of Reflection Filters, allows retrieval of any fields from classes that were previously blocked:

   - `jdk.internal.reflect.ConstantPool`
   - `jdk.internal.reflect.Reflection`
   - `jdk.internal.reflect.UnsafeStaticFieldAccessorImpl`
   - `java.lang.Class`
   - `java.lang.ClassLoader`
   - `java.lang.reflect.Constructor`
   - `java.lang.reflect.Field`
   - `java.lang.reflect.Method`
   - `java.lang.Module`
   - `java.lang.System`
   - `java.lang.invoke.MethodHandles$Lookup`
   - `java.lang.reflect.AccessibleObject`

5. On JDK 22+, `enableNativeAccess()` for unnamed modules (JEP 472) without `--enable-native-access`
6. On JDK 26+, `enableFinalFieldMutation()` for unnamed modules and `java.base` (JEP 500) without `--enable-final-field-mutation`
7. Filter-blind declared members (`getDeclaredFields0`/methods/constructors/classes)
8. Unsafe field get/set (including static `final`), trusted invoke/construct/`allocateInstance`
9. `defineClass`/hidden (or anonymous) classes, system classpath `addURL`
10. Enum constant injection, `jdk.internal.misc.Unsafe`, runtime `Instrumentation` (if the VM allows dynamic agents)

## Usage
`build.gradle`:
```groovy
repositories {
    maven {
        url 'https://maven.cleanroommc.com'
    }
}

dependencies {
    implementation 'zone.rong:imaginebreaker:3.1'
}
```

```java
ImagineBreaker ib = Index.get();
Lookup lookup = ib.trustedLookup();
Unsafe unsafe = ib.unsafe();
ib.enableNativeAccess();
ib.enableFinalFieldMutation();
ib.openBootModules();
ib.clearFieldFilters();
Field implLookup = ib.declaredField(Lookup.class, "IMPL_LOOKUP");
ib.set(box, ib.declaredField(Box.class, "x"), 99);
ib.disguiseAsModule(Caller.class, Object.class, () -> {
    // @CallerSensitive calls see java.base
});
```

Namesake: [Toaru Majutsu no Index](https://en.wikipedia.org/wiki/A_Certain_Magical_Index)
