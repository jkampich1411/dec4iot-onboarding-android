package run.jkdev.dec4iot.jetpack

import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PuckJsUriActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puck_js_uri)

        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) finish() // Not triggered by NFC? Something is wrong!

        val query = parseQuery(intent.data!!.query.toString())

        if("mac" !in query) finish() // No mac address in query? Something is wrong!

        if(isMainRunning()) {

        } else {

        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        if ("&" !in query) {
            return if ("=" !in query) {
                mapOf(
                    Pair(query, "true")
                )
            } else {
                val kv = query.split("=")
                mapOf(
                    Pair(kv[0], kv[1])
                )
            }
        }

        val returnKVs: MutableMap<String, String> = mutableMapOf()

        val parts = query.split("&")
        parts.forEach {
            if ("=" !in it) {
                returnKVs[it] = "true"
            } else {
                val kv = it.split("=")
                returnKVs[kv[0]] = kv[1]
            }
        }

        return returnKVs
    }

    private fun isMainRunning(): Boolean {
        return MainActivity.running
    }
}