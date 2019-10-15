package com.lyranetwork.sampleandroidwebview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.lyranetwork.sampleandroidwebview.payment.AbstractPaymentActivity
import com.lyranetwork.sampleandroidwebview.payment.PaymentProvider
import com.lyranetwork.sampleandroidwebview.payment.PaymentData
import com.lyranetwork.sampleandroidwebview.payment.PaymentResult
import kotlinx.android.synthetic.main.activity_main.*


// Merchant server url
// FIXME: change by the right payment server
private const val SERVER_URL="<REPLACE_ME>"

// Environment TEST or PRODUCTION, refer to documentation for more information
// FIXME: change by your targeted environment
private const val PAYMENT_MODE = "TEST"

/**
 * Main activity
 *
 * This main activity allows to user to fill payment data (amount, order id, so on)
 * After retrieving these payment data:
 * <li>PaymentProvider.execute(payload: JSONObject, serverUrl: String, activity: Activity) is executed</li>.
 * <li>Merchant server is called with payment data and returns a payment url</li>
 * <li>From this url, payment page is displayed</li>
 * <li>The payment result is handled by handlePaymentResult(result: PaymentResult) method</li>
 *
 * For readability purposes in this example, we do not use logs
 * @author Lyra Network
 */
class MainActivity: AbstractPaymentActivity() {

    /**
     * onCreate method
     * Activity creation
     *
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Create JSONObject use as payload of HTTP request to Merchant server
     *
     * @return JSONObject for payload payment part
     */
    private fun createPaymentPayload(): PaymentData {

        val paymentData = PaymentData()
        paymentData.setOrderId(orderText.text.toString())
        paymentData.setAmount(amountText.text.toString())
        paymentData.setEmail(emailText.text.toString())
        paymentData.setMode(PAYMENT_MODE)

        // Don't provide cardType parameter if any card are accepted
        if (cardSpn.selectedItemId != 0L) {
            paymentData.setCardType((cardSpn.selectedView as TextView).text.toString())
        }

        // Specify the currency code
        // For example, for Euro currency, use "978" value
        // See: https://en.wikipedia.org/wiki/ISO_4217
        paymentData.setCurrency("978")

        return paymentData
    }

    /**
     * onPayClick method
     * Payment execution
     *
     * @param view View Pay button
     */
    fun onPayClick(view: View) {
        val payload = createPaymentPayload()
        progressBar.visibility = View.VISIBLE
        PaymentProvider.execute(payload, SERVER_URL, this)
    }

    /**
     * Handle payment result
     *
     * @param result PaymentResult contains a success boolean. If success is false then result contains also errorCode as an Int (PaymentErrorCode) and cause as a String value
     */
    override fun handlePaymentResult(result: PaymentResult) {
        progressBar.visibility = View.GONE
        if (result.isSuccess()) {
            Toast.makeText(this, "Payment successful" , Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Payment failed. errorCode = " + result.getErrorCode() + " and cause = " + result.getCause() , Toast.LENGTH_LONG).show()
        }
    }
}