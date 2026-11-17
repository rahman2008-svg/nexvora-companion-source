/*
 * Copyright 2026 NexVora Lab's Ofc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.nexvora.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.edge.gallery.nexvora.data.DocumentEntity
import com.google.ai.edge.gallery.nexvora.data.MemoryEntity
import com.google.ai.edge.gallery.nexvora.data.NexVoraDatabase
import com.google.ai.edge.gallery.nexvora.data.NexVoraPrefs
import com.google.ai.edge.gallery.nexvora.data.NoteEntity
import com.google.ai.edge.gallery.nexvora.data.SavedPromptEntity
import com.google.ai.edge.gallery.nexvora.data.SummaryEntity
import com.google.ai.edge.gallery.ui.theme.NexVoraCard
import com.google.ai.edge.gallery.ui.theme.NexVoraColors
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_DOC_BYTES = 25L * 1024 * 1024
private const val PREVIEW_CHARS = 4000

private fun fmt(ts: Long): String =
  DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ts))

private fun shareText(context: Context, title: String, body: String) {
  val send =
    Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, body)
    }
  context.startActivity(Intent.createChooser(send, title))
}

/** Full-screen Personal Memory page with a back arrow. */
@Composable
fun MemoryScreen(onBack: () -> Unit) {
  Column(modifier = Modifier.fillMaxSize().background(NexVoraColors.Background)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White)
      }
      Text("Personal Memory", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
    MemoryContent(modifier = Modifier.weight(1f))
  }
}

@Composable
fun MemoryContent(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val dao = remember { NexVoraDatabase.get(context).memoryDao() }
  val prefs = remember { NexVoraPrefs(context) }
  val memories by dao.observeAll().collectAsState(initial = emptyList())
  val enabled by prefs.memoryEnabled.collectAsState(initial = true)
  var query by remember { mutableStateOf("") }
  var editing by remember { mutableStateOf<MemoryEntity?>(null) }
  var adding by remember { mutableStateOf(false) }
  var confirmClear by remember { mutableStateOf(false) }
  val shown =
    memories.filter {
      query.isBlank() ||
        it.text.contains(query, ignoreCase = true) ||
        it.category.contains(query, ignoreCase = true)
    }

  Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    NexVoraCard(Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(Modifier.weight(1f)) {
          Text("Memory enabled", color = Color.White, fontWeight = FontWeight.SemiBold)
          Text(
            "Stored only on this device. Nothing is saved unless you add it. Using memories inside chat prompts is not connected yet.",
            color = NexVoraColors.TextSecondary,
            fontSize = 12.sp,
          )
        }
        Switch(checked = enabled, onCheckedChange = { v -> scope.launch { prefs.setMemoryEnabled(v) } })
      }
    }
    OutlinedTextField(
      value = query,
      onValueChange = { query = it },
      label = { Text("Search memories") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      TextButton(onClick = { adding = true }) {
        Icon(Icons.Rounded.Add, null, tint = NexVoraColors.Cyan)
        Text(" Add memory", color = NexVoraColors.Cyan)
      }
      TextButton(onClick = { confirmClear = true }, enabled = memories.isNotEmpty()) {
        Text("Clear all", color = NexVoraColors.TextSecondary)
      }
    }
    if (shown.isEmpty()) {
      Text(
        if (memories.isEmpty()) "No memories yet. Tap Add memory to save a fact you want to keep."
        else "No memories match your search.",
        color = NexVoraColors.TextSecondary,
        modifier = Modifier.padding(8.dp),
      )
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(shown, key = { it.id }) { m ->
        NexVoraCard(Modifier.fillMaxWidth()) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(m.text, color = Color.White, fontSize = 15.sp)
              Text("${m.category} · ${fmt(m.updatedAt)}", color = NexVoraColors.TextSecondary, fontSize = 12.sp)
            }
            IconButton(onClick = { editing = m }) {
              Icon(Icons.Rounded.Edit, "Edit memory", tint = NexVoraColors.Cyan)
            }
            IconButton(onClick = { scope.launch { dao.delete(m.id) } }) {
              Icon(Icons.Rounded.Delete, "Delete memory", tint = NexVoraColors.TextSecondary)
            }
          }
        }
      }
    }
  }

  if (adding || editing != null) {
    val cur = editing
    var text by remember(cur, adding) { mutableStateOf(cur?.text ?: "") }
    var category by remember(cur, adding) { mutableStateOf(cur?.category ?: "General") }
    AlertDialog(
      onDismissRequest = { adding = false; editing = null },
      title = { Text(if (cur == null) "Add memory" else "Edit memory") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(text, { text = it }, label = { Text("Fact to remember") })
          OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true)
        }
      },
      confirmButton = {
        TextButton(
          enabled = text.isNotBlank(),
          onClick = {
            val t = text.trim()
            val c = category.trim().ifEmpty { "General" }
            scope.launch {
              if (cur == null) dao.insert(MemoryEntity(text = t, category = c))
              else dao.update(cur.copy(text = t, category = c, updatedAt = System.currentTimeMillis()))
            }
            adding = false
            editing = null
          },
        ) { Text("Save") }
      },
      dismissButton = { TextButton(onClick = { adding = false; editing = null }) { Text("Cancel") } },
    )
  }
  if (confirmClear) {
    AlertDialog(
      onDismissRequest = { confirmClear = false },
      title = { Text("Clear all memories?") },
      text = { Text("This permanently deletes every saved memory on this device.") },
      confirmButton = {
        TextButton(onClick = { scope.launch { dao.clear() }; confirmClear = false }) { Text("Clear all") }
      },
      dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
    )
  }
}

