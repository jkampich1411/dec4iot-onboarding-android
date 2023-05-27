package run.jkdev.dec4iot.jetpack.gsonmodels

import com.google.gson.annotations.SerializedName

data class BangleJsSendData(
    @SerializedName("info")
    val info: BangleJsSendDataInfo,

    @SerializedName("data")
    val data: BangleJsSendDataData
)

data class BangleJsSendDataInfo(
    @SerializedName("sensor_id")
    val sensor_id: Number,

    @SerializedName("sensor_endpoint")
    val sensor_endpoint: String,

    @SerializedName("mac_address")
    val mac_address: String
)

data class BangleJsSendDataData(
    val accl: BangleJsAccelData,
    val baro: BangleJsBaroData,
    val comp: BangleJsCompData,
    val health: BangleJsHrmData,
    val battery: Number
)