package com.ntt.skyway.demo.voiceomnia

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ntt.skyway.demo.voiceomnia.ui.theme.VoiceOmniaDemoTheme
import java.io.File
import kotlin.math.log

class MainActivity : ComponentActivity() {
    private val mainViewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seModelPath = copyAssetToInternalStorage(this, "20240221_model_se.bin")
        nativeInitialize(seModelPath)

        enableEdgeToEdge()
        setContent {
            VoiceOmniaDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = mainViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        Log.d("MainActivity", "stringFromJNI: ${stringFromJNI()}")

        // 権限の要求
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECORD_AUDIO
                ),
                0
            )
        }

        // Example of a call to a native method
        // binding.sampleText.text = stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'voiceomnia' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun nativeInitialize(filePath: String): Void

    companion object {
        // Used to load the 'voiceomnia' library on application startup.
        init {
            System.loadLibrary("voiceomniawrapper")
        }
    }

    private fun copyAssetToInternalStorage(context: Context, fileName: String): String {
        // コピー先のパス
        val outputFile = File(context.filesDir, fileName)

        // すでに存在していれば何もしない
        if (!outputFile.exists()) {
            context.assets.open(fileName).use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        // コピー先のパスを返す
        return outputFile.absolutePath
    }
}
