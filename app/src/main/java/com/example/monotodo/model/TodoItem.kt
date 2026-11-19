package com.example.monotodo.model

import java.util.UUID

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class TodoCategory(val label: String) {
    PERSONAL("Personal"),
    WORK("Work"),
    STUDY("Study"),
    ERRANDS("Errands")
}

enum class TodoFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DONE("Done")
}

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: TodoCategory = TodoCategory.PERSONAL,
    val dueLabel: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
