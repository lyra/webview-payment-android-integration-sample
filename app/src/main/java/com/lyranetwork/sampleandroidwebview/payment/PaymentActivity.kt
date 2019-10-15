package com.lyranetwork.sampleandroidwebview.payment

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import org.json.JSONObject
import com.lyranetwork.sampleandroidwebview.R
import com.lyranetwork.sampleandroidwebview.payment.scanning.CreditCardManagement


// url of the last step of payment process
private const val CALLBACK_URL_PREFIX: String = "http://webview"

// url part list that can be used by an external viewer
private const val EXTERNAL_URL_PART = "%2Fpdf, /mentions-paiement, /paiement-securise, getticket"


/**
 * WebView activity
 * This allows to display the payment page
 */
class PaymentActivity: AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    private lateinit var webView: WebView

    lateinit var scanBtn: FloatingActionButton
     /**
     * onCreate method
     * Activity creation
     *
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve parameters
        val redirectionUrl = intent.getStringExtra("redirectionUrl")

        // Init main layout
        val contentView = FrameLayout(this)
        setContentView(contentView)

        // Init web view
        webView = initWebview(redirectionUrl)
        contentView.addView(webView)

        progressBar = initProgressBar()
        contentView.addView(progressBar)

        // Credit card scanning
        scanBtn = initScanBtn()
        contentView.addView(scanBtn)

    }

    /**
     * Allow to retrieve to Card IO onScanResult
     *
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        CreditCardManagement.onScanResult(requestCode, data, webView)
    }

    /**
     * Credit card scanning button initialisation
     *
     * @return FloatingActionButton
     */
    private fun initScanBtn(): FloatingActionButton {
        var scanBtn = FloatingActionButton(this)
        val scanBtnLyt = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        scanBtnLyt.gravity = Gravity.BOTTOM or Gravity.RIGHT
        scanBtnLyt.bottomMargin = 30
        scanBtnLyt.rightMargin = 30
        scanBtn.layoutParams = scanBtnLyt
        scanBtn.setImageResource(R.drawable.camera)
        scanBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        (scanBtn as View).visibility = View.INVISIBLE
        scanBtn.setOnClickListener {
            CreditCardManagement.scan(this)
        }

        return scanBtn
    }


    /**
     * When back button is pressed
     */
    override fun onBackPressed() {
        returnResult(false, PaymentErrorCode.PAYMENT_CANCELLED_ERROR,"Payment cancelled by user")
    }

    /**
     * Progressbar initialisation
     * @return ProgressBar
     */
    private fun initProgressBar(): ProgressBar {
        val progressBar = ProgressBar(this)
        val progressBarLyt = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        progressBarLyt.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        progressBar.isIndeterminate = true
        progressBar.layoutParams = progressBarLyt

        return progressBar
    }
    /**
     * Web view initialisation
     * Instantiation & configuration of web view
     *
     * @return WebView
     */
    private fun initWebview(url: String): WebView {
        val webView = WebView(this)

        // Url loading
        webView.loadUrl(url)

        // Enable javascript
        webView.settings.javaScriptEnabled = true

        /* Register a new JavaScript interface called HTMLOUT */
        webView.addJavascriptInterface(HTMLExtractorJavaScriptInterface(this), "HTMLExtractor")

        // Allow to debug WebView from Chrome Dev Tools
        WebView.setWebContentsDebuggingEnabled(false)

        // Define new web view client by overriding shouldOverrideUrlLoading method in order to check urls
        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE

                // Permits to load HTML content to process custom task
                view.loadUrl("javascript:window.HTMLExtractor.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")

                super.onPageFinished(view, url)
            }

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return checkUrl(webView, url)
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView, webResourceRequest: WebResourceRequest): Boolean {
                return checkUrl(webView, webResourceRequest.url.toString())
            }
        }
        webView.canGoForward()

        return webView
    }

    /**
     * Returns payment result
     *
     * @param value Boolean payment is success or not
     * @param errorCode Int? In error case, error code
     * @param cause String? In error case, error cause
     */
    private fun returnResult(value: Boolean, errorCode: Int?, cause: String?) {
        val intentResult = Intent()
        val paymentResult = JSONObject().put("success", value)
        if (!value) {
            paymentResult.put("errorCode", errorCode)
            paymentResult.put("cause", cause)
        }
        intentResult.putExtra("paymentResult", paymentResult.toString())
        setResult(PaymentProvider.WEBVIEW_ACTIVITY_CODE_RESULT, intentResult)
        finish()
    }



    /**
     * Determines if an url corresponds to an external viewer
     *
     * @param url String
     * @return Boolean
     */
    private fun isUrlToOpenedSeparately(url: String): Boolean {
        return EXTERNAL_URL_PART.split(", ").any { s -> url.contains(s) }
    }

    /**
     * Determines if an url corresponds to the last step of payment process
     *
     * @param url String
     * @return Boolean
     */
    private fun isCallbackUrl(url: String): Boolean {
        return url.startsWith(CALLBACK_URL_PREFIX)
    }

    /**
     * Is the final step. We have to determine the payment result and send that to main activity
     *
     * @param result String
     */
    private fun goToFinalActivity(result: String) {
        when {
            result.contains(".success/") -> {
                returnResult(true, null,null)
            }
            result.contains(".cancel/") -> {
                returnResult(false, PaymentErrorCode.PAYMENT_CANCELLED_ERROR,"Payment cancelled by user")
            }
            result.contains(".refused/") -> {
                returnResult(false, PaymentErrorCode.PAYMENT_REFUSED_ERROR,"Payment refused")
            }
            else -> {
                returnResult(false, PaymentErrorCode.UNKNOWN_ERROR, "Unknown error")
            }
        }
    }

    /**
     * For each navigation, url is analysed to determine if it is the last step or we want to display something by an external viewer
     *
     * @param view WebView
     * @param url String
     * @return Boolean
     */
    private fun checkUrl(view: WebView, url: String): Boolean {
        val isCallBack = isCallbackUrl(url)

        when {
            // payment is finish
            isCallBack -> {
                view.stopLoading()
                goToFinalActivity(url)
            }
            // Pdf, Custom link found in page
            isUrlToOpenedSeparately(url) -> {
                view.stopLoading()
                val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                openIntent.`package` = "com.android.chrome"

                try {
                    startActivity(openIntent)
                } catch (e: ActivityNotFoundException) {
                    openIntent.`package` = null
                    startActivity(openIntent)
                }
            }
            else -> view.loadUrl(url)
        }
        return (!isCallBack)
    }
}

internal class HTMLExtractorJavaScriptInterface(_activity: Activity) {

    private val activity = _activity

    private val isPaymentPage = "function setCardData(cardData)"

    //Get current HTML loaded on webview
    @JavascriptInterface
    fun processHTML(html: String) {
        // process the html
        if (html.contains(isPaymentPage)) {
            var webViewActivity = activity as PaymentActivity
            webViewActivity.runOnUiThread(Runnable {
                (webViewActivity.scanBtn as View).visibility = View.VISIBLE
            })
        }
    }
}



