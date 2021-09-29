#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_sdt_maxkofler_homenet_1app_MainActivity_stringFromJNI(JNIEnv *env, jobject main){
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}