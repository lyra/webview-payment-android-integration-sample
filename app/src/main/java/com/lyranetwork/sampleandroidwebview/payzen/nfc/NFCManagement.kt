package com.lyranetwork.sampleandroidwebview.payzen.nfc

import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import org.json.JSONObject

// Technical code used for specifying the onScanResult of scanning activity
const val NFC_ACTIVITY_CODE_RESULT = 983

/**
 * Allow to scan credit card by NFC
 */
object NFCManagement {

    /**
     * Launch scanning activity
     *
     * @param activity Activity
     */
    fun scan(activity: Activity) {
        val scanIntent = Intent(activity, NFCActivity::class.java)

        // SCANNING_ACTIVITY_CODE_RESULT is arbitrary and is only used within this activity.
        activity.startActivityForResult(scanIntent, NFC_ACTIVITY_CODE_RESULT)
    }

    /**
     * Retrieve NFC scanning result
     *
     * @param requestCode Int
     * @param data Intent?
     * @param webView WebView
     */
    fun onScanResult(requestCode: Int, data: Intent?, webView: WebView) {
        if (requestCode == NFC_ACTIVITY_CODE_RESULT) {
            if (data != null && data.hasExtra(NFCActivity.EXTRA_SCAN_RESULT)) {

                val result = JSONObject(data.getStringExtra(NFCActivity.EXTRA_SCAN_RESULT))
                val cardNumber = result.get("cardNumber")
                val year = result.get("year")
                val month = result.get("month")
                val values = "$cardNumber|$month|$year"
                webView.loadUrl("javascript:setCardData('$values')")
            }
        }

    }
}
