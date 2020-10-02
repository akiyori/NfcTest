package com.example.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.MalformedMimeTypeException
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.nfc.Tag;
import kotlinx.android.synthetic.main.activity_main.*

private lateinit var intentFiltersArray: Array<IntentFilter>
private lateinit var techListsArray: Array<Array<String>>
private var mAdapter: NfcAdapter? = null
private var pendingIntent: PendingIntent? = null
private val nfcReader: NfcReader = NfcReader()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)

        try {
            ndef.addDataType("text/plain")
        } catch (e: MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
        intentFiltersArray = arrayOf(ndef)

        // FelicaはNFC-TypeFなのでNfcFのみ指定でOK
        techListsArray = arrayOf(arrayOf(NfcF::class.java.name))

        // NfcAdapterを取得
        mAdapter = NfcAdapter.getDefaultAdapter(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        // NFCの読み込みを有効化
        mAdapter!!.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // IntentにTagの基本データが入ってくるので取得。
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return

        // ここで取得したTagを使ってデータの読み書きを行う。
        textIdm.text = nfcReader.getId(tag)
    }

    override fun onPause() {
        super.onPause()
        mAdapter!!.disableForegroundDispatch(this)
    }
}