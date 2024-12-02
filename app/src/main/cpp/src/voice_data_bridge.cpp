//
// Created by Liangjin Huang on 2024/11/25.
//
#include <jni.h>
#include <string>
#include "global_se_model.hpp"
#include "input_base.hpp"
#include <android/log.h>

class InputVoiceData : public Input<short> {
public:
    InputVoiceData(std::vector<short>&& data) : buffer(std::move(data)) {
        __android_log_print(ANDROID_LOG_INFO, "InputVoiceData", "data size(initialize): %d.", buffer.size());
    }

    // データをコピーして返す
    size_t data(std::vector<short>& dst, size_t require, size_t trancate) override {
        // truncate分読み飛ばす
        if(trancate){
            if(trancate <= buffer.size()) {
                buffer.erase(buffer.begin(), buffer.begin() + trancate);
            }
            else {
                buffer.clear();
            }
        }

        size_t size_to_copy = std::min(buffer.size(), require);

        std::vector<short>::iterator itr = buffer.begin();
        dst.insert(dst.end(), itr, itr + size_to_copy);
        buffer.erase(itr, itr + size_to_copy);
//        __android_log_print(ANDROID_LOG_INFO, "InputVoiceData", "Data buffer size(after): %d.", buffer.size());

        //渡されたdataは 11025 / 448 shortの場合、220 からのデータがおかしくなる(220, 110, 110, 8)
        //渡されたdataは 16000 / 640 shortの場合、320 + 160 = 480からのデータがおかしくなる(320, 160, 160)
        //渡されたdataは 22050 / 896 shortの場合、640 からのデータがおかしくなる(441, 220, 220, 15)
        //渡されたdataは 44100 / 1792 shortの場合、1600 からのデータがおかしくなる(882, 441, 441, 28)
        __android_log_print(ANDROID_LOG_INFO, "InputVoiceData", "Data buffer size(after): %d, des.size: %d, require: %d, trancate: %d, data_size_copied: %d", buffer.size(), dst.size(), require, trancate, size_to_copy);

        return size_to_copy;
    }

    // 入力データが終了しているか確認
    bool eod() const override {
        return buffer.empty();
    }

private:
    std::vector<short> buffer;    // 音声データを保持するバッファ
};

extern "C" JNIEXPORT jshortArray JNICALL
Java_com_ntt_skyway_demo_voiceomnia_MainViewModel_nativeProcessVoiceData(
        JNIEnv* env,
        jobject, /* this */
        jshortArray chunk,
        jint chunkSize) {
    jshort * shortArray = env->GetShortArrayElements(chunk, nullptr);
    if (shortArray == nullptr) {
        return nullptr;
    }
    // C++のベクタにコピー

    std::vector<short> data(shortArray, shortArray + chunkSize);

    std::vector<short> processed_data;

    if (se_model) {
        InputSamplePtr inputVoiceDataPtr = std::make_shared<InputVoiceData>(std::move(data));
        se_model->startEnhancement(inputVoiceDataPtr);

        int overlap = se_model->getEnv<int>("overlap");
        __android_log_print(ANDROID_LOG_INFO, "InputVoiceData", "overlap is %d", overlap);


        std::vector<E2EResult> result_list;
        SEModel::SEState se_state;
        do {
            E2EResult se_result = se_model->enhancement(se_state);
            result_list.push_back(se_result);
        } while(se_state != SEModel::SEState::FINISHED);
        se_model->stopEnhancement();
        //TODO std::vector<E2EResult> result_list を std::vectorに変換

        for(E2EResult se_result: result_list){
            VOMatrix<float> enhanced = se_result.get<VOMatrix<float>>("enhanced");
            std::for_each(enhanced.data(), enhanced.data() + enhanced.size(),
                          [&processed_data](float sample){
                short short_sample = static_cast<short>(sample);
                processed_data.push_back(short_sample);
            });
        }

    }

    env->ReleaseShortArrayElements(chunk, shortArray, JNI_ABORT);

    // 処理結果を新しいjbyteArrayとして作成
    jshortArray result = env->NewShortArray(chunkSize);
    if (result == nullptr) {
        return nullptr;
    }

    // 結果を設定
    env->SetShortArrayRegion(result, 0, chunkSize, reinterpret_cast<jshort *>(processed_data.data()));

    return result; // 処理結果をKotlin層に返す
}
