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

package com.google.ai.edge.gallery.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.ai.edge.gallery.BuildConfig

private val NxBackground = Color(0xFF070914)
private val NxSurface = Color(0xFF111629)
private val NxPurple = Color(0xFF702CFF)
private val NxCyan = Color(0xFF00D5FF)
private val NxTextSecondary = Color(0xFFAAB2D5)

private const val WHATSAPP_1 = "01707424006"
private const val WHATSAPP_2 = "01796951709"
private const val FACEBOOK_URL = "https://www.facebook.com/share/1BNn32qoJo/"
private const val INSTAGRAM_URL = "https://www.instagram.com/ur___abdur____rahman__2008"

/** Full-screen "About Developer" page showing developer, company, technical info and credits. */
@Composable
fun AboutDeveloperDialog(onDismissed: () -> Unit) {
  val uriHandler = LocalUriHandler.current
  Dialog(
    onDismissRequest = onDismissed,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Column(modifier = Modifier.fillMaxSize().background(NxBackground)) {
      Column(
        modifier =
          Modifier.weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Header
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            modifier =
              Modifier.size(72.dp)
                .background(Brush.linearGradient(listOf(NxPurple, NxCyan)), CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("N", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
          }
          Text(
            "About Developer",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            "NexVora Companion",
            color = NxCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
          )
        }

        SectionCard(title = "Prince AR Abdur Rahman", subtitle = "Independent App Developer") {
          BodyText(
            "Independent App Developer passionate about building modern Android applications, " +
              "productivity tools, AI-powered experiences, media players, educational apps, " +
              "and next-generation digital products."
          )
          LabelValue("Contact", "")
          LinkRow("WhatsApp", WHATSAPP_1) { uriHandler.openUri("https://wa.me/880${WHATSAPP_1.removePrefix("0")}") }
          LinkRow("WhatsApp", WHATSAPP_2) { uriHandler.openUri("https://wa.me/880${WHATSAPP_2.removePrefix("0")}") }
          LinkRow("Facebook", FACEBOOK_URL) { uriHandler.openUri(FACEBOOK_URL) }
          LinkRow("Instagram", INSTAGRAM_URL) { uriHandler.openUri(INSTAGRAM_URL) }
        }

        SectionCard(title = "About Company", subtitle = "NexVora Lab's Ofc") {
          BodyText(
            "NexVora Lab's Ofc focuses on creating innovative Android applications designed to " +
              "improve productivity, entertainment, learning, and digital experiences."
          )
          LabelValue(
            "Mission",
            "Build fast, beautiful, privacy-friendly, and user-focused applications accessible " +
              "to everyone.",
          )
        }

        SectionCard(title = "Technical Information") {
          LabelValue("Version", BuildConfig.VERSION_NAME)
        }

        SectionCard(title = "Credits") {
          BodyText("Developed by Prince AR Abdur Rahman")
          BodyText("Published by NexVora Lab's Ofc")
          BodyText("© 2026 NexVora Lab's Ofc. All Rights Reserved.")
          Text(
            "Built on the open-source Google AI Edge Gallery (Apache License 2.0). " +
              "See Third-party libraries in Settings for license notices.",
            color = NxTextSecondary,
            fontSize = 12.sp,
          )
        }
      }

      Button(
        onClick = onDismissed,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NxPurple, contentColor = Color.White),
        shape = RoundedCornerShape(16.dp),
      ) {
        Text("Close")
      }
    }
  }
}

@Composable
private fun SectionCard(
  title: String,
  subtitle: String? = null,
  content: @Composable () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = NxSurface),
    border = BorderStroke(1.dp, NxPurple.copy(alpha = 0.45f)),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
      if (subtitle != null) {
        Text(subtitle, color = NxCyan, fontSize = 13.sp, fontWeight = FontWeight.Medium)
      }
      content()
    }
  }
}

@Composable
private fun BodyText(text: String) {
  Text(text, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun LabelValue(label: String, value: String) {
  Column {
    Text(label, color = NxCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    if (value.isNotEmpty()) {
      Text(value, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
    }
  }
}

@Composable
private fun LinkRow(label: String, value: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      "$label:",
      color = NxTextSecondary,
      fontSize = 13.sp,
      modifier = Modifier.padding(end = 2.dp),
      textAlign = TextAlign.Start,
    )
    Text(value, color = NxCyan, fontSize = 13.sp, modifier = Modifier.weight(1f))
  }
}
