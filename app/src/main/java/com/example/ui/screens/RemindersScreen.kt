package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.components.TumbaGradientButton
import com.example.ui.components.TumbaLogo
import com.example.ui.components.TumbaSectionLabel
import com.example.ui.theme.LocalTumbaColors
import com.example.ui.theme.LocalTumbaStrings
import com.example.utils.TextFormatUtils

/**
 * All active (future) reminders in one place: edit the time, remove the
 * reminder, or jump straight into the note.
 */
@Composable
fun RemindersScreen(
    reminders: List<Note>,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onOpenDrawer: () -> Unit,
    onEditReminder: (Note) -> Unit,
    onRemoveReminder: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    val now = System.currentTimeMillis()
    val active = remember(reminders, now) {
        reminders
            .filter { !it.inTrash && (it.reminderAt ?: 0L) > now }
            .sortedBy { it.reminderAt }
    }

    val grouped = remember(active) {
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = startOfToday + 24L * 60 * 60 * 1000
        val endOfTomorrow = endOfToday + 24L * 60 * 60 * 1000
        active.groupBy { note ->
            val at = note.reminderAt ?: 0L
            when {
                at < endOfToday -> "today"
                at < endOfTomorrow -> "tomorrow"
                else -> "later"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgMain)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSidebar)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colors.textPrimary, modifier = Modifier.size(21.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.reminders,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${active.size} ${strings.remindersActiveSuffix}",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.accentWarm.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = colors.accentWarm,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            if (active.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TumbaLogo(size = 74.dp, cornerRadius = 22.dp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = strings.noRemindersYet,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.noRemindersHint,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    TumbaGradientButton(
                        text = strings.newNote,
                        icon = Icons.Default.Add,
                        onClick = onNewNote
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (bucket, notes) ->
                        item(key = "reminder_header_$bucket") {
                            val label = when (bucket) {
                                "today" -> strings.today
                                "tomorrow" -> strings.tomorrow
                                else -> strings.reminderLater
                            }
                            TumbaSectionLabel(
                                text = label,
                                accent = colors.accentWarm,
                                modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 2.dp)
                            )
                        }
                        items(notes, key = { it.id }) { note ->
                            ReminderCard(
                                note = note,
                                onClick = { onOpenNote(note.id) },
                                onEdit = { onEditReminder(note) },
                                onRemove = { onRemoveReminder(note) }
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ReminderCard(
    note: Note,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current
    val preview = remember(note.content) { TextFormatUtils.toPreviewText(note.content, 2) }
    val time = note.reminderAt ?: 0L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgCard)
            .border(0.8.dp, colors.borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.linearGradient(
                        listOf(colors.accentWarm.copy(alpha = 0.26f), colors.accent.copy(alpha = 0.10f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = colors.accentWarm,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifBlank { strings.untitledNote },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (preview.isNotBlank()) {
                Text(
                    text = preview,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = colors.accentWarm,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = TextFormatUtils.formatReminderDateTime(time),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentWarm
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = TextFormatUtils.relativeReminderTime(time),
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textMuted
                )
            }
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.EditCalendar,
                contentDescription = strings.reminderEdit,
                tint = colors.textSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = strings.reminderRemove,
                tint = colors.danger,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
