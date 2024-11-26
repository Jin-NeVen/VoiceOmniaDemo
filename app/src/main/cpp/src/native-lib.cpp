#include <jni.h>
#include <string>
#include "se_model.hpp"
#include "global_se_model.hpp"

extern "C" JNIEXPORT jstring JNICALL
Java_com_ntt_skyway_demo_voiceomnia_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT void JNICALL
Java_com_ntt_skyway_demo_voiceomnia_MainActivity_nativeInitialize(
        JNIEnv* env,
        jobject /* this */,
        jstring filePath
        ) {
    SEModel::initialize();
    //TODO modelは：20240221_model_se.bin
    // pathはどうする？
    const char* jpath = env->GetStringUTFChars(filePath, nullptr);
    std::string se_model_path = jpath;
//    std::string se_model_name = "20240221_model_se.bin";
    se_model.reset(new SEModel);
    se_model->load(se_model_path);
    env->ReleaseStringUTFChars(filePath, jpath);
    return;
}