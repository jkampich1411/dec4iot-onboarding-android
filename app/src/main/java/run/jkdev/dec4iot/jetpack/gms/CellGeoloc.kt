package run.jkdev.dec4iot.jetpack.gms

import android.annotation.SuppressLint
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityWcdma
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import run.jkdev.dec4iot.jetpack.TAG
import run.jkdev.dec4iot.jetpack.gsonmodels.CellGeolocPayload
import run.jkdev.dec4iot.jetpack.gsonmodels.CellInfo
import run.jkdev.dec4iot.jetpack.gsonmodels.MLSResponse
import run.jkdev.dec4iot.jetpack.gsonmodels.NetworkInfo

class CellGeoloc constructor(private val mgr: TelephonyManager) {

    @SuppressLint("MissingPermission")
    fun getCells(): List<CellInfo>{
        val cellsList = mutableListOf<CellInfo>()
        val cellInfo = mgr.allCellInfo

        cellInfo.forEach { cell ->
            val identity = cell.cellIdentity

            when(identity::class.java) {
                CellIdentityGsm::class.java -> {
                    val gsmCell = identity as CellIdentityGsm

                    if(gsmCell.mccString == null || gsmCell.mncString == null || gsmCell.cid == android.telephony.CellInfo.UNAVAILABLE || gsmCell.lac == android.telephony.CellInfo.UNAVAILABLE) {
                        return@forEach
                    }

                    val info = CellInfo(
                        radioType = "gsm",
                        mcc = gsmCell.mccString!!.toInt(),
                        mnc = gsmCell.mncString!!.toInt(),
                        cellId = gsmCell.cid,
                        locationAreaCode = gsmCell.lac,
                        signalStrength = cell.cellSignalStrength.dbm,
                        lastDetectionMs = null,
                        timingAdvance = null
                    )

                    cellsList.add(info)

                }
                CellIdentityWcdma::class.java -> {
                    val wcdmaCell = identity as CellIdentityWcdma

                    if(wcdmaCell.mccString == null || wcdmaCell.mncString == null || wcdmaCell.cid == android.telephony.CellInfo.UNAVAILABLE || wcdmaCell.lac == android.telephony.CellInfo.UNAVAILABLE) {
                        return@forEach
                    }

                    val info = CellInfo(
                        radioType = "wcdma",
                        mcc = wcdmaCell.mccString!!.toInt(),
                        mnc = wcdmaCell.mncString!!.toInt(),
                        cellId = wcdmaCell.cid,
                        locationAreaCode = wcdmaCell.lac,
                        signalStrength = cell.cellSignalStrength.dbm,
                        lastDetectionMs = null,
                        timingAdvance = null
                    )

                    cellsList.add(info)
                }
                CellIdentityLte::class.java -> {
                    val lteCell = identity as CellIdentityLte

                    if(lteCell.mccString == null || lteCell.mncString == null || lteCell.ci == android.telephony.CellInfo.UNAVAILABLE || lteCell.tac == android.telephony.CellInfo.UNAVAILABLE) {
                        return@forEach
                    }

                    val info = CellInfo(
                        radioType = "lte",
                        mcc = lteCell.mccString!!.toInt(),
                        mnc = lteCell.mncString!!.toInt(),
                        cellId = lteCell.ci,
                        locationAreaCode = lteCell.tac,
                        signalStrength = cell.cellSignalStrength.dbm,
                        lastDetectionMs = null,
                        timingAdvance = null
                    )

                    cellsList.add(info)
                }
            }
        }

        return cellsList
    }

    fun getNetworkInfo(): NetworkInfo {
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()
        val localMgr = mgr.createForSubscriptionId(subId)

        val simOperator = localMgr.simOperator
        val mcc = simOperator.substring(0, 3)
        val mnc = simOperator.substring(3)

        return NetworkInfo(
            carrier = localMgr.simCarrierIdName.toString(),
            mcc = mcc.toInt(),
            mnc = mnc.toInt()
        )
    }

    suspend fun requestGeolocation(networkInfo: NetworkInfo, cellsList: List<CellInfo>): Result<MLSResponse> {   // Yes, I think that type makes perfect sense.
        val requestBody = CellGeolocPayload(
            carrier = networkInfo.carrier,
            shouldConsiderIpAddress = true,
            simMobileCountryCode = networkInfo.mcc,
            simMobileNetworkCode = networkInfo.mnc,

            cells = cellsList
        )

        val requestBodyJson = Gson().toJson(requestBody).toString()

        val req = Request.Builder()
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .url("https://location.services.mozilla.com/v1/geolocate?key=test")
            .build()

        return withContext(Dispatchers.IO) {
            val response = OkHttpClient().newCall(req).execute()
            if (response.code != 200) {
                Log.e(TAG, response.body!!.string())
                return@withContext Result.Error(Exception("MLS returned ${response.code}"))
            }

            val mls = Gson().fromJson(response.body?.string(), MLSResponse::class.java)
            return@withContext Result.Success(mls)
        }
    }

}

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}