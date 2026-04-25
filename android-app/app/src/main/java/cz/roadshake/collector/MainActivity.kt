package cz.roadshake.collector

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.roadshake.collector.sensors.CollectorEngine
import cz.roadshake.collector.ui.CollectorViewModel
import cz.roadshake.collector.voice.VoiceAssistant

class MainActivity : ComponentActivity() {

    private var engine: CollectorEngine? = null
    private var voiceAssistant: VoiceAssistant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        voiceAssistant = VoiceAssistant(applicationContext)

        setContent {
            val vm: CollectorViewModel = viewModel()
            engine = engine ?: CollectorEngine(applicationContext, { vm.currentRoadContext.spoken }, vm::onNewRecord)
            val latest by remember { androidx.compose.runtime.derivedStateOf { vm.latestRecord } }
            val speechMode = remember { mutableStateOf("context") }

            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
                val data: Intent = result.data ?: return@rememberLauncherForActivityResult
                val spoken = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
                if (spoken.isBlank()) return@rememberLauncherForActivityResult

                if (speechMode.value == "confirm") {
                    when (vm.applyYesNo(spoken)) {
                        true -> voiceAssistant?.speak("Potvrzeno, ukládám událost.")
                        false -> voiceAssistant?.speak("Rozumím, událost nepotvrzena.")
                        null -> voiceAssistant?.speak("Nerozumím odpovědi. Zkuste ANO nebo NE.")
                    }
                } else {
                    val ok = vm.applyVoiceCommand(spoken)
                    if (ok) {
                        voiceAssistant?.speak("Nastaveno: ${vm.currentRoadContext.spoken}")
                    } else {
                        voiceAssistant?.speak("Nerozumím kategorii. Zkuste například dálnice nebo retardér.")
                    }
                }
            }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("RoadShake Collector (MVP scaffold)")
                    Text(if (vm.isCollecting) "Stav: sběr běží" else "Stav: zastaveno")
                    Text("Kontext: ${vm.currentRoadContext.spoken}")
                    Text("Poslední hlasový vstup: ${vm.lastVoiceResult}")
                    Text("Poslední záznam: ${latest?.timestampUnixMs ?: "-"}")
                    Text("acc_rms: ${latest?.accRms ?: "-"}")
                    Text("gyro_rms: ${latest?.gyroRms ?: "-"}")

                    Button(onClick = {
                        vm.onStart()
                        engine?.start()
                        voiceAssistant?.speak("Sběr spuštěn")
                    }) {
                        Text("Start")
                    }

                    Button(onClick = {
                        vm.onStop()
                        engine?.stop()
                        voiceAssistant?.speak("Sběr zastaven")
                    }) {
                        Text("Stop")
                    }

                    Button(onClick = {
                        speechMode.value = "context"
                        val intent = voiceAssistant?.speechIntent("Řekněte typ cesty") ?: return@Button
                        speechLauncher.launch(intent)
                    }) {
                        Text("Hlas: typ cesty")
                    }

                    Button(onClick = {
                        speechMode.value = "confirm"
                        voiceAssistant?.speak("Byla to překážka? Odpovězte ano nebo ne.")
                        val intent = voiceAssistant?.speechIntent("Odpovězte ANO nebo NE") ?: return@Button
                        speechLauncher.launch(intent)
                    }) {
                        Text("Hlas: potvrzení ANO/NE")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engine?.stop()
        voiceAssistant?.shutdown()
    }
}
