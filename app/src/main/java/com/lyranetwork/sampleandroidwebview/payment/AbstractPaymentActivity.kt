package com.lyranetwork.sampleandroidwebview.payment

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import org.json.JSONObject

/**
 * AbstractPaymentActivity
 */
abstract class AbstractPaymentActivity: AppCompatActivity() {

    /**
     * Handle payment result
     *
     * @param result PaymentResult
     */
    abstract fun handlePaymentResult(result: PaymentResult)

    /**
     * Allow to retrieve to payment status
     *
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Manage payment result
        if (requestCode == PaymentProvider.WEBVIEW_ACTIVITY_CODE_RESULT && data != null) {
            val result = JSONObject(data.getStringExtra("paymentResult"))
            val paymentResult = PaymentResult()
            if (result.has("cause")) {
                paymentResult.setCause(result.getString("cause"))
            }
            if (result.has("errorCode")) {
                paymentResult.setErrorCode(result.getInt("errorCode"))
            }

            paymentResult.setSuccess(result.getBoolean("success"))

            handlePaymentResult(paymentResult)
        }
    }
}