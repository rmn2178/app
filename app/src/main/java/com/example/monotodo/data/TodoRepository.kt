package com.example.monotodo.data

import android.content.Context
import com.example.monotodo.model.Priority
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoItem
import org.json.JSONArray
import org.json.JSONObject

class TodoRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
                note = "Watch the smooth spring check animation and progress bar update.",
                priority = Priority.HIGH,
                category = TodoCategory.PERSONAL,
                dueLabel = "Today",
                createdAt = now
            ),
            TodoItem(
                title = "Tap any card to edit details",
                note = "Adjust priority, category, or due tag from the animated bottom sheet.",
                priority = Priority.MEDIUM,
                category = TodoCategory.WORK,
                dueLabel = "Tomorrow",
                createdAt = now - 1000L
            ),
            TodoItem(
                title = "Try filtering by All, Active, or Done",
                note = "Everything is saved offline automatically on your device.",
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
        private const val KEY_INITIALIZED = "initialized_v1"
    }
}
