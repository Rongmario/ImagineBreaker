#include "zone_rong_imaginebreaker_NativeImagineBreaker.h"
#include <iostream>

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    openBaseModules
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_openBaseModules(JNIEnv* env, jclass thisClass) {
    std::cout << "[ImagineBreaker] Opening modules natively..." << std::endl;

    jclass imagineBreakerClass = env->FindClass("zone/rong/imaginebreaker/ImagineBreaker");
    jmethodID findBootModule = env->GetStaticMethodID(imagineBreakerClass, "unsafeFindBootModule", "(Ljava/lang/String;)Ljava/lang/Module;");
    jobject javaBaseModule = env->CallStaticObjectMethod(imagineBreakerClass, findBootModule, env->NewStringUTF("java.base"));

    jclass moduleClass = env->FindClass("java/lang/Module");
    jfieldID everyoneModuleField = env->GetStaticFieldID(moduleClass, "EVERYONE_MODULE", "Ljava/lang/Module;");
    jobject everyoneModule = env->GetStaticObjectField(moduleClass, everyoneModuleField);

    jclass reflectionDataClass = env->FindClass("java/lang/Module$ReflectionData");
    jfieldID exportsField = env->GetStaticFieldID(reflectionDataClass, "exports", "Ljava/lang/WeakPairMap;");
    jobject exports = env->GetStaticObjectField(reflectionDataClass, exportsField);

    jmethodID internal$openModules = env->GetStaticMethodID(thisClass, "internal$openModules", "(Ljava/lang/Module;Ljava/lang/Module;Ljava/lang/Object;)V");
    env->CallStaticVoidMethod(thisClass, internal$openModules, javaBaseModule, everyoneModule, exports);
}

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    addExportsToAll0
 * Signature: (Ljava/lang/Module;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_addExportsToAll0(JNIEnv* env, jclass thisClass, jobject module, jstring exports) {
    jclass moduleClass = env->GetObjectClass(module);
    jmethodID addExportsToAll0 = env->GetStaticMethodID(moduleClass, "addExportsToAll0", "(Ljava/lang/Module;Ljava/lang/String;)V");
    env->CallStaticVoidMethod(moduleClass, addExportsToAll0, module, exports);
}

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    weakPairMap_computeIfAbsent
 * Signature: (Ljava/lang/Object;Ljava/lang/Module;Ljava/lang/Module;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_weakPairMap_1computeIfAbsent(JNIEnv* env, jclass thisClass, jobject exports, jobject javaBaseModule,
jobject everyoneModule, jobject computeIfAbsentFunction) {
    jclass weakPairMapClass = env->FindClass("java/lang/WeakPairMap");
    jmethodID computeIfAbsent = env->GetMethodID(weakPairMapClass, "computeIfAbsent", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;");
    return env->CallObjectMethod(exports, computeIfAbsent, javaBaseModule, everyoneModule, computeIfAbsentFunction);
}

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    removeFieldReflectionFilters
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_removeFieldReflectionFilters(JNIEnv *, jclass) {
   std::cout << "[ImagineBreaker] Removing field reflection filters natively..." << std::endl;
   jclass reflectionClass = env->FindClass("jdk/internal/reflect/Reflection");
   jfieldID fieldFilterMapField = env->GetStaticFieldID(reflectionClass, "fieldFilterMap", "Ljava/util/Map;");
   jobject fieldFilterMap = env->GetStaticObjectField(reflectionClass, fieldFilterMapField);
   env->SetStaticObjectField(reflectionClass, fieldFilterMapField, NULL);
   return fieldFilterMap;
}

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    removeMethodReflectionFilters
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_removeMethodReflectionFilters(JNIEnv *, jclass) {
    std::cout << "[ImagineBreaker] Removing method reflection filters natively..." << std::endl;
    jclass reflectionClass = env->FindClass("jdk/internal/reflect/Reflection");
    jfieldID methodFilterMapField = env->GetStaticFieldID(reflectionClass, "methodFilterMap", "Ljava/util/Map;");
    jobject methodFilterMap = env->GetStaticObjectField(reflectionClass, methodFilterMapField);
    env->SetStaticObjectField(reflectionClass, methodFilterMapField, NULL);
    return methodFilterMap;
}

/*
 * Class:     zone_rong_imaginebreaker_NativeImagineBreaker
 * Method:    clearReflectionData
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_zone_rong_imaginebreaker_NativeImagineBreaker_clearReflectionData(JNIEnv *, jclass, jclass clazz) {
    jfieldID reflectionDataField = env->GetFieldID(clazz, "reflectionData", "Ljava/lang/ref/SoftReference;");
    env->SetObjectField(clazz, reflectionDataField, NULL);
}

