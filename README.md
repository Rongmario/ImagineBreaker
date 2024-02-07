<img style="float: right;" src="https://i.imgur.com/rimNwts.png" width="40%" height="40%">

# Imagine Breaker [幻想殺し]


### *I will destroy that illusion of yours.* 

<sub>(Disclaimer: use with care, I'll not be held responsible for your JVM burning up in flames)</sub>

With the love of JNI, this supports:

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

Namesake: [Toaru Majutsu no Index](https://en.wikipedia.org/wiki/A_Certain_Magical_Index)
