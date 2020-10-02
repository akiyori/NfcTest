package com.example.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mNfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFilters: Array<IntentFilter>? = null
    private var techLists: Array<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // 受け取るIntentを指定
        intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED))

        // 反応するタグの種類を指定
        techLists = arrayOf(
            arrayOf(android.nfc.tech.Ndef::class.java.name),
            arrayOf(android.nfc.tech.NdefFormatable::class.java.name))

        mNfcAdapter = NfcAdapter.getDefaultAdapter(applicationContext)
    }

    override fun onResume() {
        super.onResume()

        // NFCタグの検出を有効化
        mNfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
    }

    /**
     * NFCタグの検出時に呼ばれる
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // タグのIDを取得
        val tagId : ByteArray = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: return

        var list = ArrayList<String>()
        for(byte in tagId) {
            list.add(String.format("%02X", byte.toInt() and 0xFF))
        }

        // 画面に表示
        var tagTextView: TextView = findViewById(R.id.textIdm)
        tagTextView.text = list.joinToString(":")
    }

    override fun onPause() {
        super.onPause()

        mNfcAdapter?.disableForegroundDispatch(this)
    }
}