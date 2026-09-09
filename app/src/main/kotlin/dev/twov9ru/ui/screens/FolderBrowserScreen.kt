package dev.twov9ru.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.twov9ru.ui.theme.Ash
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.Obsidian
import dev.twov9ru.ui.theme.Snow
import dev.twov9ru.ui.theme.WaveAmber
import dev.twov9ru.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "wav", "aac", "ogg", "opus", "m4a")

/**
 * Raw Folder Browser — opened via Long-Press on the 2V9RU Nexus mark.
 *
 * Allows navigating internal storage directly, displaying:
 * - Directories (navigable)
 * - Audio files (.mp3, .flac, .wav, .aac, .ogg, .opus, .m4a)
 *
 * Runs directory listing on IO dispatcher to never block the main thread.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserScreen(
    playerViewModel: PlayerViewModel,
    onDismiss:       () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Obsidian
    ) {
        FolderBrowserContent(
            playerViewModel = playerViewModel,
            onDismiss       = onDismiss
        )
    }
}

@Composable
private fun FolderBrowserContent(
    playerViewModel: PlayerViewModel,
    onDismiss:       () -> Unit
) {
    var currentDir by remember {
        mutableStateOf(Environment.getExternalStorageDirectory())
    }
    val entries = remember { mutableStateListOf<File>() }

    // Load directory contents on the IO dispatcher
    LaunchedEffect(currentDir) {
        val files = withContext(Dispatchers.IO) {
            currentDir.listFiles()
                ?.filter { it.isDirectory || it.extension.lowercase() in AUDIO_EXTENSIONS }
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?: emptyList()
        }
        entries.clear()
        entries.addAll(files)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Header + breadcrumb ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentDir != Environment.getExternalStorageDirectory()) {
                IconButton(onClick = { currentDir = currentDir.parentFile ?: currentDir }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up", tint = Snow)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "RAW BROWSER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Snow
                )
                Text(
                    text     = currentDir.absolutePath,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Ash,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── File list ──────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(entries, key = { it.absolutePath }) { file ->
                FolderEntry(
                    file    = file,
                    onClick = {
                        if (file.isDirectory) {
                            currentDir = file
                        } else {
                            // Phase 2: queue file in playback service
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FolderEntry(file: File, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector  = if (file.isDirectory) Icons.Default.Folder else Icons.Default.AudioFile,
            contentDescription = null,
            tint         = if (file.isDirectory) WaveAmber else Nexus,
            modifier     = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = file.name,
                style    = MaterialTheme.typography.titleMedium,
                color    = Snow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory) {
                Text(
                    text  = file.extension.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ash
                )
            }
        }
    }
}