private enum class Section(val label: String) {
  Memories("Memories"),
  Notes("Notes"),
  Prompts("Prompts"),
  Documents("Documents"),
  Summaries("Summaries"),
}

private data class LibItem(val id: Long, val title: String, val body: String, val time: Long)

/** Library tab: local notes, prompts, documents, summaries and memories. */
@Composable
fun LibraryTab(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var section by remember { mutableStateOf(Section.Memories) }
  Column(modifier = modifier.fillMaxSize().background(NexVoraColors.Background)) {
    Text(
      "Library",
      color = Color.White,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(16.dp),
    )
    Row(
      modifier = Modifier.horizontalScrollCompat().padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Section.entries.forEach { s ->
        FilterChip(selected = section == s, onClick = { section = s }, label = { Text(s.label) })
      }
    }
    when (section) {
      Section.Memories -> MemoryContent(modifier = Modifier.weight(1f).padding(top = 8.dp))
      Section.Documents -> DocumentsContent(modifier = Modifier.weight(1f))
      else -> TextItemsContent(section, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun Modifier.horizontalScrollCompat(): Modifier =
  this.then(Modifier.horizontalScroll(rememberScrollState()))

@Composable
private fun TextItemsContent(section: Section, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val db = remember { NexVoraDatabase.get(context) }
  val libItems by
    remember(section) {
        when (section) {
          Section.Notes -> kotlinx.coroutines.flow.flow {
              db.noteDao().observeAll().collect { l -> emit(l.map { LibItem(it.id, it.title, it.body, it.createdAt) }) }
            }
          Section.Prompts -> kotlinx.coroutines.flow.flow {
              db.promptDao().observeAll().collect { l -> emit(l.map { LibItem(it.id, it.title, it.body, it.createdAt) }) }
            }
          else -> kotlinx.coroutines.flow.flow {
              db.summaryDao().observeAll().collect { l -> emit(l.map { LibItem(it.id, it.title, it.body, it.createdAt) }) }
            }
        }
      }
      .collectAsState(initial = emptyList())
  var query by remember(section) { mutableStateOf("") }
  var adding by remember(section) { mutableStateOf(false) }
  var viewing by remember(section) { mutableStateOf<LibItem?>(null) }
  val shown =
    libItems.filter {
      query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true)
    }

  Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(
      query,
      { query = it },
      label = { Text("Search ${section.label.lowercase()}") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    TextButton(onClick = { adding = true }) {
      Icon(Icons.Rounded.Add, null, tint = NexVoraColors.Cyan)
      Text(" Add ${section.label.dropLast(1).lowercase()}", color = NexVoraColors.Cyan)
    }
    if (shown.isEmpty()) {
      Text("Nothing here yet.", color = NexVoraColors.TextSecondary)
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(shown, key = { it.id }) { item ->
        NexVoraCard(Modifier.fillMaxWidth().clickable { viewing = item }) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
              Text(item.body, color = NexVoraColors.TextSecondary, fontSize = 13.sp, maxLines = 2)
              Text(fmt(item.time), color = NexVoraColors.TextSecondary, fontSize = 11.sp)
            }
            IconButton(onClick = { shareText(context, item.title, item.body) }) {
              Icon(Icons.Rounded.Share, "Share", tint = NexVoraColors.Cyan)
            }
            IconButton(
              onClick = {
                scope.launch {
                  when (section) {
                    Section.Notes -> db.noteDao().delete(item.id)
                    Section.Prompts -> db.promptDao().delete(item.id)
                    else -> db.summaryDao().delete(item.id)
                  }
                }
              }
            ) { Icon(Icons.Rounded.Delete, "Delete", tint = NexVoraColors.TextSecondary) }
          }
        }
      }
    }
  }

  if (adding) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { adding = false },
      title = { Text("Add ${section.label.dropLast(1).lowercase()}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
          OutlinedTextField(body, { body = it }, label = { Text("Text") })
        }
      },
      confirmButton = {
        TextButton(
          enabled = title.isNotBlank() && body.isNotBlank(),
          onClick = {
            val t = title.trim()
            val b = body.trim()
            scope.launch {
              when (section) {
                Section.Notes -> db.noteDao().insert(NoteEntity(title = t, body = b))
                Section.Prompts -> db.promptDao().insert(SavedPromptEntity(title = t, body = b))
                else -> db.summaryDao().insert(SummaryEntity(title = t, body = b))
              }
            }
            adding = false
          },
        ) { Text("Save") }
      },
      dismissButton = { TextButton(onClick = { adding = false }) { Text("Cancel") } },
    )
  }
  viewing?.let { v ->
    AlertDialog(
      onDismissRequest = { viewing = null },
      title = { Text(v.title) },
      text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text(v.body) } },
      confirmButton = { TextButton(onClick = { viewing = null }) { Text("Close") } },
    )
  }
}

