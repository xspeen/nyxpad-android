package com.nyxpad.app.domain.model

/**
 * Templates that ship with NyxPad. They are never written to the database, so
 * they stay available even after a user clears their data.
 */
data class BuiltInTemplate(
    val name: String,
    val title: String,
    val content: String,
)

object BuiltInTemplates {
    val all: List<BuiltInTemplate> = listOf(
        BuiltInTemplate(
            name = "Blank note",
            title = "",
            content = "",
        ),
        BuiltInTemplate(
            name = "Daily journal",
            title = "Daily journal",
            content = "Today I noticed…\n\n\nWhat I want to remember:\n\n\n" +
                "One thing I am letting go of:\n\n",
        ),
        BuiltInTemplate(
            name = "Three good things",
            title = "Three good things",
            content = "1. \n2. \n3. \n\nWhy they mattered:\n\n",
        ),
        BuiltInTemplate(
            name = "Meeting notes",
            title = "Meeting notes",
            content = "Attendees:\n\nAgenda:\n- \n\nDecisions:\n- \n\n" +
                "Action items:\n- [ ] \n",
        ),
        BuiltInTemplate(
            name = "Story idea",
            title = "Story idea",
            content = "The spark\n\n\n\nThe world\n\n\n\nThe turn\n\n\n",
        ),
        BuiltInTemplate(
            name = "Brain dump",
            title = "Brain dump",
            content = "Everything on my mind right now:\n\n- \n- \n- \n",
        ),
        BuiltInTemplate(
            name = "Weekly review",
            title = "Weekly review",
            content = "What went well\n\n\n\nWhat I learned\n\n\n\nNext week\n\n\n",
        ),
    )
}
