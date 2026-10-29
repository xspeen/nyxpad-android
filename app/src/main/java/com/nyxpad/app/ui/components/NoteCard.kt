package com.nyxpad.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxpad.app.domain.model.Note
import com.nyxpad.app.domain.model.WritingMood
import com.nyxpad.app.ui.theme.LocalNyxAccent
import com.nyxpad.app.ui.theme.LocalNyxPalette
import com.nyxpad.app.ui.theme.NyxBodySmall
import com.nyxpad.app.ui.theme.NyxCaption
import com.nyxpad.app.ui.theme.NyxTitle
import com.nyxpad.app.ui.theme.tagColor
import com.nyxpad.app.utils.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isGrid: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNyxPalette.current
    val accent = LocalNyxAccent.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "noteCardScale",
    )
    val shape = RoundedCornerShape(16.dp)
    val cardColor = if (note.colorTag == com.nyxpad.app.domain.model.NoteColorTag.Default) {
        palette.tagDefault
    } else {
        tagColor(note.colorTag)
    }

    if (isGrid) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 154.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = accent.copy(alpha = 0.08f),
                    spotColor = accent.copy(alpha = 0.12f),
                )
                .clip(shape)
                .background(cardColor)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongPress,
                )
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = note.displayTitle,
                    style = NyxTitle.copy(fontSize = 18.sp, lineHeight = 23.sp),
                    color = palette.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (note.isPinned) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pinned",
                        tint = accent,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            if (note.preview.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.preview,
                    style = NyxBodySmall,
                    color = palette.subtext,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note.moodTag != WritingMood.None) {
                    WritingMoodBadge(mood = note.moodTag)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = DateUtils.formatCardDate(note.updatedAt),
                    style = NyxCaption,
                    color = palette.subtext,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (note.wordCount == 1) "1 word" else "${note.wordCount} words",
                    style = NyxCaption,
                    color = palette.subtext,
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = accent.copy(alpha = 0.06f),
                    spotColor = accent.copy(alpha = 0.10f),
                )
                .clip(shape)
                .background(cardColor)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongPress,
                ),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (note.colorTag == com.nyxpad.app.domain.model.NoteColorTag.Default) accent.copy(alpha = 0.35f) else tagColor(note.colorTag)),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.displayTitle,
                        style = NyxTitle.copy(fontSize = 18.sp, lineHeight = 23.sp),
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "Pinned",
                            tint = accent,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                if (note.preview.isNotBlank()) {
                    Text(
                        text = note.preview,
                        style = NyxBodySmall,
                        color = palette.subtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = DateUtils.formatCardDate(note.updatedAt),
                        style = NyxCaption,
                        color = palette.subtext,
                    )
                    if (note.moodTag != WritingMood.None) {
                        WritingMoodBadge(mood = note.moodTag)
                    }
                    Text(
                        text = if (note.wordCount == 1) "1 word" else "${note.wordCount} words",
                        style = NyxCaption,
                        color = palette.subtext,
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = palette.subtext.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 12.dp)
                    .size(18.dp),
            )
        }
    }
}
