package com.saitawngpha.ghostaddemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.saitawngpha.ghostaddemo.adjobscheduler.AdJobScheduler
import com.saitawngpha.ghostaddemo.services.ForegroundAdService
import kotlinx.coroutines.launch

/**
 * Educational UI to demonstrate the GhostAd mechanism
 * Allows researchers to start/stop the malicious service
 * source@ https://blog.checkpoint.com/research/ghostad-hidden-google-play-adware-drains-devices-and-disrupts-millions-of-users/
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GhostAdDemoScreen(
                onStartService = { startGhostAdService() },
                onStopService = { stopGhostAdService() }
            )
        }
    }

    private fun startGhostAdService() {
        val intent = Intent(this, ForegroundAdService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // Schedule the watchdog job
        AdJobScheduler.scheduleAdJob(this)
    }

    private fun stopGhostAdService() {
        // Stop the service
        stopService(Intent(this, ForegroundAdService::class.java))

        // Cancel the watchdog job
        val jobScheduler = getSystemService(android.app.job.JobScheduler::class.java)
        jobScheduler.cancel(AdJobScheduler.JOB_ID)

        // Note: Real malware would have multiple persistence mechanisms
        // making it extremely difficult to stop completely
    }
}

@Composable
fun GhostAdDemoScreen(
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GhostAd Malware Research Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "⚠️ EDUCATIONAL PURPOSE ONLY ⚠️\n\n" +
                            "This demonstrates how GhostAd malware persists in the background " +
                            "with invisible notifications and self-healing JobScheduler.\n\n" +
                            "Check logcat for 'FRAUD:' messages to see the ad loop in action.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            onStartService()
                            isRunning = true
                        },
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Start Malicious Service")
                    }

                    Button(
                        onClick = {
                            onStopService()
                            isRunning = false
                        },
                        enabled = isRunning
                    ) {
                        Text("Stop Service")
                    }
                }

                if (isRunning) {
                    Text(
                        text = "\n🔴 Service is RUNNING\n" +
                                "• Check Settings > Apps > Running Services\n" +
                                "• Look for the blank notification\n" +
                                "• Watch logcat for fraudulent activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            GhostAdDemoTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//
//                }
//            }
//        }
//    }
//}

