package com.example.monotodo.data

import android.content.Context
import com.example.monotodo.model.Priority
import com.example.monotodo.model.SoundPack
import com.example.monotodo.model.SoundSettings
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoItem
import org.json.JSONArray
import org.json.JSONObject

class TodoRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSoundSettings(): SoundSettings {
        val packName = prefs.getString(KEY_SOUND_PACK, SoundPack.SOFT.name) ?: SoundPack.SOFT.name
        val pack = runCatching { SoundPack.valueOf(packName) }.getOrDefault(SoundPack.SOFT)
        return SoundSettings(
            enabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            volume = prefs.getFloat(KEY_SOUND_VOLUME, 0.35f),
            pack = pack,
            onComplete = prefs.getBoolean(KEY_SOUND_ON_COMPLETE, true),
            onAdd = prefs.getBoolean(KEY_SOUND_ON_ADD, true),
            onDelete = prefs.getBoolean(KEY_SOUND_ON_DELETE, false),
            onTap = prefs.getBoolean(KEY_SOUND_ON_TAP, false),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        )
    }

    fun saveSoundSettings(s: SoundSettings) {
        prefs.edit()
            .putBoolean(KEY_SOUND_ENABLED, s.enabled)
            .putFloat(KEY_SOUND_VOLUME, s.volume)
            .putString(KEY_SOUND_PACK, s.pack.name)
            .putBoolean(KEY_SOUND_ON_COMPLETE, s.onComplete)
            .putBoolean(KEY_SOUND_ON_ADD, s.onAdd)
            .putBoolean(KEY_SOUND_ON_DELETE, s.onDelete)
            .putBoolean(KEY_SOUND_ON_TAP, s.onTap)
            .putBoolean(KEY_HAPTICS_ENABLED, s.hapticsEnabled)
            .apply()
    }

    fun loadTasks(): List<TodoItem> {
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            val starter = defaultStarterTasks()
            saveTasks(starter)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
            return starter
        }

        val raw = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        TodoItem(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            note = obj.optString("note", ""),
                            isDone = obj.optBoolean("isDone", false),
                            priority = runCatching {
                                Priority.valueOf(obj.optString("priority", Priority.MEDIUM.name))
                            }.getOrDefault(Priority.MEDIUM),
                            category = runCatching {
                                TodoCategory.valueOf(obj.optString("category", TodoCategory.PERSONAL.name))
                            }.getOrDefault(TodoCategory.PERSONAL),
                            dueLabel = obj.optString("dueLabel", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTasks(tasks: List<TodoItem>) {
        val array = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("note", task.note)
                put("isDone", task.isDone)
                put("priority", task.priority.name)
                put("category", task.category.name)
                put("dueLabel", task.dueLabel)
                put("createdAt", task.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_TASKS, array.toString()).apply()
    }

    private fun defaultStarterTasks(): List<TodoItem> {
        val now = System.currentTimeMillis()
        return listOf(
            TodoItem(
                title = "Tap the circle to finish a task",
                note = "Smooth spring animation with subtle sound.",
                priority = Priority.HIGH,
                category = TodoCategory.PERSONAL,
                dueLabel = "Today",
                createdAt = now
            ),
            TodoItem(
                title = "Customize subtle sounds in top-right settings",
                note = "Switch between Soft, Crisp, or Zen styles and adjust volume.",
                priority = Priority.MEDIUM,
                category = TodoCategory.WORK,
                dueLabel = "Tomorrow",
                createdAt = now - 1000L
            ),
            TodoItem(
                title = "Tap any task to edit or add notes",
                isDone = true,
                priority = Priority.LOW,
                category = TodoCategory.STUDY,
                createdAt = now - 2000L
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "monotodo_prefs"
        private const val KEY_TASKS = "tasks_json"
        private const val KEY_INITIALIZED = "initialized_v4"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_SOUND_PACK = "sound_pack"
        private const val KEY_SOUND_ON_COMPLETE = "sound_on_complete"
        private const val KEY_SOUND_ON_ADD = "sound_on_add"
        private const val KEY_SOUND_ON_DELETE = "sound_on_delete"
        private const val KEY_SOUND_ON_TAP = "sound_on_tap"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    }
}
