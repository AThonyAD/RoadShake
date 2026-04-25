package cz.roadshake.collector.sensors

import android.content.Context
import cz.roadshake.collector.data.CsvLogger
import cz.roadshake.collector.data.TelemetryRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * MVP scaffold engine:
 * - runs a 1-second tick
 * - writes a placeholder telemetry record to CSV
 *
 * NOTE: Real sensor + GPS integration comes in the next implementation step.
 */
class CollectorEngine(
    context: Context,
    private val roadContextProvider: () -> String,
    private val onTick: (TelemetryRecord) -> Unit
) {
    private val logger = CsvLogger(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job != null) return

        job = scope.launch {
            logger.initializeFileIfNeeded()
            while (isActive) {
                val sample = fakeRecord()
                logger.append(sample)
                onTick(sample)
                delay(1_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun fakeRecord(): TelemetryRecord {
        val ax = Random.nextDouble(-0.8, 0.8)
        val ay = Random.nextDouble(-0.8, 0.8)
        val az = Random.nextDouble(-0.8, 0.8)

        return TelemetryRecord(
            timestampUnixMs = System.currentTimeMillis(),
            lat = 50.0873,
            lon = 14.4200,
            speedKmh = Random.nextDouble(20.0, 60.0),
            accRms = sqrt((ax * ax + ay * ay + az * az) / 3.0),
            gyroRms = Random.nextDouble(0.01, 0.12),
            phoneModel = android.os.Build.MODEL.orEmpty(),
            roadContext = roadContextProvider()
        )
    }
}
