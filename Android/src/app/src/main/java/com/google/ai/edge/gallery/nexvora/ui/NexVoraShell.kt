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

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.edge.gallery.data.BuiltInTaskId
import com.google.ai.edge.gallery.data.ModelDownloadStatusType
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.nexvora.data.NexVoraDatabase
import com.google.ai.edge.gallery.nexvora.data.NexVoraPrefs
import com.google.ai.edge.gallery.ui.common.tos.AppTosDialog
import com.google.ai.edge.gallery.ui.common.tos.TosViewModel
import com.google.ai.edge.gallery.ui.home.AboutDeveloperDialog
import com.google.ai.edge.gallery.ui.home.SettingsDialog
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import com.google.ai.edge.gallery.ui.theme.NexVoraCard
import com.google.ai.edge.gallery.ui.theme.NexVoraColors
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Tab(val label: String, val icon: ImageVector) {
  Home("Home", Icons.Rounded.Home),
  Chat("AI Chat", Icons.Rounded.Chat),
  Assistant("Assistant", Icons.Rounded.AutoAwesome),
  Library("Library", Icons.Rounded.Folder),
  Settings("Settings", Icons.Rounded.Settings),
}

/**
 * Main NexVora shell: onboarding gate, terms dialog, and the five-tab bottom navigation. Task
 * screens (chat, voice, image, ...) are the existing Gallery screens, opened through
 * [navigateToTaskScreen].
 */
@Composable
fun NexVoraMainShell(
  modelManagerViewModel: ModelManagerViewModel,
  tosViewModel: TosViewModel,
  navigateToTaskScreen: (Task) -> Unit,
  onModelsClicked: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefs = remember { NexVoraPrefs(context) }
  val onboardingDone: Boolean? by prefs.onboardingDone.collectAsState(initial = null)
  var showTos by remember { mutableStateOf(!tosViewModel.getIsTosAccepted()) }
  var tab by rememberSaveable { mutableIntStateOf(0) }
  var overlay by rememberSaveable { mutableStateOf<String?>(null) }
  val uiState by modelManagerViewModel.uiState.collectAsState()

  fun openTask(id: String) {
    val task = uiState.tasks.find { it.id == id }
    if (task != null) navigateToTaskScreen(task)
    else
      Toast.makeText(context, "This feature is not available yet. Models may still be loading.", Toast.LENGTH_LONG)
        .show()
  }

  when (onboardingDone) {
    null -> Column(Modifier.fillMaxSize().background(NexVoraColors.Background)) {}
    false -> NexVoraOnboarding(onFinished = { scope.launch { prefs.setOnboardingDone(true) } })
    true -> {
      BackHandler(enabled = overlay != null) { overlay = null }
      BackHandler(enabled = overlay == null && tab != 0) { tab = 0 }
      if (overlay == "memory") {
        MemoryScreen(onBack = { overlay = null })
      } else {
        Scaffold(
          containerColor = NexVoraColors.Background,
          bottomBar = {
            NavigationBar(containerColor = NexVoraColors.Surface) {
              Tab.entries.forEachIndexed { i, t ->
                NavigationBarItem(
                  selected = tab == i,
                  onClick = { tab = i },
                  icon = { Icon(t.icon, contentDescription = t.label) },
                  label = { Text(t.label, fontSize = 11.sp, maxLines = 1) },
                  colors =
                    NavigationBarItemDefaults.colors(
                      selectedIconColor = Color.White,
                      selectedTextColor = NexVoraColors.Cyan,
                      indicatorColor = NexVoraColors.Purple,
                      unselectedIconColor = NexVoraColors.TextSecondary,
                      unselectedTextColor = NexVoraColors.TextSecondary,
                    ),
                )
              }
            }
          },
        ) { inner ->
          Column(Modifier.padding(inner).fillMaxSize()) {
            when (Tab.entries[tab]) {
              Tab.Home ->
                HomeTab(
                  modelManagerViewModel = modelManagerViewModel,
                  onOpenTask = ::openTask,
                  onOpenMemory = { overlay = "memory" },
                  onOpenDocuments = { tab = Tab.Library.ordinal },
                  onModelsClicked = onModelsClicked,
                )
              Tab.Chat ->
                ActionListTab(
                  title = "AI Chat",
                  subtitle = "Talk to a model running on your device.",
                  items =
                    listOf(
                      ActionItem("Start chat", "Open the on-device chat", Icons.Rounded.Chat) {
                        openTask(BuiltInTaskId.LLM_CHAT)
                      },
                      ActionItem("Choose or download a model", "Local AI Mode", Icons.Rounded.Memory) {
                        onModelsClicked()
                      },
                    ),
                  footer = "Chat history is not saved between sessions yet.",
                )
              Tab.Assistant ->
                ActionListTab(
                  title = "Assistant",
                  subtitle = "Voice, images and text tools. Each uses your local model.",
                  items =
                    listOf(
                      ActionItem("Voice Assistant", "Speak to your model (audio-capable model needed)", Icons.Rounded.Mic) {
                        openTask(BuiltInTaskId.LLM_ASK_AUDIO)
                      },
                      ActionItem("Image Understanding", "Ask about a photo (multimodal model needed)", Icons.Rounded.Image) {
                        openTask(BuiltInTaskId.LLM_ASK_IMAGE)
                      },
                      ActionItem("AI Tools (Prompt Lab)", "Summarize, rewrite and more with prompt templates", Icons.Rounded.TextSnippet) {
                        openTask(BuiltInTaskId.LLM_PROMPT_LAB)
                      },
                      ActionItem("Agent Chat", "Chat with skills", Icons.Rounded.Psychology) {
                        openTask(BuiltInTaskId.LLM_AGENT_CHAT)
                      },
                    ),
                  footer = "",
                )
              Tab.Library -> LibraryTab()
              Tab.Settings ->
                SettingsTab(
                  modelManagerViewModel = modelManagerViewModel,
                  onModelsClicked = onModelsClicked,
                )
            }
          }
        }
      }
    }
  }

  if (showTos) {
    AppTosDialog(onTosAccepted = { showTos = false; tosViewModel.acceptTos() })
  }
}

