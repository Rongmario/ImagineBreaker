<img style="float: right;" src="https://i.imgur.com/rimNwts.png" width="40%" height="40%">

# Imagine Breaker [幻想殺し]

<br>

#### *I will destroy that illusion of yours.*

<br>
<br>
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


Coming in 2.0: Native way to break down JPMS, this way will bypass the need for having to include JVM Arguments.

Namesake: [Toaru Majutsu no Index](https://en.wikipedia.org/wiki/A_Certain_Magical_Index)