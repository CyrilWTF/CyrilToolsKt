package com.cyrilw.cyriltools.util

import android.net.Uri
import android.util.Base64
import android.util.SparseArray

object TextConvertUtils {

    private const val MESSAGE_ILLEGAL: String = "Illegal input"
    private const val MESSAGE_UNSUPPORTED: String = "Unsupported input"

    private val morseCodeAlpha: Array<Char> by lazy {
        arrayOf(
            '\u0000', 'E', 'T', 'I', 'A', 'N', 'M', 'S', 'U', 'R',
            'W', 'D', 'K', 'G', 'O', 'H', 'V', 'F', '\u0000', 'L',
            '\u0000', 'P', 'J', 'B', 'X', 'C', 'Y', 'Z', 'Q'
        )
    }

    private val morseCodeExtra: SparseArray<Char> by lazy {
        SparseArray<Char>().apply {
            put(31, '5')
            put(32, '4')
            put(34, '3')
            put(38, '2')
            put(39, '&')
            put(41, '+')
            put(46, '1')
            put(47, '6')
            put(48, '=')
            put(49, '/')
            put(53, '(')
            put(55, '7')
            put(59, '8')
            put(61, '9')
            put(62, '0')
            put(75, '?')
            put(81, '\"')
            put(84, '.')
            put(89, '@')
            put(93, '\'')
            put(96, '-')
            put(106, '!')
            put(108, ')')
            put(114, ',')
            put(119, ':')
        }
    }

    fun textToUnicode(text: String): String {
        val unicode = StringBuilder()

        for (i in 0 until text.length) {
            unicode.append("""\u""")
            val str = Integer.toHexString(text[i].toInt())
            for (j in 0 until 4 - str.length) {
                unicode.append("0")
            }
            unicode.append(str)
        }

        return unicode.toString()
    }

    fun unicodeToText(unicode: String): String {
        val text = StringBuilder()
        val hexes = unicode.split("""\u""")

        if (hexes.size < 2) {
            throw IllegalInputException("""$MESSAGE_ILLEGAL: need "\u"""")
        }

        for (i in 1 until hexes.size) {
            if (hexes[i].length != 4) {
                throw IllegalInputException("""$MESSAGE_ILLEGAL: "\u${hexes[i]}"""")
            }

            val c = Integer.parseInt(hexes[i], 16).toChar()
            text.append(c)
        }

        return text.toString()
    }

    fun textToBase64(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
    }

    fun base64ToText(base64: String): String {
        try {
            val result = Base64.decode(base64, Base64.NO_WRAP)
            for (b in result) {
                if (b < 0) {
                    throw IllegalArgumentException()
                }
            }
            return String(result)
        } catch (e: IllegalArgumentException) {
            throw IllegalInputException("""$MESSAGE_ILLEGAL: bad base64""")
        }
    }

    fun textToMorse(text: String): String {
        val morse = StringBuilder()

        for (c in text) {
            when (c.toInt()) {
                in 38..58, 33, 34, 61, 63, 64 -> {
                    val index = morseCodeExtra.indexOfValue(c)
                    morse.append(generateMorse(morseCodeExtra.keyAt(index)))
                }
                in 65..90, in 97..122 -> {
                    for (i in 1 until morseCodeAlpha.size) {
                        if (morseCodeAlpha[i] == c.toUpperCase()) {
                            morse.append(generateMorse(i))
                            break
                        }
                    }
                }
                10, 32 -> morse.append(' ')
                else -> throw IllegalInputException("""$MESSAGE_UNSUPPORTED: '$c'""")
            }

            morse.append(' ')
        }
        morse.deleteCharAt(morse.length - 1)

        return morse.toString()
    }

    fun morseToText(morse: String): String {
        val text = StringBuilder()
        val words = morse.split("   ")

        for (word in words) {
            val letters = word.split(' ')

            for (letter in letters) {
                if (letter == "") {
                    throw IllegalInputException("""$MESSAGE_ILLEGAL: only 1 or 3 space(s) allowed""")
                }

                var index = 0

                for (i in 0 until letter.length) {
                    index = when (letter[i]) {
                        '.' -> index * 2 + 1
                        '-' -> (index + 1) * 2
                        else -> throw IllegalInputException("""$MESSAGE_ILLEGAL: "$word"""")
                    }
                }

                when (index) {
                    in 1 until morseCodeAlpha.size -> {
                        if (morseCodeAlpha[index] == '\u0000') {
                            throw IllegalInputException("""$MESSAGE_UNSUPPORTED: "$word"""")
                        } else {
                            text.append(morseCodeAlpha[index])
                        }
                    }
                    else -> {
                        val i = morseCodeExtra.indexOfKey(index)
                        if (i < 0) {
                            throw IllegalInputException("""$MESSAGE_UNSUPPORTED: "$word"""")
                        } else {
                            text.append(morseCodeExtra.valueAt(i))
                        }
                    }
                }
            }
            text.append(' ')
        }
        text.deleteCharAt(text.length - 1)

        return text.toString()
    }

    fun textToUrl(text: String): String {
        return Uri.encode(text)
    }

    fun urlToText(uri: String): String {
        return Uri.decode(uri)
    }

    private fun generateMorse(index: Int): String {
        val string = StringBuilder()
        var i = index

        while (i != 0) {
            i = if (i % 2 == 1) {
                string.append('.')
                (i - 1) / 2
            } else {
                string.append('-')
                i / 2 - 1
            }
        }

        return string.reverse().toString()
    }

    class IllegalInputException(message: String) : Exception(message)

}