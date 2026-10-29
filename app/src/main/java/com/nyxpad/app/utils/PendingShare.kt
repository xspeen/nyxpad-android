package com.nyxpad.app.utils

/**
 * A tiny hand-off between the launching Activity and a brand-new editor. It is
 * deliberately memory-only: shared text is consumed immediately and never
 * written to disk unless the user saves the note.
 */
object PendingShare {
    private var title: String? = null
    private var text: String? = null

    fun set(title: String?, text: String?) {
        this.title = title
        this.text = text
    }

    fun consume(): Pair<String?, String?>? {
        val value = if (title == null && text == null) null else title to text
        title = null
        text = null
        return value
    }
}
