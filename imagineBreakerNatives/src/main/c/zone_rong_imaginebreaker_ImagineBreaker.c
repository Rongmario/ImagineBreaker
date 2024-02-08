#include <jni.h>

jclass moduleClass;
jobject everyoneModule;
jmethodID addOpens;

jclass reflectionClass;
jfieldID fieldFilterMap;
jfieldID methodFilterMap;

jclass classClass;
jfieldID reflectionData;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_1) != JNI_OK) {
        return -1;
    }

    moduleClass = (*env)->FindClass(env, "java/lang/Module");
    everyoneModule = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(env, moduleClass, (*env)->GetStaticFieldID(env, moduleClass, "EVERYONE_MODULE", "Ljava/lang/Module;")));
    addOpens = (*env)->GetMethodID(env, moduleClass, "implAddExportsOrOpens", "(Ljava/lang/String;Ljava/lang/Module;ZZ)V");

    reflectionClass = (*env)->FindClass(env, "jdk/internal/reflect/Reflection");
    fieldFilterMap = (*env)->GetStaticFieldID(env, reflectionClass, "fieldFilterMap", "Ljava/util/Map;");
    methodFilterMap = (*env)->GetStaticFieldID(env, reflectionClass, "methodFilterMap", "Ljava/util/Map;");

    classClass = (*env)->FindClass(env, "java/lang/Class");
    reflectionData = (*env)->GetFieldID(env, classClass, "reflectionData", "Ljava/lang/ref/SoftReference;");

    (*env)->ExceptionCheck(env);
    (*env)->ExceptionClear(env);

    return JNI_VERSION_1_1;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    (*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_1);

    (*env)->DeleteGlobalRef(env, everyoneModule);
}

/*
 * Class:     zone_rong_imaginebreaker_ImagineBreaker
 * Method:    implAddOpensToModule
 * Signature: (Ljava/lang/Module;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_zone_rong_imaginebreaker_ImagineBreaker_implAddOpensToModule(JNIEnv* env, jclass _, jobject module, jstring package) {
    (*env)->CallVoidMethod(env, module, addOpens, package, everyoneModule, 1, 1);
}

/*
 * Class:     zone_rong_imaginebreaker_ImagineBreaker
 * Method:    implRemoveFieldReflectionFilters
 * Signature: ()Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_zone_rong_imaginebreaker_ImagineBreaker_implRemoveFieldReflectionFilters(JNIEnv* env, jclass _) {
    jobject map = (*env)->GetStaticObjectField(env, reflectionClass, fieldFilterMap);
    (*env)->SetStaticObjectField(env, reflectionClass, fieldFilterMap, NULL);
    return map;
}

/*
 * Class:     zone_rong_imaginebreaker_ImagineBreaker
 * Method:    implRemoveMethodReflectionFilters
 * Signature: ()Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_zone_rong_imaginebreaker_ImagineBreaker_implRemoveMethodReflectionFilters(JNIEnv* env, jclass _) {
    jobject map = (*env)->GetStaticObjectField(env, reflectionClass, methodFilterMap);
    (*env)->SetStaticObjectField(env, reflectionClass, methodFilterMap, NULL);
    return map;
}

/*
 * Class:     zone_rong_imaginebreaker_ImagineBreaker
 * Method:    implRemoveClassReflectionCacheData
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_zone_rong_imaginebreaker_ImagineBreaker_implRemoveClassReflectionCacheData(JNIEnv* env, jclass _, jclass clazz) {
    (*env)->SetObjectField(env, clazz, reflectionData, NULL);
}