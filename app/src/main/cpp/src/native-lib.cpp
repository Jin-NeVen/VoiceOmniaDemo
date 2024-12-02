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
    //modelは：20240221_model_se.bin
    const char* jpath = env->GetStringUTFChars(filePath, nullptr);
    std::string se_model_path = jpath;
    se_model.reset(new SEModel);
    se_model->load(se_model_path);
    //common env variable
    //frame related
    //default 16000.0
    //44100, 11025, 22050,
    se_model->setEnv("sample-frequency", (float)16000.0);
    // default 25
    se_model->setEnv("frame-length", (float)40.0);
    se_model->setEnv("frame-shift", (float)20.0);
//    se_model->setEnv("dither", 1.0);
//    se_model->setEnv("preemphasis-coefficient", 0.97);
//    se_model->setEnv("remove-dc-offset", true);
//    // hamming, hanning, povey, rectangular, sine, blackmann
//    se_model->setEnv("window-type", "povey");
//    se_model->setEnv("round-to-power-of-two", true);
//    se_model->setEnv("blackman-coeff", 0.42);
//    se_model->setEnv("snip-edges", true);
//    se_model->setEnv("allow-downsample", false);
//    se_model->setEnv("allow-upsample", false);
//    se_model->setEnv("max-feature-vectors", -1);

    // mel env variable
    // fbank env variable
    // pitch env variable
    // compute opt env variable
    // feature-extractor env variable


    //se env variable
//    se_model->setEnv("file-type", "SE");
//    se_model->setEnv("file-version", "");
//    se_model->setEnv("magic-number-bin", "senb");
//    se_model->setEnv("magic-number-xml", "senx");
//    se_model->setEnv("is-switching-se", false);
    // overlap: readonly
//    int overlap = se_model->getEnv<int>("overlap");
//    se_model->setEnv("overlap",(int)160);
//    se_model->setEnv("offline", true);
//    se_model->setEnv("chunk-size", (int)80);
//    se_model->setEnv("reset-context", 0);
//    se_model->setEnv("num-channels", 1);
//    se_model->setEnv("inverse-feature-extractor-type", "none");
    // 0.0~1.0
    se_model->setEnv("enhancement-strength", (float)0.0);

//    se_model->setEnv("switching-strength", 1.0);


    env->ReleaseStringUTFChars(filePath, jpath);
    return;
}