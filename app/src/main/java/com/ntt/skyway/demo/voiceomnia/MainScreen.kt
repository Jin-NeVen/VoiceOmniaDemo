package com.ntt.skyway.demo.voiceomnia


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            android.view.MotionEvent.ACTION_DOWN -> {
                                viewModel.startRecording()
                                true
                            }
                            android.view.MotionEvent.ACTION_UP -> {
                                viewModel.stopRecording()
                                true
                            }
                            else -> false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {}, modifier = Modifier) {
                    Text("Hold to Record")
                }
            }
            Button(onClick = {
                viewModel.startPlayback()
            }) {
                Text(text = "Start Playback")
            }
        }
    }
}