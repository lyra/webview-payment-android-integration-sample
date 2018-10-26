package com.lyranetwork.sampleandroidwebview.payzen.nfc

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.enums.SwEnum
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider
import java.io.IOException

/**
 * NFC provider
 *
 * @property tagComm IsoDep?
 */
class NFCProvider : IProvider {

    private var tagComm: IsoDep? = null

    /**
     * transceive method
     *
     * @param pCommand ByteArray
     * @return ByteArray?
     * @throws CommunicationException
     */
    @Throws(CommunicationException::class)
    override fun transceive(pCommand: ByteArray): ByteArray? {
       var response: ByteArray? = null
        try {
            // send command to emv card
            response = tagComm!!.transceive(pCommand)
        } catch (e: IOException) {
            throw CommunicationException(e.message)
        }

        try {
            val `val` = SwEnum.getSW(response)
        } catch (e: Exception) {
        }

        return response
    }

    /**
     * tagComm setter
     *
     * @param tagComm IsoDep
     */
    fun setTagComm(tagComm: IsoDep) {
        this.tagComm = tagComm
    }

}