private data class ActionItem(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val onClick: () -> Unit,
)

@Composable
private fun ActionListTab(title: String, subtitle: String, items: List<ActionItem>, footer: String) {
  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text(subtitle, color = NexVoraColors.TextSecondary, fontSize = 14.sp)
    items.forEach { item ->
      NexVoraCard(Modifier.fillMaxWidth().clickable(onClick = item.onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(item.icon, null, tint = NexVoraColors.Cyan)
          Column(Modifier.padding(start = 14.dp)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, color = NexVoraColors.TextSecondary, fontSize = 12.sp)
          }
        }
      }
    }
    if (footer.isNotEmpty()) Text(footer, color = NexVoraColors.TextSecondary, fontSize = 12.sp)
  }
}

@Composable
private fun HomeTab(
  modelManagerViewModel: ModelManagerViewModel,
  onOpenTask: (String) -> Unit,
  onOpenMemory: () -> Unit,
  onOpenDocuments: () -> Unit,
  onModelsClicked: () -> Unit,
) {
  val context = LocalContext.current
  val prefs = remember { NexVoraPrefs(context) }
  val name by prefs.userName.collectAsState(initial = "")
  val db = remember { NexVoraDatabase.get(context) }
  val docs by db.documentDao().observeAll().collectAsState(initial = emptyList())
  val uiState by modelManagerViewModel.uiState.collectAsState()
  val downloaded =
    uiState.tasks
      .flatMap { it.models }
      .distinctBy { it.name }
      .filter { uiState.modelDownloadStatus[it.name]?.status == ModelDownloadStatusType.SUCCEEDED && it.isLlm }
  val active = downloaded.firstOrNull()
  val freeGb = remember { File(context.filesDir.absolutePath).usableSpace / (1024.0 * 1024 * 1024) }

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      if (name.isBlank()) "Hello 👋" else "Hello, $name 👋",
      color = Color.White,
      fontSize = 26.sp,
      fontWeight = FontWeight.Bold,
    )
    Text("NexVora Companion · Your Private AI. Always With You.", color = NexVoraColors.TextSecondary, fontSize = 13.sp)

    NexVoraCard(Modifier.fillMaxWidth().clickable(onClick = onModelsClicked)) {
      Column(Modifier.padding(16.dp)) {
        Text(
          if (active != null) "AI Ready" else "No model downloaded",
          color = if (active != null) NexVoraColors.Success else NexVoraColors.Cyan,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
        )
        Text(
          if (active != null) "Model: ${active.displayName.ifEmpty { active.name }}" else "Tap to open Local AI Mode and download a model.",
          color = NexVoraColors.TextSecondary,
          fontSize = 13.sp,
        )
        Text(
          "Free storage: ${"%.1f".format(freeGb)} GB · Downloaded models: ${downloaded.size}",
          color = NexVoraColors.TextSecondary,
          fontSize = 12.sp,
        )
      }
    }

    val cards =
      listOf(
        Triple("AI Chat", Icons.Rounded.Chat) { onOpenTask(BuiltInTaskId.LLM_CHAT) },
        Triple("Voice Assistant", Icons.Rounded.Mic) { onOpenTask(BuiltInTaskId.LLM_ASK_AUDIO) },
        Triple("Image Understanding", Icons.Rounded.Image) { onOpenTask(BuiltInTaskId.LLM_ASK_IMAGE) },
        Triple("Documents", Icons.Rounded.Description) { onOpenDocuments() },
        Triple("Personal Memory", Icons.Rounded.Psychology) { onOpenMemory() },
        Triple("Local AI Mode", Icons.Rounded.Memory) { onModelsClicked() },
      )
    cards.chunked(3).forEach { row ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        row.forEach { (label, icon, action) ->
          NexVoraCard(Modifier.weight(1f).clickable(onClick = action)) {
            Column(
              Modifier.padding(vertical = 14.dp, horizontal = 6.dp).fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(icon, null, tint = NexVoraColors.Cyan)
              Text(label, color = Color.White, fontSize = 12.sp, maxLines = 2)
            }
          }
        }
      }
    }

    Text("Recent documents", color = Color.White, fontWeight = FontWeight.SemiBold)
    if (docs.isEmpty()) {
      Text("No documents imported yet.", color = NexVoraColors.TextSecondary, fontSize = 13.sp)
    } else {
      docs.take(3).forEach { d ->
        Text("• ${d.name}", color = NexVoraColors.TextSecondary, modifier = Modifier.clickable(onClick = onOpenDocuments))
      }
    }
    Text("Recent conversations", color = Color.White, fontWeight = FontWeight.SemiBold)
    Text("Conversation history is not stored yet.", color = NexVoraColors.TextSecondary, fontSize = 13.sp)

    NexVoraCard(Modifier.fillMaxWidth().clickable(onClick = onModelsClicked)) {
      Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("Download More Models", color = Color.White, fontWeight = FontWeight.Bold)
          Text("Pick a model that fits your device", color = NexVoraColors.TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.Download, "Download models", tint = NexVoraColors.Cyan)
      }
    }
  }
}

