package com.example.heatmap.data.helper

import android.util.Log

object DebugUtilis {
    var _charLimit = 2000
    @JvmStatic
    fun d(tag: String?, message: String): Int {
        // If the message is less than the limit just show
        if (message.length < _charLimit) {
            return Log.d(tag, message)
        }
        val sections = message.length / _charLimit
        for (i in 0..sections) {
            val max = _charLimit * (i + 1)
            if (max >= message.length) {
                Log.d(tag, message.substring(_charLimit * i))
            } else {
                Log.d(tag, message.substring(_charLimit * i, max))
            }
            Log.d(tag, "`````")
        }
        return 1
    }
}
