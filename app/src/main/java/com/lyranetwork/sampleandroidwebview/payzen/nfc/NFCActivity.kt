package com.lyranetwork.sampleandroidwebview.payzen.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.devnied.emvnfccard.parser.EmvParser
import org.apache.commons.io.IOUtils
import com.lyranetwork.sampleandroidwebview.R
import org.json.JSONObject
import java.io.IOException
import java.util.*


/**
 * Activity to manage NFC communication
 *
 * @property nfcAdapter NfcAdapter
 * @property nfcProvider NFCProvider
 * @property INTENT_FILTER Array<IntentFilter>
 * @property TECH_LIST Array<Array<(kotlin.String..kotlin.String?)>>
 */
class NFCActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_SCAN_RESULT = "NFC_SCAN_RESULT"
    }

    private lateinit var nfcAdapter: NfcAdapter

    /**
     * IsoDep provider
     */
    private val nfcProvider = NFCProvider()

    /**
     * Intent filter
     */
    private val INTENT_FILTER = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

    /**
     * Tech List
     */
    private val TECH_LIST = arrayOf(arrayOf(IsoDep::class.java.name))

    /**
     * onCreate method
     * Activity creation
     *
     * @param savedInstanceState Bundle?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init main layout
        val contentView = LinearLayout(this)
        contentView.gravity = Gravity.CENTER
        contentView.orientation = LinearLayout.VERTICAL
        setContentView(contentView)

        val nfcLayoutImg = ImageView(this)
        val nfcLayoutImgLyt = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        nfcLayoutImgLyt.leftMargin = 200
        nfcLayoutImgLyt.rightMargin = 200
        nfcLayoutImg.layoutParams = nfcLayoutImgLyt
        nfcLayoutImg.setImageResource(R.drawable.universalcontactlesscardsymbol)
        contentView.addView(nfcLayoutImg)

        val nfcLayoutTxt = TextView(this)
        val nfcLayoutTxtLyt = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        nfcLayoutTxtLyt.topMargin = 100
        nfcLayoutTxt.layoutParams = nfcLayoutTxtLyt
        nfcLayoutTxt.text = getText(R.string.contactless_scanner)
        nfcLayoutTxt.gravity = Gravity.CENTER
        nfcLayoutTxt.textSize = 30f
        contentView.addView(nfcLayoutTxt)

        // NFC Adapter instantiation
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    /**
     * onResume lifecycle of activity, enable NFC reading
     */
    override fun onResume() {
        super.onResume()

        val pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, INTENT_FILTER, TECH_LIST)
    }

    /**
     * onPause lifecycle of activity, disable NFC reading
     */
    override fun onPause() {
        nfcAdapter.disableForegroundDispatch(this)
        super.onPause()
    }

    /**
     * Return NFC scanning result
     *
     * @param cardNumber String?
     * @param expirationMonth String?
     * @param expirationYear String?
     */
    private fun returnResult(cardNumber: String?, expirationMonth: String?, expirationYear: String?) {
        val intentResult = Intent()
        val card = JSONObject().put("cardNumber", cardNumber)
                .put("month", expirationMonth)
                .put("year", expirationYear)
        intentResult.putExtra(EXTRA_SCAN_RESULT, card.toString())
        setResult(NFC_ACTIVITY_CODE_RESULT, intentResult)
        finish()
    }

    /**
     * Manage NFC result
     *
     * @param intent Intent
     */
    override fun onNewIntent(intent : Intent){
        if(NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                doAsync {
                    val tagComm = IsoDep.get(tag)
                    if (tagComm == null) {
                        // error
                        returnResult("", "", "")
                    }
                    try {
                        tagComm.connect()
                        nfcProvider.setTagComm(tagComm)
                        val parser = EmvParser(nfcProvider, true)
                        val card = parser.readEmvCard()
                        val cal = Calendar.getInstance()
                        cal.time = card.expireDate
                        val year = cal.get(Calendar.YEAR).toString()
                        val month = (cal.get(Calendar.MONTH) + 1).toString()
                        returnResult(card.cardNumber, month, year)
                    } catch (e: IOException) {
                        // error
                        returnResult("", "", "")
                    } finally {
                        IOUtils.closeQuietly(tagComm)
                    }
                }.execute()
            }
        }
    }
}


/**
 * Allow to execute HTTP call outside UI thread
 * @property handler Function0<Unit>
 * @constructor
 */
class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}
