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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.LocalTumbaColors
import com.example.ui.theme.LocalTumbaStrings
import com.example.utils.TextFormatUtils
import java.io.File

/**
 * Home dashboard: quick access to folders plus a chronological history of notes.
 * The notes themselves live in the separate "All notes" section, so this screen is
 * only about orientation and returning to recent work.
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

            HorizontalDivider(color = colors.borderColor, thickness = 0.8.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item(key = "folders_header") {
                    SectionHeader(
                        title = strings.folders.uppercase(),
                        actionLabel = strings.viewAll,
                        onAction = onOpenFolders,
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 14.dp, bottom = 6.dp)
                    )
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
                    SectionHeader(
                        title = strings.noteHistory.uppercase(),
                        actionLabel = strings.viewAll,
                        onAction = onOpenAllNotes,
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 18.dp, bottom = 6.dp)
                    )
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 4.dp)
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

        FloatingActionButton(
            onClick = onNewNote,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("home_fab_new_note"),
            containerColor = colors.textPrimary,
            contentColor = colors.bgMain,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = strings.newNote,
                modifier = Modifier.size(26.dp)
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
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TUMBA NOTES",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = profileNickname.ifBlank { strings.personalKnowledgeBase },
                fontSize = 10.5.sp,
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
                .border(1.dp, colors.accent.copy(alpha = 0.6f), CircleShape)
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
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTumbaColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = colors.textMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = actionLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 4.dp)
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

    Column(
        modifier = Modifier
            .width(136.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgCard)
            .border(0.8.dp, colors.borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = folderTint,
                modifier = Modifier.size(17.dp)
            )
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
            fontSize = 10.5.sp,
            color = colors.textMuted,
            fontFamily = FontFamily.Monospace
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
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgElevated.copy(alpha = 0.4f))
            .border(0.8.dp, colors.borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = strings.newFolder,
            tint = colors.textSecondary,
            modifier = Modifier.size(19.dp)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgCard)
            .border(0.8.dp, colors.borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
    ) {
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.bgElevated)
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
                tint = if (note.isFavorite) colors.accent else colors.textMuted,
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
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.noNotesYet,
            fontSize = 12.5.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.textPrimary)
                .clickable(onClick = onNewNote)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.bgMain,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.createNote,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.bgMain
            )
        }
    }
}
