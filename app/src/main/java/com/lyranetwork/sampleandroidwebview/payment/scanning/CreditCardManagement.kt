package com.lyranetwork.sampleandroidwebview.payment.scanning

import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard

// Technical code used for specifying the onScanResult of scanning activity
const val SCANNING_ACTIVITY_CODE_RESULT = 982

/**
 * Allow to scan credit card
 */
object CreditCardManagement {

    /**
     * Launch scanning activity
     *
     * @param activity Activity
     */
    fun scan(activity: Activity) {
        val scanIntent = Intent(activity, CardIOActivity::class.java)

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false) // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false) // default: false
        scanIntent.putExtra(io.card.payment.CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
        scanIntent.putExtra(io.card.payment.CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true)
        scanIntent.putExtra(io.card.payment.CardIOActivity.EXTRA_USE_CARDIO_LOGO, false)
        scanIntent.putExtra(io.card.payment.CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, false)

        // SCANNING_ACTIVITY_CODE_RESULT is arbitrary and is only used within this activity.
        activity.startActivityForResult(scanIntent, SCANNING_ACTIVITY_CODE_RESULT)
    }

    /**
     * Retrieve scanning result
     *
     * @param requestCode Int
     * @param data Intent?
     * @param webView WebView
     */
    fun onScanResult(requestCode: Int, data: Intent?, webView: WebView) {
        if (requestCode == SCANNING_ACTIVITY_CODE_RESULT) {
            val resultDisplayStr: String
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {

                val scanResult = data.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)
                val cardNumber = scanResult.cardNumber
                var year = ""
                var month = ""
                if (scanResult.isExpiryValid) {
                    year = scanResult.expiryYear.toString()
                    month = scanResult.expiryMonth.toString()
                }
                val values = "$cardNumber|$month|$year"
                webView.loadUrl("javascript:setCardData('$values')")
            }
        }
    }
}