package com.example.monotodo.model

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Normal"),
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

enum class SoundPack(val label: String, val subtitle: String) {
    SOFT("Soft", "Warm muted"),
    CRISP("Crisp", "Micro click"),
    ZEN("Zen", "Airy drop")
}

@Immutable
data class SoundSettings(
    val enabled: Boolean = true,
    val volume: Float = 0.35f,
    val pack: SoundPack = SoundPack.SOFT,
    val onComplete: Boolean = true,
    val onAdd: Boolean = true,
    val onDelete: Boolean = false,
    val onTap: Boolean = false,
    val hapticsEnabled: Boolean = true
)

@Immutable
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
