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

    // サンプリング周波数16k / 量子化ビット数16 / 1ch に固定
    // 11025, 22050, 16000, 44100
    val sampleRate = 16000 // サンプルレート（標準的な値）
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val frameLength = 400 //25ms
    val frameShift = 160 //10ms

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
                Log.d(TAG, "chunk = ${chunk.contentToString()}")
                val processedChunk = nativeProcessVoiceData(chunk, chunk.size)
                Log.d(TAG, "processedChunk = ${processedChunk.contentToString()}")
                audioTrack.write(processedChunk, 0, processedChunk.size)
                Log.d(TAG, "difference starts: ${findMatchingIndex(chunk.toList(), processedChunk.toList())}")
            }
            audioTrack.stop()
            audioTrack.release()
        }
    }

    fun findMatchingIndex(array1: List<Short>, array2: List<Short>): Int {
        val minLength = minOf(array1.size, array2.size)
        for (i in 0 until minLength) {
            if (array1[i] != array2[i]) {
                return i // 最初に異なる要素のインデックスを返す
            }
        }
        return minLength // 全て一致している場合は最小の長さを返す
    }

    private external fun nativeProcessVoiceData(chunk: ShortArray, chunkSize: Int): ShortArray
}