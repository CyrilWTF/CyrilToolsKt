package com.cyrilw.cyriltools.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseFragment
import com.cyrilw.cyriltools.util.QRCodeUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class QRCodeFragment : BaseFragment(), CoroutineScope {

    companion object {
        private const val TYPE: String = "image/*"
        private const val READ_REQUEST_CODE: Int = 0
        private const val CROP_REQUEST_CODE: Int = 1
    }

    override val layoutRes: Int
        get() = R.layout.fragment_qrcode
    override val menuRes: Int?
        get() = R.menu.menu_qr_code

    private val ivQRCode: ImageView by bindView(R.id.qr_code)
    private val etContent: EditText by bindView(R.id.qr_code_content)

    private val mJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + mJob

    private var isGenerated: Boolean = false

    override fun onDestroyView() {
        super.onDestroyView()

        val bitmap = (ivQRCode.drawable as? BitmapDrawable)?.bitmap
        bitmap?.run {
            ivQRCode.setImageBitmap(null)
            recycle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    override fun initView(view: View) {
        setToolbarTitle(Constant.FEATURE_QR_CODE)
        setShowBack()

        val btnEncode = view.findViewById<Button>(R.id.qr_code_encode)

        btnEncode.setOnClickListener {
            etContent.text?.let {
                try {
                    val bitmap = QRCodeUtils.encode(it.toString())
                    ivQRCode.setImageBitmap(bitmap)
                    isGenerated = true
                } catch (e: Exception) {
                    mActivity?.showSnackBar("Encoding fail.")
                    e.printStackTrace()
                }
            }
        }
    }

    override fun setMenu(itemId: Int) {
        when (itemId) {
            R.id.menu_select_image -> selectImage()
            R.id.menu_save_image -> saveImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val bundle = data?.extras
            when (requestCode) {
                READ_REQUEST_CODE -> {
                    val intent = Intent("com.android.camera.action.CROP").apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(uri, TYPE)
                        putExtra("aspectX", 1)
                        putExtra("aspectY", 1)
                        putExtra("outputX", 300)
                        putExtra("outputY", 300)
                        putExtra("return-data", true)
                    }
                    startActivityForResult(intent, CROP_REQUEST_CODE)
                }
                CROP_REQUEST_CODE -> {
                    val bitmap = bundle?.getParcelable<Bitmap>("data")
                    bitmap?.let {
                        ivQRCode.setImageBitmap(it)
                        isGenerated = false
                        try {
                            etContent.setText(QRCodeUtils.decode(it))
                        } catch (e: Exception) {
                            mActivity?.showSnackBar("Decoding fail")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @Suppress("SimpleDateFormat")
    private fun saveImage() {
        if (isGenerated) {
            val time = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
            val dir = mActivity?.getExternalFilesDir("QRCode")
            try {
                launch {
                    val path = "$dir/$time.jpg"
                    val bitmap = (ivQRCode.drawable as? BitmapDrawable)?.bitmap
                    bitmap?.run {
                        val outputStream = FileOutputStream(File(path))
                        compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    launch(Dispatchers.Main) { mActivity?.showSnackBar("QR code saved") }
                }
            } catch (e: Exception) {
                mActivity?.showSnackBar("Saving fail")
                e.printStackTrace()
            }
        } else {
            mActivity?.showSnackBar("No generated QR code")
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = TYPE
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

}
