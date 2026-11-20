package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.LocalTumbaColors
import com.example.utils.RichTextConverter
import kotlin.math.max
import kotlin.math.min

class TumbaWysiwygState {
    var editText: EditText? = null
    var onContentChange: ((String) -> Unit)? = null
    /** True while the buffer is rewritten programmatically, so the watcher stays quiet. */
    var isUpdatingInternally = false

    // Undo / Redo stacks
    val undoStack = mutableListOf<CharSequence>()
    val redoStack = mutableListOf<CharSequence>()

    fun setContent(rawContent: String, textColor: Int, accentColor: Int, mutedColor: Int, codeBgColor: Int) {
        val et = editText ?: return
        isUpdatingInternally = true
        val styled = RichTextConverter.fromFormattedString(
            rawContent = rawContent,
            textColor = textColor,
            accentColor = accentColor,
            mutedColor = mutedColor,
            codeBgColor = codeBgColor
        )
        et.setText(styled)
        et.setSelection(et.text?.length ?: 0)
        isUpdatingInternally = false
    }

    /**
     * Returns the live markdown content of the editor, or null when the editor view
     * has not been attached yet. Callers must treat null as "unknown" instead of
     * falling back to a blank string, otherwise an intentionally emptied note would
     * be silently restored to its previous content.
     */
    fun getContentOrNull(): String? {
        val et = editText ?: return null
        val text = et.text ?: return ""
        return RichTextConverter.toFormattedString(text)
    }

    fun getContent(): String = getContentOrNull() ?: ""

    private fun snapshotForUndo() {
        val et = editText ?: return
        val current = et.text?.toString() ?: return
        if (undoStack.isEmpty() || undoStack.last().toString() != current) {
            undoStack.add(SpannableStringBuilder(et.text))
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
        }
    }

