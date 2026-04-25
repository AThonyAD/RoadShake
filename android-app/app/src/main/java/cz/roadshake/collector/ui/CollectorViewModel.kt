package cz.roadshake.collector.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cz.roadshake.collector.data.TelemetryRecord

enum class RoadContext(val spoken: String) {
    DALNICE("dálnice"),
    BEZNA_SILNICE("běžná silnice"),
    ROZBITA_SILNICE("rozbitá silnice"),
    POLNI_LESNI("polní/lesní cesta"),
    VLAKOVY_PREJEZD("vlakový přejezd"),
    RETARDER("retardér")
}

class CollectorViewModel : ViewModel() {
    var isCollecting by mutableStateOf(false)
        private set

    var latestRecord by mutableStateOf<TelemetryRecord?>(null)
        private set

    var currentRoadContext by mutableStateOf(RoadContext.BEZNA_SILNICE)
        private set

    var lastVoiceResult by mutableStateOf("-")
        private set

    fun onStart() {
        isCollecting = true
    }

    fun onStop() {
        isCollecting = false
    }

    fun onNewRecord(record: TelemetryRecord) {
        latestRecord = record
    }

    fun applyVoiceCommand(recognizedText: String): Boolean {
        val normalized = recognizedText.lowercase()
        val mapped = when {
            "dálnice" in normalized || "dalnice" in normalized -> RoadContext.DALNICE
            "běžná silnice" in normalized || "bezna silnice" in normalized -> RoadContext.BEZNA_SILNICE
            "rozbitá silnice" in normalized || "rozbita silnice" in normalized -> RoadContext.ROZBITA_SILNICE
            "polní" in normalized || "lesní" in normalized || "polni" in normalized || "lesni" in normalized -> RoadContext.POLNI_LESNI
            "vlakový přejezd" in normalized || "vlakovy prejezd" in normalized -> RoadContext.VLAKOVY_PREJEZD
            "retardér" in normalized || "retarder" in normalized -> RoadContext.RETARDER
            else -> null
        }

        lastVoiceResult = recognizedText

        return if (mapped != null) {
            currentRoadContext = mapped
            true
        } else {
            false
        }
    }

    fun applyYesNo(recognizedText: String): Boolean? {
        val normalized = recognizedText.lowercase()
        lastVoiceResult = recognizedText
        return when {
            normalized.contains("ano") -> true
            normalized.contains("ne") -> false
            else -> null
        }
    }
}
