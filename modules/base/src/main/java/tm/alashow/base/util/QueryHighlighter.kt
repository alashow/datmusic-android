/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.widget.TextView
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * From https://cyrilmottier.com/2017/03/06/highlighting-search-terms/
 */
class QueryHighlighter(
    private var highlightStyle: CharacterStyle = StyleSpan(Typeface.BOLD),
    private val queryNormalizer: QueryNormalizer = QueryNormalizer.FOR_SEARCH,
    private val mode: Mode = Mode.CHARACTERS
) {
    enum class Mode { CHARACTERS, WORDS }

    class QueryNormalizer(private val normalizer: (source: CharSequence) -> CharSequence = { s -> s }) {

        operator fun invoke(source: CharSequence) = normalizer(source)

        companion object {
            val NONE: QueryNormalizer = QueryNormalizer()

            val CASE: QueryNormalizer = QueryNormalizer { source ->
                if (TextUtils.isEmpty(source)) {
                    source
                } else source.toString().uppercase()
            }

            private val PATTERN_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            private val PATTERN_NON_LETTER_DIGIT_TO_SPACES = Pattern.compile("[^\\p{L}\\p{Nd}]")
            val FOR_SEARCH: QueryNormalizer = QueryNormalizer { searchTerm ->
                var result = Normalizer.normalize(searchTerm, Normalizer.Form.NFD)
                result = PATTERN_DIACRITICS.matcher(result).replaceAll("")
                result = PATTERN_NON_LETTER_DIGIT_TO_SPACES.matcher(result).replaceAll(" ")
                result.lowercase()
            }
        }
    }

    fun apply(text: CharSequence, wordPrefix: CharSequence): CharSequence {
        val normalizedText = queryNormalizer(text)
        val normalizedWordPrefix = queryNormalizer(wordPrefix)
        val index = indexOfQuery(normalizedText, normalizedWordPrefix)
        return if (index != -1) {
            SpannableString(text).apply {
                setSpan(highlightStyle, index, index + normalizedWordPrefix.length, 0)
            }
        } else text
    }

    fun apply(view: TextView, text: CharSequence, query: CharSequence) {
        view.text = apply(text, query)
    }

    private fun indexOfQuery(text: CharSequence?, query: CharSequence?): Int {
        if (query == null || text == null) {
            return -1
        }
        val textLength = text.length
        val queryLength = query.length
        if (queryLength == 0 || textLength < queryLength) {
            return -1
        }
        for (i in 0..textLength - queryLength) {
            // Only match word prefixes
            if (mode == Mode.WORDS && i > 0 && text[i - 1] != ' ') {
                continue
            }
            var j = 0
            while (j < queryLength) {
                if (text[i + j] != query[j]) {
                    break
                }
                j++
            }
            if (j == queryLength) {
                return i
            }
        }
        return -1
    }
}
