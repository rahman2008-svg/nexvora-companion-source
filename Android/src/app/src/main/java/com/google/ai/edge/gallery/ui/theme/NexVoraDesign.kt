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

package com.google.ai.edge.gallery.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** NexVora Companion design tokens (from the UI reference). */
object NexVoraColors {
  val Background = Color(0xFF070914)
  val Surface = Color(0xFF111629)
  val SurfaceHigh = Color(0xFF1A2040)
  val Purple = Color(0xFF702CFF)
  val PurpleDeep = Color(0xFF4A1BB8)
  val Cyan = Color(0xFF00D5FF)
  val TextPrimary = Color(0xFFFFFFFF)
  val TextSecondary = Color(0xFFAAB2D5)
  val Outline = Color(0xFF2A3157)
  val Success = Color(0xFF2ECC71)
  val BrandGradient = Brush.linearGradient(listOf(Purple, Cyan))
}

/** Dark glass-style card with a subtle violet border. */
@Composable
fun NexVoraCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(20.dp))
        .background(NexVoraColors.Surface.copy(alpha = 0.92f))
        .border(
          BorderStroke(1.dp, NexVoraColors.Purple.copy(alpha = 0.40f)),
          RoundedCornerShape(20.dp),
        )
  ) {
    content()
  }
}

/** Full-width gradient call-to-action label; wrap in a clickable Box where used. */
@Composable
fun NexVoraGradientLabel(text: String, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp)
        .clip(RoundedCornerShape(26.dp))
        .background(NexVoraColors.BrandGradient)
        .padding(horizontal = 20.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
  }
}
