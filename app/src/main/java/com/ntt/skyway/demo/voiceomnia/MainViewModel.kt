package com.ntt.skyway.demo.voiceomnia

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }
    var isRecording by mutableStateOf<Boolean>(false)
        private set

    val sampleRate = 44100 // サンプルレート（標準的な値）
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val unitAudioBuffer = ShortArray(bufferSize)

    val recordedData = mutableListOf<Short>()

    @SuppressLint("MissingPermission")
    fun startRecording() {
        recordedData.clear()
        Log.d(TAG, "start recording, unit buffer size = $bufferSize")
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        isRecording = true

        viewModelScope.launch(Dispatchers.IO) {
            audioRecord.startRecording()
            while (isRecording) {
                val readBytes = audioRecord.read(unitAudioBuffer, 0, bufferSize)
                Log.d(TAG, "Bytes read: $readBytes")
                if (readBytes > 0) {
                    recordedData.addAll(unitAudioBuffer.take(readBytes))
                } else {
                    break
                }
            }
            Log.d(TAG, "stop recording.")
            audioRecord.stop()
            audioRecord.release()

            Log.d(TAG, "recordedData.size = ${recordedData.size}")
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    fun startPlayback() {
        viewModelScope.launch(Dispatchers.IO) {
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioTrack.play()
            for (i in recordedData.indices step bufferSize) {
                val chunk = recordedData.subList(i, minOf(i + bufferSize, recordedData.size)).toShortArray()
                Log.d(TAG, "No.${i / bufferSize}, chunk.size = ${chunk.size}")
                val processedChunk = nativeProcessVoiceData(chunk, chunk.size)
                audioTrack.write(processedChunk, 0, processedChunk.size)
            }
            audioTrack.stop()
            audioTrack.release()
        }
    }

    private external fun nativeProcessVoiceData(chunk: ShortArray, chunkSize: Int): ShortArray
}