    fun undo() {
        val et = editText ?: return
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(SpannableStringBuilder(et.text))
            isUpdatingInternally = true
            et.setText(prev)
            et.setSelection(et.text?.length ?: 0)
            isUpdatingInternally = false
            onContentChange?.invoke(getContent())
        }
    }

    fun redo() {
        val et = editText ?: return
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(SpannableStringBuilder(et.text))
            isUpdatingInternally = true
            et.setText(next)
            et.setSelection(et.text?.length ?: 0)
            isUpdatingInternally = false
            onContentChange?.invoke(getContent())
        }
    }

    fun toggleBold() {
        val et = editText ?: return
        val text = et.text ?: return
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        if (start == end) return

        snapshotForUndo()
        val existing = text.getSpans(start, end, StyleSpan::class.java).filter { it.style == Typeface.BOLD }
        if (existing.isNotEmpty()) {
            existing.forEach { text.removeSpan(it) }
        } else {
            text.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        et.setSelection(start, end)
        onContentChange?.invoke(getContent())
    }

    fun toggleItalic() {
        val et = editText ?: return
        val text = et.text ?: return
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        if (start == end) return

        snapshotForUndo()
        val existing = text.getSpans(start, end, StyleSpan::class.java).filter { it.style == Typeface.ITALIC }
        if (existing.isNotEmpty()) {
            existing.forEach { text.removeSpan(it) }
        } else {
            text.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        et.setSelection(start, end)
        onContentChange?.invoke(getContent())
    }

    fun toggleStrikethrough() {
        val et = editText ?: return
        val text = et.text ?: return
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        if (start == end) return

        snapshotForUndo()
        val existing = text.getSpans(start, end, StrikethroughSpan::class.java)
        if (existing.isNotEmpty()) {
            existing.forEach { text.removeSpan(it) }
        } else {
            text.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        et.setSelection(start, end)
        onContentChange?.invoke(getContent())
    }

    fun applyHeading(level: Int) {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = et.selectionStart
        val fullString = text.toString()
        val lineStart = fullString.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = fullString.indexOf('\n', cursor).let { if (it == -1) fullString.length else it }

        snapshotForUndo()
        // Remove existing sizes & heading bold on this line
        val oldSizes = text.getSpans(lineStart, lineEnd, RelativeSizeSpan::class.java)
        oldSizes.forEach { text.removeSpan(it) }

        when (level) {
            1 -> {
                text.setSpan(RelativeSizeSpan(1.45f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            2 -> {
                text.setSpan(RelativeSizeSpan(1.28f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            0 -> {
                // Clear headings
                val bolds = text.getSpans(lineStart, lineEnd, StyleSpan::class.java).filter { it.style == Typeface.BOLD }
                bolds.forEach { text.removeSpan(it) }
            }
        }

        et.setSelection(cursor.coerceIn(lineStart, lineEnd))
        onContentChange?.invoke(getContent())
    }

    /**
     * Toggles the checklist marker of the line that contains [targetOffset]
     * (the tapped line), falling back to the caret when no offset is supplied.
     */
    fun toggleChecklist(mutedColor: Int, targetOffset: Int = -1) {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = if (targetOffset in 0..text.length) targetOffset else et.selectionStart
        val fullString = text.toString()
        val lineStart = fullString.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }

        snapshotForUndo()
        val lineText = fullString.substring(lineStart)
        when {
            lineText.startsWith(RichTextConverter.CHECKBOX_UNCHECKED) -> {
                // Toggle to checked
                text.replace(lineStart, lineStart + RichTextConverter.CHECKBOX_UNCHECKED.length, RichTextConverter.CHECKBOX_CHECKED)
                val lineEnd = text.toString().indexOf('\n', lineStart).let { if (it == -1) text.length else it }
                text.setSpan(StrikethroughSpan(), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.setSpan(ForegroundColorSpan(mutedColor), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            lineText.startsWith(RichTextConverter.CHECKBOX_CHECKED) -> {
                // Toggle to unchecked
                text.replace(lineStart, lineStart + RichTextConverter.CHECKBOX_CHECKED.length, RichTextConverter.CHECKBOX_UNCHECKED)
                val lineEnd = text.toString().indexOf('\n', lineStart).let { if (it == -1) text.length else it }
                text.getSpans(lineStart, lineEnd, StrikethroughSpan::class.java).forEach { text.removeSpan(it) }
                text.getSpans(lineStart, lineEnd, ForegroundColorSpan::class.java).forEach { text.removeSpan(it) }
            }
            else -> {
                // Insert new unchecked checkbox at line start
                text.insert(lineStart, RichTextConverter.CHECKBOX_UNCHECKED)
            }
        }
        et.setSelection(min(cursor + RichTextConverter.CHECKBOX_UNCHECKED.length, text.length))
        onContentChange?.invoke(getContent())
    }

    fun insertBulletList(accentColor: Int) {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = et.selectionStart
        val fullString = text.toString()
        val lineStart = fullString.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }

        snapshotForUndo()
        text.insert(lineStart, "• ")
        et.setSelection(min(cursor + 2, text.length))
        onContentChange?.invoke(getContent())
    }

    fun insertNumberedList() {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = et.selectionStart
        val fullString = text.toString()
        val lineStart = fullString.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }

        snapshotForUndo()
        text.insert(lineStart, "1. ")
        et.setSelection(min(cursor + 3, text.length))
        onContentChange?.invoke(getContent())
    }

    fun insertQuote(accentColor: Int) {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = et.selectionStart
        val fullString = text.toString()
        val lineStart = fullString.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = fullString.indexOf('\n', cursor).let { if (it == -1) fullString.length else it }

        snapshotForUndo()
        val existing = text.getSpans(lineStart, lineEnd, QuoteSpan::class.java)
        if (existing.isNotEmpty()) {
            existing.forEach { text.removeSpan(it) }
        } else {
            text.setSpan(QuoteSpan(accentColor), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(StyleSpan(Typeface.ITALIC), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        et.setSelection(cursor)
        onContentChange?.invoke(getContent())
    }

    fun insertCodeBlock(codeBgColor: Int) {
        val et = editText ?: return
        val text = et.text ?: return
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)

        snapshotForUndo()
        if (start == end) {
            val insert = "code"
            text.insert(start, insert)
            text.setSpan(TypefaceSpan("monospace"), start, start + insert.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(BackgroundColorSpan(codeBgColor), start, start + insert.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            et.setSelection(start + insert.length)
        } else {
            text.setSpan(TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(BackgroundColorSpan(codeBgColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            et.setSelection(end)
        }
        onContentChange?.invoke(getContent())
    }

    fun insertDivider() {
        val et = editText ?: return
        val text = et.text ?: return
        val cursor = et.selectionStart
        snapshotForUndo()
        text.insert(cursor, "\n――――――――――――――――――――\n")
        et.setSelection(min(cursor + 22, text.length))
        onContentChange?.invoke(getContent())
    }

    /** Plain text currently selected by the user (empty when the caret is collapsed). */
    fun selectedText(): String {
        val et = editText ?: return ""
        val text = et.text ?: return ""
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        if (end <= start) return ""
        return text.subSequence(start, end).toString()
    }

    /** Accepts "example.com" and turns it into "https://example.com". */
    fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return when {
            trimmed.startsWith("http://", true) ||
                trimmed.startsWith("https://", true) ||
                trimmed.startsWith("mailto:", true) ||
                trimmed.startsWith("tel:", true) ||
                trimmed.startsWith("note://", true) -> trimmed
            trimmed.startsWith("www.", true) -> "https://$trimmed"
            else -> "https://$trimmed"
        }
    }

    /** Updates the URL (and optionally the label) of the link under the caret. */
    fun updateLinkAtSelection(url: String, newLabel: String?, accentColor: Int): Boolean {
        val et = editText ?: return false
        val text = et.text ?: return false
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        val spans = text.getSpans(start, end, URLSpan::class.java)
            .ifEmpty { text.getSpans(start, start, URLSpan::class.java) }
        val span = spans.firstOrNull() ?: return false
        var s = text.getSpanStart(span).coerceAtLeast(0)
        var e = text.getSpanEnd(span).coerceAtMost(text.length)
        if (s >= e) return false

        snapshotForUndo()
        isUpdatingInternally = true
        spans.forEach { text.removeSpan(it) }
        text.getSpans(s, e, ForegroundColorSpan::class.java).forEach { text.removeSpan(it) }
        text.getSpans(s, e, UnderlineSpan::class.java).forEach { text.removeSpan(it) }

        if (!newLabel.isNullOrBlank()) {
            text.replace(s, e, newLabel)
            e = s + newLabel.length
        }
        text.setSpan(URLSpan(url), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(ForegroundColorSpan(accentColor), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(UnderlineSpan(), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        et.setSelection(e)
        isUpdatingInternally = false
        onContentChange?.invoke(getContent())
        return true
    }

    /** Existing link touching the caret or the current selection, if any. */
    fun linkAtSelection(): Pair<String, String>? {
        val et = editText ?: return null
        val text = et.text ?: return null
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        val probe = if (start == end) start else start
        val spans = text.getSpans(probe, probe.coerceAtLeast(probe), URLSpan::class.java)
            .ifEmpty { text.getSpans(start, end, URLSpan::class.java) }
        val span = spans.firstOrNull() ?: return null
        val s = text.getSpanStart(span).coerceAtLeast(0)
        val e = text.getSpanEnd(span).coerceAtMost(text.length)
        if (s >= e) return null
        return text.subSequence(s, e).toString() to span.url
    }

    /** Removes the link styling from the caret/selection without touching the text. */
    fun removeLinkAtSelection(): Boolean {
        val et = editText ?: return false
        val text = et.text ?: return false
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)
        val spans = text.getSpans(start, end, URLSpan::class.java)
            .ifEmpty { text.getSpans(start, start, URLSpan::class.java) }
        if (spans.isEmpty()) return false

        snapshotForUndo()
        spans.forEach { span ->
            val s = text.getSpanStart(span)
            val e = text.getSpanEnd(span)
            text.removeSpan(span)
            // Drop the visual styling that belonged to the link itself.
            text.getSpans(s.coerceAtLeast(0), e.coerceAtLeast(0), UnderlineSpan::class.java)
                .forEach { underline ->
                    if (text.getSpanStart(underline) >= s && text.getSpanEnd(underline) <= e) {
                        text.removeSpan(underline)
                    }
                }
        }
        onContentChange?.invoke(getContent())
        return true
    }

    /**
     * Converts `[label](https://url)` into a clickable label as soon as the user types
     * the trailing space, so no Markdown mode is ever needed. Cursor position, the
     * already-typed space and normal editing keep working because the replacement maps
     * 1:1 onto the visible label.
     */
    fun tryAutoConvertMarkdownLink(accentColor: Int): Boolean {
        val et = editText ?: return false
        if (et.selectionStart != et.selectionEnd) return false
        val text = et.text ?: return false
        val cursor = et.selectionStart
        if (cursor < 4) return false
        val trigger = text[cursor - 1]
        if (trigger != ' ' && trigger != '\n') return false

        val before = text.subSequence(0, cursor - 1).toString()
        val lineStart = before.lastIndexOf('\n') + 1
        val linePart = before.substring(lineStart)
        val match = MARKDOWN_LINK_AT_END.find(linePart) ?: return false
        val label = match.groupValues[1]
        val url = match.groupValues[2]
        if (label.isBlank()) return false

        val replaceStart = lineStart + match.range.first
        val replaceEnd = lineStart + match.range.last + 1

        snapshotForUndo()
        isUpdatingInternally = true
        text.replace(replaceStart, replaceEnd, label)
        val linkEnd = replaceStart + label.length
        text.setSpan(URLSpan(url), replaceStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(ForegroundColorSpan(accentColor), replaceStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(UnderlineSpan(), replaceStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        // Keep the typed trigger character after the label and place the caret after it.
        val next = min(linkEnd + 1, text.length)
        et.setSelection(next)
        isUpdatingInternally = false
        onContentChange?.invoke(getContent())
        return true
    }

    companion object {
        private val MARKDOWN_LINK_AT_END =
            Regex("\\[([^\\[\\]\\n]+)\\]\\(((?:https?://|mailto:)[^\\s()]+)\\)$")
    }

    fun insertLink(title: String, url: String, accentColor: Int) {
        val et = editText ?: return
        val text = et.text ?: return
        val start = min(et.selectionStart, et.selectionEnd)
        val end = max(et.selectionStart, et.selectionEnd)

        val safeUrl = normalizeUrl(url)
        snapshotForUndo()
        if (start == end) {
            text.insert(start, title)
            val linkEnd = start + title.length
            text.setSpan(URLSpan(safeUrl), start, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(ForegroundColorSpan(accentColor), start, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(UnderlineSpan(), start, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            et.setSelection(linkEnd)
        } else {
            text.setSpan(URLSpan(safeUrl), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(ForegroundColorSpan(accentColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            et.setSelection(end)
        }
        onContentChange?.invoke(getContent())
    }
}

@Composable
fun TumbaWysiwygEditor(
    state: TumbaWysiwygState,
    initialContent: String,
    fontSize: Float = 16f,
    fontFamilyStyle: String = "sans",
    placeholderText: String = "",
    overrideTextColor: androidx.compose.ui.graphics.Color? = null,
    overrideMutedColor: androidx.compose.ui.graphics.Color? = null,
    onContentChanged: (String) -> Unit,
    onLinkTapped: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current
    val isLight = colors.isLight

    val primaryTextColorInt = remember(overrideTextColor, colors.textPrimary) {
        val c = overrideTextColor ?: colors.textPrimary
        Color.argb((c.alpha * 255).toInt(), (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
    }
    val accentColorInt = remember(colors.accent) {
        val c = colors.accent
        Color.argb((c.alpha * 255).toInt(), (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
    }
    val mutedColorInt = remember(overrideMutedColor, colors.textMuted) {
        val c = overrideMutedColor ?: colors.textMuted
        Color.argb((c.alpha * 255).toInt(), (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
    }
    val codeBgColorInt = remember(isLight) {
        if (isLight) Color.parseColor("#E8EAED") else Color.parseColor("#26282C")
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            TumbaRichEditText(ctx).apply {
                background = null // Transparent background
                setTextColor(primaryTextColorInt)
                setHintTextColor(mutedColorInt)
                hint = placeholderText
                textSize = fontSize
                gravity = Gravity.TOP or Gravity.START
                imeOptions = EditorInfo.IME_ACTION_NONE
                isVerticalScrollBarEnabled = true
                setLineSpacing(0f, 1.4f)

                typeface = when (fontFamilyStyle) {
                    "serif" -> Typeface.SERIF
                    "mono" -> Typeface.MONOSPACE
                    else -> Typeface.SANS_SERIF
                }

                // Checkbox tap detection
                setOnCheckboxTapListener { tappedLineStart ->
                    state.toggleChecklist(mutedColorInt, tappedLineStart)
                }

                // URL tap detection: internal note links stay in the app, web links leave it
                onLinkClickListener = { url ->
                    if (url.startsWith("note://")) {
                        val targetTitle = url.removePrefix("note://")
                        onLinkTapped?.invoke(targetTitle)
                    } else if (url.isNotBlank()) {
                        runCatching {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                }

                state.editText = this
                state.onContentChange = onContentChanged

                // Load initial formatted content
                state.setContent(
                    rawContent = initialContent,
                    textColor = primaryTextColorInt,
                    accentColor = accentColorInt,
                    mutedColor = mutedColorInt,
                    codeBgColor = codeBgColorInt
                )

                // Text change watcher
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (state.isUpdatingInternally) return
                        // Typing "[Steam](https://...)" + space turns into a link instantly.
                        if (state.tryAutoConvertMarkdownLink(accentColorInt)) return
                        state.onContentChange?.invoke(state.getContent())
                    }
                })
            }
        },
        update = { editText ->
            editText.setTextColor(primaryTextColorInt)
            editText.setHintTextColor(mutedColorInt)
            editText.textSize = fontSize
            editText.typeface = when (fontFamilyStyle) {
                "serif" -> Typeface.SERIF
                "mono" -> Typeface.MONOSPACE
                else -> Typeface.SANS_SERIF
            }
        }
    )
}

class TumbaRichEditText(context: Context) : EditText(context) {

    private var onCheckboxTap: ((Int) -> Unit)? = null
    var onLinkClickListener: ((String) -> Unit)? = null

    fun setOnCheckboxTapListener(listener: (Int) -> Unit) {
        onCheckboxTap = listener
    }

    private var downX = 0f
    private var downY = 0f
    private var downAt = 0L

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downAt = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {
                // Only a genuine, short tap may activate a link: long presses and drags
                // belong to text selection and must never launch the browser.
                val moved = kotlin.math.abs(event.x - downX) + kotlin.math.abs(event.y - downY)
                val isTap = moved < 24f && System.currentTimeMillis() - downAt < 450L
                if (isTap && selectionStart == selectionEnd) {
                    val x = event.x.toInt() - totalPaddingLeft + scrollX
                    val y = event.y.toInt() - totalPaddingTop + scrollY

                    val line = layout?.getLineForVertical(y) ?: -1
                    if (line != -1 && layout != null) {
                        val off = layout?.getOffsetForHorizontal(line, x.toFloat()) ?: -1
                        val text = text
                        if (text != null && off in 0..text.length) {
                            // Check if tapped on a link
                            val urls = text.getSpans(off, off, URLSpan::class.java)
                            if (urls.isNotEmpty()) {
                                val url = urls[0].url
                                if (url != null) {
                                    onLinkClickListener?.invoke(url)
                                    return true
                                }
                            }

                            // Check if tapped on checkbox at start of line
                            val lineStart = layout?.getLineStart(line) ?: 0
                            if (off in lineStart..(lineStart + 3)) {
                                val full = text.toString()
                                if (full.startsWith(RichTextConverter.CHECKBOX_UNCHECKED, lineStart) ||
                                    full.startsWith(RichTextConverter.CHECKBOX_CHECKED, lineStart)
                                ) {
                                    onCheckboxTap?.invoke(lineStart)
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
