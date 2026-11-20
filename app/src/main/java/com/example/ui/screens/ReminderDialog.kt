package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TumbaGradientButton
import com.example.ui.theme.LocalTumbaColors
import com.example.ui.theme.LocalTumbaStrings
import com.example.utils.TextFormatUtils
import java.util.Calendar

/**
 * Offline reminder picker.
 *
 * Both date and time are chosen with the platform pickers (fully localised and
 * available since API 1), while quick presets cover the common cases in one tap.
 * Everything is stored as an epoch millis value on the note.
 */
@Composable
fun ReminderDialog(
    isOpen: Boolean,
    initialTime: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onRemove: () -> Unit
) {
    if (!isOpen) return
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current
    val context = LocalContext.current

    // Default to the next full hour so the picker never opens on a past time.
    val initialCalendar = remember(initialTime, isOpen) {
        Calendar.getInstance().apply {
            if (initialTime != null && initialTime > System.currentTimeMillis()) {
                timeInMillis = initialTime
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }

    var selected by remember(initialTime, isOpen) { mutableStateOf(initialCalendar.timeInMillis) }

    fun setDate(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = selected }
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        selected = cal.timeInMillis
    }

    fun setTime(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = selected }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        selected = cal.timeInMillis
    }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selected }
        DatePickerDialog(
            context,
            { _, year, month, day -> setDate(year, month, day) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000L
        }.show()
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selected }
        TimePickerDialog(
            context,
            { _, hour, minute -> setTime(hour, minute) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    val now = System.currentTimeMillis()
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val presets = remember(startOfToday) {
        listOf(
            strings.reminderPresetHour to now + 60 * 60 * 1000,
            strings.reminderPresetEvening to startOfToday + 20 * 60 * 60 * 1000,
            strings.reminderPresetMorning to startOfToday + 33 * 60 * 60 * 1000
        ).filter { it.second > now }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.warmBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = colors.bgMain,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.reminderTitle,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.reminderSubtitle,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = TextFormatUtils.formatReminderDateTime(selected),
                    color = colors.accentWarm,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PickerTile(
                        icon = { Icon(Icons.Default.CalendarMonth, null, tint = colors.accent, modifier = Modifier.size(16.dp)) },
                        label = strings.reminderPickDate,
                        onClick = { openDatePicker() },
                        modifier = Modifier.weight(1f)
                    )
                    PickerTile(
                        icon = { Icon(Icons.Default.AccessTime, null, tint = colors.accentSecondary, modifier = Modifier.size(16.dp)) },
                        label = strings.reminderPickTime,
                        onClick = { openTimePicker() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.reminderQuickPresets,
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { (label, time) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.bgCard)
                                .border(
                                    0.8.dp,
                                    if (selected == time) colors.accent else colors.borderColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selected = time }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected == time) colors.accent else colors.textSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (selected <= System.currentTimeMillis()) {
                    Text(
                        text = strings.reminderPastTimeWarning,
                        color = colors.danger,
                        fontSize = 10.5.sp
                    )
                }
            }
        },
        confirmButton = {
            TumbaGradientButton(
                text = strings.reminderSave,
                icon = Icons.Default.NotificationsActive,
                onClick = {
                    if (selected > System.currentTimeMillis()) onConfirm(selected)
                },
                modifier = Modifier.testTag("reminder_confirm_button"),
                height = 42.dp
            )
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initialTime != null) {
                    TextButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = colors.danger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.reminderRemove, color = colors.danger, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(strings.cancel, color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun PickerTile(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgCard)
            .border(0.8.dp, colors.borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = colors.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
    }
}