@Composable
private fun DocumentsContent(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val db = remember { NexVoraDatabase.get(context) }
  val docs by db.documentDao().observeAll().collectAsState(initial = emptyList())
  var message by remember { mutableStateOf<String?>(null) }
  var previewDoc by remember { mutableStateOf<DocumentEntity?>(null) }
  var previewText by remember { mutableStateOf("") }

  val picker =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
      if (uri != null) {
        scope.launch {
          message =
            withContext(Dispatchers.IO) {
              try {
                val cr = context.contentResolver
                val mime = cr.getType(uri) ?: "application/octet-stream"
                val supported = mime == "application/pdf" || mime.startsWith("text/")
                if (!supported) return@withContext "Unsupported file type: $mime. Import PDF or text files."
                var name = "document"
                var size = -1L
                cr.query(uri, null, null, null, null)?.use { c ->
                  if (c.moveToFirst()) {
                    val ni = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val si = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (ni >= 0) name = c.getString(ni) ?: name
                    if (si >= 0 && !c.isNull(si)) size = c.getLong(si)
                  }
                }
                if (size > MAX_DOC_BYTES) return@withContext "File is larger than 25 MB."
                val dir = File(context.filesDir, "documents").apply { mkdirs() }
                val dest = File(dir, "${System.currentTimeMillis()}_${name.replace('/', '_')}")
                var copied = 0L
                cr.openInputStream(uri)?.use { input ->
                  dest.outputStream().use { out ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                      val n = input.read(buf)
                      if (n < 0) break
                      copied += n
                      if (copied > MAX_DOC_BYTES) {
                        dest.delete()
                        return@withContext "File is larger than 25 MB."
                      }
                      out.write(buf, 0, n)
                    }
                  }
                } ?: return@withContext "Could not open the file."
                db.documentDao().insert(DocumentEntity(name = name, mimeType = mime, sizeBytes = copied, filePath = dest.absolutePath))
                "Imported $name"
              } catch (e: Exception) {
                "Import failed: ${e.message}"
              }
            }
        }
      }
    }

  Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    TextButton(onClick = { picker.launch(arrayOf("application/pdf", "text/*")) }) {
      Icon(Icons.Rounded.Add, null, tint = NexVoraColors.Cyan)
      Text(" Import PDF or text file", color = NexVoraColors.Cyan)
    }
    Text(
      "Files are copied into app-private storage. AI summarization and PDF text extraction are not implemented yet; text files can be previewed.",
      color = NexVoraColors.TextSecondary,
      fontSize = 12.sp,
    )
    message?.let { Text(it, color = NexVoraColors.Cyan, fontSize = 13.sp) }
    if (docs.isEmpty()) Text("No documents imported.", color = NexVoraColors.TextSecondary)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(docs, key = { it.id }) { d ->
        NexVoraCard(
          Modifier.fillMaxWidth().clickable {
            previewDoc = d
            previewText = ""
            scope.launch {
              previewText =
                withContext(Dispatchers.IO) {
                  val f = File(d.filePath)
                  when {
                    !f.exists() -> "The file is no longer available on this device."
                    d.mimeType.startsWith("text/") ->
                      f.bufferedReader().use { r ->
                        val buf = CharArray(PREVIEW_CHARS)
                        val n = r.read(buf)
                        if (n <= 0) "(empty)" else String(buf, 0, n)
                      }
                    else -> "PDF text extraction is not available yet."
                  }
                }
            }
          }
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(d.name, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
              Text("${d.sizeBytes / 1024} KB · ${fmt(d.createdAt)}", color = NexVoraColors.TextSecondary, fontSize = 12.sp)
            }
            IconButton(
              onClick = {
                scope.launch {
                  withContext(Dispatchers.IO) { File(d.filePath).delete() }
                  db.documentDao().delete(d.id)
                }
              }
            ) { Icon(Icons.Rounded.Delete, "Delete document", tint = NexVoraColors.TextSecondary) }
          }
        }
      }
    }
  }
  previewDoc?.let { d ->
    AlertDialog(
      onDismissRequest = { previewDoc = null },
      title = { Text(d.name) },
      text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text(previewText.ifEmpty { "Loading…" }) } },
      confirmButton = { TextButton(onClick = { previewDoc = null }) { Text("Close") } },
    )
  }
}
