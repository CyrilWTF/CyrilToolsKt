package com.cyrilw.cyriltools.util

import android.graphics.Bitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeUtils {

    private const val WIDTH: Int = 300
    private const val HEIGHT: Int = 300
    private const val CHARACTER: String = "utf-8"

    fun encode(content: String): Bitmap {
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        hints[EncodeHintType.CHARACTER_SET] = CHARACTER
        hints[EncodeHintType.MARGIN] = 1


        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints)

        val pixels = IntArray(WIDTH * HEIGHT)
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                pixels[y * WIDTH + x] = if (bitMatrix.get(x, y)) 0 else 0xffffff
            }
        }

        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT)

        return bitmap
    }

    fun decode(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = HashMap<DecodeHintType, Any>()
        hints[DecodeHintType.CHARACTER_SET] = CHARACTER
        hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE

        return QRCodeReader().decode(binaryBitmap, hints).toString()
    }

}