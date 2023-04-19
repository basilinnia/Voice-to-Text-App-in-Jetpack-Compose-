package com.example.audiototext
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.example.audiototext.ui.theme.AudioToTextTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val speechRecognizerParser by lazy { SpeechRecognizerParser(application) }

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalAnimationApi::class,
        ExperimentalPermissionsApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioToTextTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val snackbarHostState = remember { SnackbarHostState() }
                    var canRecord by remember { mutableStateOf(false) }

                    val recordAudioLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = {isGranted->
                            canRecord = isGranted
                        }
                    )
                    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
                    LaunchedEffect(key1 = recordAudioPermissionState) {
                        if (recordAudioPermissionState.hasPermission) {
                            canRecord = true
                        } else {
                            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }

                    val state by speechRecognizerParser.state.collectAsState()

                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    if(recordAudioPermissionState.hasPermission) {
                                        if (state.isSpeaking) {
                                            speechRecognizerParser.stopListening()
                                        } else {
                                            speechRecognizerParser.startListening("en")
                                        }
                                    } else if (recordAudioPermissionState.shouldShowRationale) {
                                        Toast.makeText(this, "App needs permission to record audio", Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        Toast.makeText(this, "Check app permissions", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                AnimatedContent(targetState = state.isSpeaking) {
                                    if (state.isSpeaking) {
                                        Icon(
                                            imageVector = Icons.Rounded.Stop,
                                            contentDescription = "Stop icon"
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Mic,
                                            contentDescription = "Mic icon"
                                        )
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedContent(targetState = state.isSpeaking) {
                                if (state.isSpeaking) {
                                    Text(text = "Speaking...")
                                } else {
                                    Text(
                                        text = state.spokenText.ifEmpty {
                                            "Click on mic to record audio."
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
