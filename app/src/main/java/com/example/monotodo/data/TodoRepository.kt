package com.example.monotodo.data

import android.content.Context
import com.example.monotodo.model.Priority
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoItem
import org.json.JSONArray
import org.json.JSONObject

class TodoRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
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
                title = "Tap the circle to complete a task",
                note = "Feel the tactile spring animation, particle sparkle, and chime.",
                priority = Priority.HIGH,
                category = TodoCategory.PERSONAL,
                dueLabel = "Today",
                createdAt = now
            ),
            TodoItem(
                title = "Tap any card to edit or expand notes",
                note = "Customize priority, category, and due tags from the monochrome bottom sheet.",
                priority = Priority.MEDIUM,
                category = TodoCategory.WORK,
                dueLabel = "Tomorrow",
                createdAt = now - 1000L
            ),
            TodoItem(
                title = "Finish all tasks for a 100% burst",
                note = "Toggle sound effects anytime using the speaker button in the top bar.",
                isDone = true,
                priority = Priority.LOW,
                category = TodoCategory.STUDY,
                dueLabel = "This Week",
                createdAt = now - 2000L
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "monotodo_prefs"
        private const val KEY_TASKS = "tasks_json"
        private const val KEY_INITIALIZED = "initialized_v2"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
    }
}
