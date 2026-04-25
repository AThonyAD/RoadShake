package cz.roadshake.collector.data

import android.content.Context
import java.io.File

class CsvLogger(private val context: Context) {

    private val fileName = "roadshake_session.csv"

    fun initializeFileIfNeeded() {
        val file = targetFile()
        if (!file.exists()) {
            file.writeText("ts_unix_ms,lat,lon,speed_kmh,acc_rms,gyro_rms,phone_model,mount_position,road_context\n")
        }
    }

    fun append(record: TelemetryRecord) {
        targetFile().appendText(record.toCsvRow() + "\n")
    }

    fun targetFile(): File = File(context.filesDir, fileName)
}
