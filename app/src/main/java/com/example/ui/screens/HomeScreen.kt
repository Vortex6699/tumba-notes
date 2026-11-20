package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Folder
import com.example.data.model.Note
import com.example.data.model.Tag
import com.example.ui.components.TumbaLogo
import com.example.ui.components.TumbaSectionLabel
import com.example.ui.theme.LocalTumbaColors
import com.example.ui.theme.LocalTumbaStrings
import com.example.utils.NoteColorHelper
import com.example.utils.TextFormatUtils
import java.io.File

/**
 * Home dashboard: a branded hero, quick access to folders and a chronological
 * history of notes. The notes themselves live in the separate "All notes"
 * section, so this screen is only about orientation and returning to recent work.
 */
@Composable
fun HomeScreen(
    notes: List<Note>,
    folders: List<Folder>,
    tags: List<Tag>,
    noteTagsMap: Map<String, List<String>>,
    profileNickname: String,
    profileAvatarPath: String?,
    onSelectNote: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onOpenFolders: () -> Unit,
    onOpenAllNotes: () -> Unit,
    onNewNote: () -> Unit,
    onNewFolder: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onArchiveNote: (String) -> Unit,
    onTrashNote: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDrawer: () -> Unit,
    reminderCount: Int = 0,
    onOpenReminders: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    val activeNotes = remember(notes) {
        notes
            .filter { !it.inTrash && !it.isArchived }
            .sortedByDescending { it.updatedAt }
    }
    val folderMap = remember(folders) { folders.associateBy { it.id } }
    val tagMap = remember(tags) { tags.associateBy { it.id } }
    val noteCounts = remember(notes) {
        val counts = mutableMapOf<String, Int>()
        notes.forEach { note ->
            if (!note.inTrash && !note.isArchived) {
                note.folderId?.let { counts[it] = (counts[it] ?: 0) + 1 }
            }
        }
        counts
    }
    val noteGroups = remember(activeNotes) {
        activeNotes.groupBy { TextFormatUtils.formatDateKey(it.updatedAt) }
    }

    val todayKey = remember { TextFormatUtils.formatDateKey(System.currentTimeMillis()) }
    val yesterdayKey = remember {
        TextFormatUtils.formatDateKey(System.currentTimeMillis() - 24L * 60 * 60 * 1000)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgMain)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                profileNickname = profileNickname,
                profileAvatarPath = profileAvatarPath,
                onOpenProfile = onOpenProfile,
                onOpenSearch = onOpenSearch,
                onOpenDrawer = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item(key = "folders_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 8.dp, top = 14.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TumbaSectionLabel(text = strings.folders, modifier = Modifier.weight(1f))
                        Text(
                            text = strings.viewAll,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenFolders)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                item(key = "folders_row") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "new_folder") {
                            NewFolderCard(onClick = onNewFolder)
                        }
                        items(folders, key = { it.id }) { folder ->
                            FolderCard(
                                folder = folder,
                                noteCount = noteCounts[folder.id] ?: 0,
                                onClick = { onSelectFolder(folder.id) }
                            )
                        }
                    }
                }

                item(key = "history_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TumbaSectionLabel(text = strings.noteHistory, modifier = Modifier.weight(1f))
                        Text(
                            text = strings.viewAll,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenAllNotes)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (activeNotes.isEmpty()) {
                    item(key = "history_empty") {
                        EmptyHistory(onNewNote = onNewNote)
                    }
                } else {
                    noteGroups.forEach { (dateKey, dayNotes) ->
                        item(key = "history_day_$dateKey") {
                            val label = when (dateKey) {
                                todayKey -> strings.today
                                yesterdayKey -> strings.yesterday
                                else -> TextFormatUtils.formatDisplayDate(dateKey)
                            }
                            Text(
                                text = label.uppercase(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted,
                                letterSpacing = 1.3.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(dayNotes, key = { it.id }) { note ->
                            HistoryNoteRow(
                                note = note,
                                folder = note.folderId?.let { folderMap[it] },
                                tag = (noteTagsMap[note.id] ?: emptyList())
                                    .mapNotNull { tagMap[it] }
                                    .firstOrNull(),
                                onOpen = { onSelectNote(note.id) },
                                onToggleFavorite = { onToggleFavorite(note.id) },
                                onTogglePin = { onTogglePin(note.id) },
                                onArchive = { onArchiveNote(note.id) },
                                onTrash = { onTrashNote(note.id) }
                            )
                        }
                    }
                }
            }
        }

        // Gradient floating action button with a soft glow.
        val fabInteraction = remember { MutableInteractionSource() }
        val fabPressed by fabInteraction.collectIsPressedAsState()
        val fabScale by animateFloatAsState(
            targetValue = if (fabPressed) 0.93f else 1f,
            animationSpec = spring(stiffness = 700f, dampingRatio = 0.6f),
            label = "fab"
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .scale(fabScale)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = colors.accent.copy(alpha = 0.6f),
                    spotColor = colors.accent.copy(alpha = 0.7f)
                )
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.brandBrush)
                .clickable(
                    interactionSource = fabInteraction,
                    indication = null,
                    onClick = onNewNote
                )
                .testTag("home_fab_new_note"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = strings.newNote,
                tint = if (colors.isLight) Color.White else colors.bgMain,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    profileNickname: String,
    profileAvatarPath: String?,
    onOpenProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSidebar)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier
                .size(38.dp)
                .testTag("home_menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = colors.textPrimary,
                modifier = Modifier.size(21.dp)
            )
        }

        TumbaLogo(size = 30.dp, cornerRadius = 9.dp)
        Spacer(modifier = Modifier.width(9.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TUMBA NOTES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.6.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = profileNickname.ifBlank { strings.personalKnowledgeBase },
                fontSize = 10.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onOpenSearch,
            modifier = Modifier
                .size(34.dp)
                .testTag("home_search_button")
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = strings.quickSearch,
                tint = colors.textSecondary,
                modifier = Modifier.size(19.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.bgElevated)
                .border(1.2.dp, colors.accent.copy(alpha = 0.7f), CircleShape)
                .clickable(onClick = onOpenProfile)
                .testTag("home_avatar_button"),
            contentAlignment = Alignment.Center
        ) {
            val avatarFile = profileAvatarPath?.let { File(it) }
            if (avatarFile != null && avatarFile.exists()) {
                AsyncImage(
                    model = avatarFile,
                    contentDescription = "Profile Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                val initial = profileNickname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                Text(
                    text = initial,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun GradientAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.7f),
        label = "action"
    )
    Row(
        modifier = modifier
            .height(42.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(colors.brandBrush)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (colors.isLight) Color.White else colors.bgMain,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (colors.isLight) Color.White else colors.bgMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderCard(
    folder: Folder,
    noteCount: Int,
    onClick: () -> Unit
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current
    val folderTint = remember(folder.color) {
        runCatching { Color(android.graphics.Color.parseColor(folder.color)) }
            .getOrDefault(colors.accent)
    }
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .width(140.dp)
            .height(90.dp)
            .shadow(8.dp, shape, ambientColor = folderTint.copy(alpha = 0.3f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        folderTint.copy(alpha = if (colors.isLight) 0.14f else 0.20f),
                        colors.bgCard
                    )
                )
            )
            .border(0.8.dp, folderTint.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(folderTint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = folderTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = folder.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "$noteCount ${strings.notesCountSuffix}",
            fontSize = 10.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun NewFolderCard(onClick: () -> Unit) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    Column(
        modifier = Modifier
            .width(112.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgElevated.copy(alpha = 0.35f))
            .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = strings.newFolder,
            tint = colors.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = strings.newFolder,
            fontSize = 10.5.sp,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryNoteRow(
    note: Note,
    folder: Folder?,
    tag: Tag?,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onTrash: () -> Unit
) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    var showMenu by remember { mutableStateOf(false) }
    val preview = remember(note.content) { TextFormatUtils.toPreviewText(note.content, 2) }
    val isLight = colors.isLight
    val tint = remember(note.color) {
        NoteColorHelper.getEffectiveNoteBg(note.color, isLight, colors.bgCard)
    }
    val accentTint = remember(note.color, isLight) {
        // A saturated version of the note colour drives the little left stripe.
        val c = runCatching { Color(android.graphics.Color.parseColor(note.color)) }.getOrNull()
        if (c == null || note.color.equals("#202124", true) || note.color.equals("#FFFFFF", true)) {
            colors.accent
        } else {
            c.copy(alpha = 0.85f)
        }
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 1000f, dampingRatio = 0.8f),
        label = "note-row"
    )
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .scale(scale)
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentTint.copy(alpha = if (isLight) 0.10f else 0.14f),
                        tint,
                        tint
                    )
                )
            )
            .border(0.7.dp, colors.borderColor.copy(alpha = 0.7f), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onOpen)
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accentTint)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = note.title.ifBlank { strings.untitledNote },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.reminderAt != null && note.reminderAt!! > System.currentTimeMillis()) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = colors.accentWarm,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            if (preview.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = preview,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (folder != null) {
                    val folderTint = runCatching { Color(android.graphics.Color.parseColor(folder.color)) }
                        .getOrDefault(colors.accent)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(folderTint.copy(alpha = 0.14f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = folderTint,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = folder.name,
                            fontSize = 9.5.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
                if (tag != null) {
                    Text(
                        text = "#${tag.name}",
                        fontSize = 9.5.sp,
                        color = colors.textMuted,
                        maxLines = 1
                    )
                }
                Text(
                    text = TextFormatUtils.formatDate(note.updatedAt),
                    fontSize = 9.5.sp,
                    color = colors.textMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Favorite",
                tint = if (note.isFavorite) colors.accentWarm else colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(30.dp)
                    .testTag("home_note_menu_${note.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = colors.textMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(colors.bgElevated)
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.PushPin, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                    },
                    text = {
                        Text(
                            text = if (note.isPinned) strings.unpin else strings.pinToTop,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    },
                    onClick = {
                        onTogglePin()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Archive, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                    },
                    text = { Text(strings.archiveNote, fontSize = 12.sp, color = colors.textPrimary) },
                    onClick = {
                        onArchive()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Delete, null, tint = colors.danger, modifier = Modifier.size(16.dp))
                    },
                    text = { Text(strings.moveToTrash, fontSize = 12.sp, color = colors.danger) },
                    onClick = {
                        // Removing from the home feed is a real trash action, so the note
                        // disappears immediately and can still be restored from Trash.
                        onTrash()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory(onNewNote: () -> Unit) {
    val colors = LocalTumbaColors.current
    val strings = LocalTumbaStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colors.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.noNotesYet,
            fontSize = 12.5.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        GradientAction(
            label = strings.createNote,
            icon = Icons.Default.Add,
            onClick = onNewNote
        )
    }
}
