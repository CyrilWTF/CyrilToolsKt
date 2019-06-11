package com.cyrilw.cyriltools.ui

import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseFragment
import com.cyrilw.cyriltools.util.TextConvertUtils

class TextConvertFragment : BaseFragment() {

    companion object {
        private const val TYPE_UNICODE: String = "Unicode"
        private const val TYPE_BASE64: String = "Base64"
        private const val TYPE_MORSE: String = "Morse"
        private const val TYPE_URL: String = "Url"

        private const val NO_TEXT: String = "No text found"
    }

    override val layoutRes: Int
        get() = R.layout.fragment_text_convert
    override val menuRes: Int?
        get() = null

    private val etNormal: EditText by bindView(R.id.text_normal)
    private val etSpecial: EditText by bindView(R.id.text_special)

    private var mType = TYPE_UNICODE

    override fun initView(view: View) {
        setToolbarTitle(Constant.FEATURE_TEXT_CONVERT)
        setShowBack()

        val btnConvert = view.findViewById<Button>(R.id.convert)
        val btnClear = view.findViewById<Button>(R.id.text_clear)
        val spinner = view.findViewById<Spinner>(R.id.options)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mType = spinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        btnConvert.setOnClickListener {
            convert(mType)
        }

        btnClear.setOnClickListener {
            etNormal.text.clear()
            etSpecial.text.clear()
        }
    }

    private fun convert(type: String) {
        try {
            if (etNormal.text.toString().isEmpty()) {
                if (etSpecial.text.toString().isEmpty()) {
                    mActivity?.showSnackBar(NO_TEXT)
                } else {
                    etSpecial.text.toString().let {
                        val origin = when (type) {
                            TYPE_UNICODE -> TextConvertUtils.unicodeToText(it)
                            TYPE_BASE64 -> TextConvertUtils.base64ToText(it)
                            TYPE_MORSE -> TextConvertUtils.morseToText(it)
                            TYPE_URL -> TextConvertUtils.urlToText(it)
                            else -> null
                        }
                        with(etNormal) {
                            setText(origin)
                            setSelection(text.length)
                        }
                    }
                }
            } else {
                etNormal.text.toString().let {
                    val result = when (type) {
                        TYPE_UNICODE -> TextConvertUtils.textToUnicode(it)
                        TYPE_BASE64 -> TextConvertUtils.textToBase64(it)
                        TYPE_MORSE -> TextConvertUtils.textToMorse(it)
                        TYPE_URL -> TextConvertUtils.textToUrl(it)
                        else -> null
                    }
                    with(etSpecial) {
                        setText(result)
                        setSelection(text.length)
                    }
                }
            }
        } catch (e: TextConvertUtils.IllegalInputException) {
            mActivity?.showSnackBar(e.message ?: "Convert fail")
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}