package run.jkdev.dec4iot.jetpack.http

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

class Get(queue: RequestQueue, tag: String) {
    private var queue: RequestQueue
    private var tag: String

    init {
        this.queue = queue
        this.tag = tag
    }

    fun StringReq(url: String, shouldCache: Boolean, res: Response.Listener<String>, err: Response.ErrorListener) {
        val req = StringRequest(Request.Method.GET, url, res, err)
        req.setShouldCache(shouldCache)
        req.tag = this.tag
        this.queue.add(req)
    }
}