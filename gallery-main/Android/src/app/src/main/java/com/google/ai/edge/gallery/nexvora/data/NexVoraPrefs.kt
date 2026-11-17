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

package com.google.ai.edge.gallery.nexvora.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.nexVoraStore by preferencesDataStore(name = "nexvora_prefs")

/** NexVora app preferences (Preferences DataStore). */
class NexVoraPrefs(private val context: Context) {
  private val onboardingKey = booleanPreferencesKey("onboarding_done")
  private val memoryKey = booleanPreferencesKey("memory_enabled")
  private val userNameKey = stringPreferencesKey("user_name")

  /** null while loading, then true/false. */
  val onboardingDone: Flow<Boolean> = context.nexVoraStore.data.map { it[onboardingKey] ?: false }
  val memoryEnabled: Flow<Boolean> = context.nexVoraStore.data.map { it[memoryKey] ?: true }
  val userName: Flow<String> = context.nexVoraStore.data.map { it[userNameKey] ?: "" }

  suspend fun setOnboardingDone(done: Boolean) {
    context.nexVoraStore.edit { it[onboardingKey] = done }
  }

  suspend fun setMemoryEnabled(enabled: Boolean) {
    context.nexVoraStore.edit { it[memoryKey] = enabled }
  }

  suspend fun setUserName(name: String) {
    context.nexVoraStore.edit { it[userNameKey] = name.trim() }
  }
}
