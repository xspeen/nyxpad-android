package com.nyxpad.app.ui.screens.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.ui.components.NyxEmptyState
import com.nyxpad.app.ui.components.NyxScreenHeader
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBody
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxError
import com.nyxpad.app.ui.theme.NyxTitle
import com.nyxpad.app.utils.DateUtils

@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    var deleteTarget by remember { mutableStateOf<Note?>(null) }
    var showEmptyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NyxScreenHeader(
                title = "Trash",
                subtitle = "Deleted notes stay recoverable",
                onBack = onBack,
                trailing = {
                    if (state.notes.isNotEmpty()) {
                        TextButton(onClick = { showEmptyDialog = true }) {
                            Text(
                                text = "Empty",
                                style = NyxCaption,
                                color = NyxError,
                            )
                        }
                    }
                },
            )

            if (state.notes.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    NyxEmptyState(
                        title = "Trash is empty",
                        subtitle = "Notes you delete wait here before they are gone for good.",
                        actionLabel = "Back to notes",
                        onAction = onBack,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 14.dp,
                        bottom = 40.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        TrashNoteCard(
                            note = note,
                            purgeDays = state.purgeDays,
                            onRestore = { viewModel.restore(note) },
                            onDelete = { deleteTarget = note },
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { note ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete forever?", color = palette.text) },
            text = {
                Text(
                    text = "\"${note.displayTitle}\" will be permanently erased. This cannot be undone.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteForever(note)
                        deleteTarget = null
                    },
                ) {
                    Text("Delete forever", color = NyxError)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Keep it", color = palette.subtext)
                }
            },
        )
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Empty trash?", color = palette.text) },
            text = {
                Text(
                    text = "Every note in Trash will be permanently erased. This cannot be undone.",
                    style = NyxBodySmall,
                    color = palette.subtext,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyDialog = false
                    },
                ) {
                    Text("Empty trash", color = NyxError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("Cancel", color = palette.subtext)
                }
            },
        )
    }
}

@Composable
private fun TrashNoteCard(
    note: Note,
    purgeDays: Int,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.card)
            .border(1.dp, palette.border, shape),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = note.displayTitle,
                    style = NyxTitle.copy(fontSize = 17.sp, lineHeight = 22.sp),
                    color = palette.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = DateUtils.purgeLabel(note.deletedAt, purgeDays),
                    style = NyxCaption,
                    color = accent.copy(alpha = 0.8f),
                )
            }
            val previewText = if (note.isLocked) {
                "Locked note — unlock to read"
            } else {
                note.preview
            }
            if (previewText.isNotBlank()) {
                Text(
                    text = previewText,
                    style = NyxBodySmall,
                    color = palette.subtext,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            Text(
                text = "Deleted ${DateUtils.formatCardDate(note.deletedAt ?: note.updatedAt)}",
                style = NyxCaption,
                color = palette.subtext.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.divider),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onRestore,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestoreFromTrash,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Restore",
                    style = NyxBody,
                    color = accent,
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(palette.divider),
            )
            TextButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    tint = NyxError,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Delete forever",
                    style = NyxBody,
                    color = NyxError,
                )
            }
        }
    }
}
