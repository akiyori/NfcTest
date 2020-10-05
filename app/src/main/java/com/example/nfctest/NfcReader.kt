package com.example.nfctest

import android.nfc.tech.NfcF
import android.nfc.Tag;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

class NfcReader {
    fun readTag(tag: Tag?): Array<ByteArray?>? {
        val nfc = NfcF.get(tag)

        try {
            nfc.connect()
            // System 1のシステムコード -> 0xFE00
            val targetSystemCode =
                byteArrayOf(0xfe.toByte(), 0x00.toByte())

            // polling コマンドを作成
            val polling = polling(targetSystemCode)

            // コマンドを送信して結果を取得
            val pollingRes = nfc.transceive(polling)

            // System 0 のIDｍを取得(1バイト目はデータサイズ、2バイト目はレスポンスコード、IDmのサイズは8バイト)
            val targetIDm: ByteArray = Arrays.copyOfRange(pollingRes, 2, 10)

            // サービスに含まれているデータのサイズ(今回は4だった)
            val size = 4

            // 対象のサービスコード -> 0x1A8B
            val targetServiceCode =
                byteArrayOf(0x1A.toByte(), 0x8B.toByte())

            // Read Without Encryption コマンドを作成
            val req = readWithoutEncryption(targetIDm, size, targetServiceCode)

            // コマンドを送信して結果を取得
            val res = nfc.transceive(req)
            nfc.close()

            // 結果をパースしてデータだけ取得
            return parse(res)
        } catch (e: Exception) {
            Log.e(tag.toString(), e.message, e)
        }
        return null
    }

    /**
     * Pollingコマンドの取得。
     * @param systemCode byte[] 指定するシステムコード
     * @return Pollingコマンド
     * @throws IOException
     */
    private fun polling(systemCode: ByteArray): ByteArray {
        val bout = ByteArrayOutputStream(100)
        bout.write(0x00) // データ長バイトのダミー
        bout.write(0x00) // コマンドコード
        bout.write(systemCode) // systemCode
        bout.write(0x01) // リクエストコード
        bout.write(0x0f) // タイムスロット
        val msg: ByteArray = bout.toByteArray()
        msg[0] = msg.size.toByte() // 先頭１バイトはデータ長
        return msg
    }

    /**
     * Read Without Encryptionコマンドの取得。
     * @param IDm 指定するシステムのID
     * @param size 取得するデータの数
     * @return Read Without Encryptionコマンド
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readWithoutEncryption(
        idm: ByteArray,
        size: Int,
        serviceCode: ByteArray
    ): ByteArray {
        val bout = ByteArrayOutputStream(100)
        bout.write(0) // データ長バイトのダミー
        bout.write(0x06) // コマンドコード
        bout.write(idm) // IDm 8byte
        bout.write(1) // サービス数の長さ(以下２バイトがこの数分繰り返す)

        // サービスコードの指定はリトルエンディアンなので、下位バイトから指定します。
        bout.write(serviceCode.reversedArray())
        bout.write(size) // ブロック数

        // ブロック番号の指定
        for (i in 0 until size) {
            bout.write(0x80) // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i) // ブロック番号
        }
        val msg: ByteArray = bout.toByteArray()
        msg[0] = msg.size.toByte() // 先頭１バイトはデータ長
        return msg
    }

    /**
     * Read Without Encryption応答の解析。
     * @param res byte[]
     * @return 文字列表現
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun parse(res: ByteArray): Array<ByteArray?>? {
        // res[10] エラーコード。0x00の場合が正常
        if (res[10].toInt() != 0x00) throw RuntimeException("Read Without Encryption Command Error")

        // res[12] 応答ブロック数
        // res[13 + n * 16] 実データ 16(byte/ブロック)の繰り返し
        val size: Int = res[12].toInt()
        val data =
            Array<ByteArray?>(size) { ByteArray(16) }
        val str = ""
        for (i in 0 until size) {
            val tmp = ByteArray(16)
            val offset = 13 + i * 16
            for (j in 0..15) {
                tmp[j] = res[offset + j]
            }
            data[i] = tmp
        }
        return data
    }
}