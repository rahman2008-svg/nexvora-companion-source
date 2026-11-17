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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.edge.gallery.ui.theme.NexVoraColors
import com.google.ai.edge.gallery.ui.theme.NexVoraGradientLabel
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val PAGES =
  listOf(
    OnboardingPage(
      "Welcome to NexVora Companion",
      "Your Private AI. Always With You. An all-in-one AI assistant that runs on your phone.",
    ),
    OnboardingPage(
      "Private Offline AI",
      "After you download a model, chat runs on your device. Your conversations are not sent to any cloud AI service. Downloading models needs internet.",
    ),
    OnboardingPage(
      "Voice and Image Intelligence",
      "Talk to your model or ask questions about photos. Image input needs a multimodal model, and some Android speech services may need a download or internet.",
    ),
    OnboardingPage(
      "Personal Memory and Documents",
      "Save facts you approve and import documents. Everything is stored locally and you can delete it at any time.",
    ),
    OnboardingPage(
      "Model Setup",
      "Open Local AI Mode to download a compatible model. Larger models need more RAM and storage, so pick one that fits your device.",
    ),
    OnboardingPage(
      "Get Started",
      "Permissions such as camera and microphone are requested only when you use those features.",
    ),
  )

/** Six-page onboarding. Calls [onFinished] on Skip or Get Started. */
@Composable
fun NexVoraOnboarding(onFinished: () -> Unit) {
  val pagerState = rememberPagerState(pageCount = { PAGES.size })
  val scope = rememberCoroutineScope()
  val last = pagerState.currentPage == PAGES.lastIndex
  Column(modifier = Modifier.fillMaxSize().background(NexVoraColors.Background).padding(24.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      if (!last) {
        TextButton(onClick = onFinished) { Text("Skip", color = NexVoraColors.TextSecondary) }
      } else {
        Spacer(Modifier.height(48.dp))
      }
    }
    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Box(
          modifier = Modifier.size(96.dp).background(NexVoraColors.BrandGradient, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Text("N", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Text(
          PAGES[page].title,
          color = Color.White,
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
          PAGES[page].body,
          color = NexVoraColors.TextSecondary,
          fontSize = 16.sp,
          lineHeight = 24.sp,
          textAlign = TextAlign.Center,
        )
      }
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.Center,
    ) {
      repeat(PAGES.size) { i ->
        Box(
          modifier =
            Modifier.padding(4.dp)
              .size(if (i == pagerState.currentPage) 10.dp else 8.dp)
              .background(
                if (i == pagerState.currentPage) NexVoraColors.Cyan else NexVoraColors.Outline,
                CircleShape,
              )
        )
      }
    }
    Box(
      modifier =
        Modifier.fillMaxWidth().clickable {
          if (last) onFinished()
          else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
    ) {
      NexVoraGradientLabel(if (last) "Get Started" else "Next")
    }
  }
}
