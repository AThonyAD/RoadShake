package cz.roadshake.collector.data

data class TelemetryRecord(
    val timestampUnixMs: Long,
    val lat: Double,
    val lon: Double,
    val speedKmh: Double,
    val accRms: Double,
    val gyroRms: Double,
    val phoneModel: String,
    val mountPosition: String = "cup_holder_rubber_pad",
    val roadContext: String = "běžná silnice"
)

fun TelemetryRecord.toCsvRow(): String = listOf(
    timestampUnixMs,
    lat,
    lon,
    speedKmh,
    accRms,
    gyroRms,
    phoneModel.replace(",", "_"),
    mountPosition.replace(",", "_"),
    roadContext.replace(",", "_")
).joinToString(",")
