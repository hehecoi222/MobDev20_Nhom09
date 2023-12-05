package com.mobdev20.nhom09.quicknote.helpers

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText

class TextProcessor {
    companion object {
        fun convertFormat(string: String): Spannable {
            var reduceString = string.replace("[#*_@!%~]".toRegex(), "")
            var spannable = SpannableStringBuilder(reduceString)

            // Chữ đậm **
            var patternBold = Regex("\\*(.*?)\\*")
            var matchesBold = patternBold.findAll(string)

            var wordArrayBold = mutableListOf<String>()
            for (match in matchesBold) {
                wordArrayBold.add(match.groupValues[1])
            }

            if (wordArrayBold.isNotEmpty()) {
                for (word in wordArrayBold) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Chữ nghiêng ##
            var patternItalic = Regex("#(.*?)#")
            var matchesItalic = patternItalic.findAll(string)

            var wordArrayItalic = mutableListOf<String>()
            for (match in matchesItalic) {
                wordArrayItalic.add(match.groupValues[1])
            }

            if (wordArrayItalic.isNotEmpty()) {
                for (word in wordArrayItalic) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Gạch chân __
            var patternUnderline = Regex("\\_(.*?)\\_")
            var matchesUnderline = patternUnderline.findAll(string)

            var wordArrayUnderline = mutableListOf<String>()
            for (match in matchesUnderline) {
                wordArrayUnderline.add(match.groupValues[1])
            }

            if (wordArrayUnderline.isNotEmpty()) {
                for (word in wordArrayUnderline) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Chữ đậm, nghiêng @@
            var patternBoldItalic = Regex("@(.*?)@")
            var matchesBoldItalic = patternBoldItalic.findAll(string)

            var wordArrayBoldItalic = mutableListOf<String>()
            for (match in matchesBoldItalic) {
                wordArrayBoldItalic.add(match.groupValues[1])
            }

            if (wordArrayBoldItalic.isNotEmpty()) {
                for (word in wordArrayBoldItalic) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD_ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Chữ đậm, gạch chân !!
            var patternBoldUnderline = Regex("!(.*?)!")
            var matchesBoldUnderline = patternBoldUnderline.findAll(string)

            var wordArrayBoldUnderline = mutableListOf<String>()
            for (match in matchesBoldUnderline) {
                wordArrayBoldUnderline.add(match.groupValues[1])
            }

            if (wordArrayBoldUnderline.isNotEmpty()) {
                for (word in wordArrayBoldUnderline) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Chữ nghiêng, gạch chân %%
            var patternItalicUnderline = Regex("%(.*?)%")
            var matchesItalicUnderline = patternItalicUnderline.findAll(string)

            var wordArrayItalicUnderline = mutableListOf<String>()
            for (match in matchesItalicUnderline) {
                wordArrayItalicUnderline.add(match.groupValues[1])
            }

            if (wordArrayItalicUnderline.isNotEmpty()) {
                for (word in wordArrayItalicUnderline) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            // Chữ đậm, nghiêng, gạch chân ~~
            var patternAll = Regex("~(.*?)~")
            var matchesAll = patternAll.findAll(string)

            var wordArrayAll = mutableListOf<String>()
            for (match in matchesAll) {
                wordArrayAll.add(match.groupValues[1])
            }

            if (wordArrayAll.isNotEmpty()) {
                for (word in wordArrayAll) {
                    var start = reduceString.indexOf(word)
                    if (start != -1) {
                        var end = start + word.length
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD_ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            return spannable
        }

        fun setFormat(noteBody: EditText, style: Int): String {
            val start = noteBody.selectionStart
            val end = noteBody.selectionEnd
            val string = noteBody.text.substring(start, end)
            val spannable = SpannableStringBuilder(noteBody.text)

            if (start != end) {
                val hasBold = hasStyleSpan(spannable, start, end, Typeface.BOLD)
                val hasItalic = hasStyleSpan(spannable, start, end, Typeface.ITALIC)
                val hasUnderline = hasUnderlineSpan(spannable, start, end)

                if (style == Typeface.BOLD) {
                    if (hasBold) {
                        if (hasItalic && hasUnderline) {
                            spannable.replace(start, end, "%${string}%")
                        } else if (hasItalic) {
                            spannable.replace(start, end, "#${string}#")
                        } else if (hasUnderline) {
                            spannable.replace(start, end, "_${string}_")
                        }
                    } else {
                        if (hasItalic && hasUnderline) {
                            spannable.replace(start, end, "~${string}~")
                        } else if (hasItalic) {
                            spannable.replace(start, end, "@${string}@")
                        } else if (hasUnderline) {
                            spannable.replace(start, end, "!${string}!")
                        } else {
                            spannable.replace(start, end, "*${string}*")
                        }
                    }
                } else if (style == Typeface.ITALIC) {
                    if (hasItalic) {
                        if (hasBold && hasUnderline) {
                            spannable.replace(start, end, "!${string}!")
                        } else if (hasBold) {
                            spannable.replace(start, end, "*${string}*")
                        } else if (hasUnderline) {
                            spannable.replace(start, end, "_${string}_")
                        }
                    } else {
                        if (hasBold && hasUnderline) {
                            spannable.replace(start, end, "~${string}~")
                        } else if (hasBold) {
                            spannable.replace(start, end, "@${string}@")
                        } else if (hasUnderline) {
                            spannable.replace(start, end, "%${string}%")
                        } else {
                            spannable.replace(start, end, "#${string}#")
                        }
                    }
                } else {
                    if (hasUnderline) {
                        if (hasBold && hasItalic) {
                            spannable.replace(start, end, "@${string}@")
                        } else if (hasBold) {
                            spannable.replace(start, end, "*${string}*")
                        } else if (hasItalic) {
                            spannable.replace(start, end, "#${string}#")
                        }
                    } else {
                        if (hasBold && hasItalic) {
                            spannable.replace(start, end, "~${string}~")
                        } else if (hasBold) {
                            spannable.replace(start, end, "!${string}!")
                        } else if (hasItalic) {
                            spannable.replace(start, end, "%${string}%")
                        } else {
                            spannable.replace(start, end, "_${string}_")
                        }
                    }
                }

            }

            return spannable.toString()
        }

        private fun hasStyleSpan(spannable: Spannable, start: Int, end: Int, style: Int): Boolean {
            val styleSpans = spannable.getSpans(start, end, StyleSpan::class.java)
            return styleSpans.any { it.style == style }
        }

        private fun hasUnderlineSpan(spannable: Spannable, start: Int, end: Int): Boolean {
            val underlineSpans = spannable.getSpans(start, end, UnderlineSpan::class.java)
            return underlineSpans.isNotEmpty()
        }

        fun renderFormat(noteBody: EditText, style: Int) {
            val start = noteBody.selectionStart
            val end = noteBody.selectionEnd
            val spannable = SpannableString(noteBody.text)

            if (start != end) {
                var exist = false

                if (style != 3) {
                    spannable.getSpans(start, end, StyleSpan::class.java)?.forEach {
                        val currentStyle = it.style
                        if (currentStyle == style) {
                            spannable.removeSpan(it)
                            exist = true
                        }
                    }
                } else {
                    spannable.getSpans(start, end, UnderlineSpan::class.java)?.forEach {
                        spannable.removeSpan(it)
                        exist = true
                    }
                }

                if (!exist) {
                    if (style != 3) {
                        spannable.setSpan(
                            StyleSpan(style),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                noteBody.setText(spannable)
                noteBody.setSelection(end)
            }
        }
    }
}