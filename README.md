<img style="float: right;" src="https://i.imgur.com/rimNwts.png" width="40%" height="40%">

# Imagine Breaker [幻想殺し]


### *I will destroy that illusion of yours.*

With the love of ~~JNI~~ Unsafe & MethodHandles/VarHandles API from Java 9 - 23, this provides:

1. Public-facing `sun.misc.Unsafe` API instance & maximum priviledge **(TRUSTED)** `MethodHandles$Lookup` instance.
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

## Usage
`build.gradle`:
```groovy
repositories {
    maven {
        url 'https://maven.cleanroommc.com'
    }
}

dependencies {
    implementation 'zone.rong:imaginebreaker:2.1'
}
```

Namesake: [Toaru Majutsu no Index](https://en.wikipedia.org/wiki/A_Certain_Magical_Index)