@Composable
private fun SettingsTab(modelManagerViewModel: ModelManagerViewModel, onModelsClicked: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefs = remember { NexVoraPrefs(context) }
  val memoryEnabled by prefs.memoryEnabled.collectAsState(initial = true)
  var showSettings by remember { mutableStateOf(false) }
  var showAbout by remember { mutableStateOf(false) }
  var confirmClear by remember { mutableStateOf(false) }
  var resetOnboarding by remember { mutableStateOf(false) }

  Column(
    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    SettingsRow("App, theme and AI settings", "Theme, Hugging Face token, speech engine, licenses") { showSettings = true }
    SettingsRow("Local AI Mode", "Download, import and manage models", onModelsClicked)
    NexVoraCard(Modifier.fillMaxWidth()) {
      Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("Personal memory", color = Color.White, fontWeight = FontWeight.SemiBold)
          Text("Allow saving memories on this device", color = NexVoraColors.TextSecondary, fontSize = 12.sp)
        }
        Switch(checked = memoryEnabled, onCheckedChange = { v -> scope.launch { prefs.setMemoryEnabled(v) } })
      }
    }
    SettingsRow("Show onboarding again", "Replay the welcome screens") { resetOnboarding = true }
    SettingsRow("Clear local data", "Delete memories, notes, prompts, documents and summaries") { confirmClear = true }
    SettingsRow("About Developer & Company", "Prince AR Abdur Rahman · NexVora Lab's Ofc") { showAbout = true }
    Text(
      "NexVora Companion v1.0.0\n© 2026 NexVora Lab's Ofc. All Rights Reserved.\nBased on Google AI Edge Gallery (Apache License 2.0).",
      color = NexVoraColors.TextSecondary,
      fontSize = 12.sp,
    )
  }

  if (showSettings) {
    SettingsDialog(
      curThemeOverride = modelManagerViewModel.readThemeOverride(),
      curFirebaseAnalytics = modelManagerViewModel.readFirebaseAnalytics(),
      modelManagerViewModel = modelManagerViewModel,
      onDismissed = { showSettings = false },
    )
  }
  if (showAbout) AboutDeveloperDialog(onDismissed = { showAbout = false })
  if (resetOnboarding) {
    AlertDialog(
      onDismissRequest = { resetOnboarding = false },
      title = { Text("Show onboarding again?") },
      confirmButton = {
        TextButton(onClick = { scope.launch { prefs.setOnboardingDone(false) }; resetOnboarding = false }) { Text("Yes") }
      },
      dismissButton = { TextButton(onClick = { resetOnboarding = false }) { Text("Cancel") } },
    )
  }
  if (confirmClear) {
    AlertDialog(
      onDismissRequest = { confirmClear = false },
      title = { Text("Clear all local data?") },
      text = { Text("This permanently deletes your memories, notes, prompts, summaries and imported documents. Downloaded models are kept.") },
      confirmButton = {
        TextButton(
          onClick = {
            confirmClear = false
            scope.launch {
              val db = NexVoraDatabase.get(context)
              db.memoryDao().clear()
              db.noteDao().clear()
              db.promptDao().clear()
              db.summaryDao().clear()
              db.documentDao().clear()
              withContext(Dispatchers.IO) { File(context.filesDir, "documents").deleteRecursively() }
              Toast.makeText(context, "Local data cleared", Toast.LENGTH_SHORT).show()
            }
          }
        ) { Text("Clear") }
      },
      dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
  NexVoraCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Column(Modifier.padding(16.dp)) {
      Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
      Text(subtitle, color = NexVoraColors.TextSecondary, fontSize = 12.sp)
    }
  }
}
