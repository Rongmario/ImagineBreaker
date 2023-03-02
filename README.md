<img style="float: right;" src="https://i.imgur.com/rimNwts.png" width="40%" height="40%">

# Imagine Breaker [幻想殺し]


### *I will destroy that illusion of yours.*

Supports:

1. Breaking down the Java Platform Module System (JPMS) to allow Reflection into any base or requested modules
2. Removal of Reflection Filters, allows retrieval of any fields/methods from classes that were previously blocked:

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
   - `sun.misc.Unsafe`
   
All of the above is achieved with 3 different ways:

1. Reflection
2. Unsafe
3. JNI

Namesake: [Toaru Majutsu no Index](https://en.wikipedia.org/wiki/A_Certain_Magical_Index)
