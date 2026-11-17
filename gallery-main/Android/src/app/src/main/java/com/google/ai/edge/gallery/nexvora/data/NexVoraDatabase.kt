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
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** A user-approved fact. Stored locally only; never changes model weights. */
@Entity(tableName = "memories")
data class MemoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val text: String,
  val category: String = "General",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val body: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "saved_prompts")
data class SavedPromptEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val body: String,
  val createdAt: Long = System.currentTimeMillis(),
)

/** Document metadata only. The file itself lives in app-private storage ([filePath]). */
@Entity(tableName = "documents")
data class DocumentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val mimeType: String,
  val sizeBytes: Long,
  val filePath: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "summaries")
data class SummaryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val documentId: Long? = null,
  val title: String,
  val body: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface MemoryDao {
  @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
  fun observeAll(): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories WHERE text LIKE '%' || :q || '%' OR category LIKE '%' || :q || '%' ORDER BY updatedAt DESC LIMIT :limit")
  suspend fun search(q: String, limit: Int): List<MemoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: MemoryEntity): Long

  @Update suspend fun update(m: MemoryEntity)

  @Query("DELETE FROM memories WHERE id = :id") suspend fun delete(id: Long)

  @Query("DELETE FROM memories") suspend fun clear()
}

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY createdAt DESC") fun observeAll(): Flow<List<NoteEntity>>

  @Insert suspend fun insert(n: NoteEntity): Long

  @Query("DELETE FROM notes WHERE id = :id") suspend fun delete(id: Long)

  @Query("DELETE FROM notes") suspend fun clear()
}

@Dao
interface PromptDao {
  @Query("SELECT * FROM saved_prompts ORDER BY createdAt DESC")
  fun observeAll(): Flow<List<SavedPromptEntity>>

  @Insert suspend fun insert(p: SavedPromptEntity): Long

  @Query("DELETE FROM saved_prompts WHERE id = :id") suspend fun delete(id: Long)

  @Query("DELETE FROM saved_prompts") suspend fun clear()
}

@Dao
interface DocumentDao {
  @Query("SELECT * FROM documents ORDER BY createdAt DESC")
  fun observeAll(): Flow<List<DocumentEntity>>

  @Insert suspend fun insert(d: DocumentEntity): Long

  @Query("DELETE FROM documents WHERE id = :id") suspend fun delete(id: Long)

  @Query("DELETE FROM documents") suspend fun clear()
}

@Dao
interface SummaryDao {
  @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
  fun observeAll(): Flow<List<SummaryEntity>>

  @Insert suspend fun insert(s: SummaryEntity): Long

  @Query("DELETE FROM summaries WHERE id = :id") suspend fun delete(id: Long)

  @Query("DELETE FROM summaries") suspend fun clear()
}

@Database(
  entities =
    [
      MemoryEntity::class,
      NoteEntity::class,
      SavedPromptEntity::class,
      DocumentEntity::class,
      SummaryEntity::class,
    ],
  version = 1,
  exportSchema = true,
)
abstract class NexVoraDatabase : RoomDatabase() {
  abstract fun memoryDao(): MemoryDao

  abstract fun noteDao(): NoteDao

  abstract fun promptDao(): PromptDao

  abstract fun documentDao(): DocumentDao

  abstract fun summaryDao(): SummaryDao

  companion object {
    @Volatile private var instance: NexVoraDatabase? = null

    fun get(context: Context): NexVoraDatabase =
      instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                NexVoraDatabase::class.java,
                "nexvora.db",
              )
              .build()
              .also { instance = it }
        }
  }
}
