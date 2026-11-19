package com.example.monotodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monotodo.data.TodoRepository
import com.example.monotodo.model.Priority
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoFilter
import com.example.monotodo.model.TodoItem
import com.example.monotodo.ui.theme.MonoPalette
import com.example.monotodo.ui.theme.MonoTodoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        val repository = TodoRepository(applicationContext)
        setContent {
            MonoTodoTheme {
                MonoTodoScreen(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MonoTodoScreen(repository: TodoRepository) {
    var tasks by remember { mutableStateOf(repository.loadTasks()) }
    var selectedFilter by remember { mutableStateOf(TodoFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tasks) {
        repository.saveTasks(tasks)
    }

    val filteredTasks = remember(tasks, selectedFilter, searchQuery) {
        tasks
            .filter { task ->
                when (selectedFilter) {
                    TodoFilter.ALL -> true
                    TodoFilter.ACTIVE -> !task.isDone
                    TodoFilter.DONE -> task.isDone
                }
            }
            .filter { task ->
                val q = searchQuery.trim()
                q.isEmpty() ||
                    task.title.contains(q, ignoreCase = true) ||
                    task.note.contains(q, ignoreCase = true) ||
                    task.category.label.contains(q, ignoreCase = true)
            }
            .sortedWith(
                compareBy<TodoItem> { it.isDone }
                    .thenByDescending { it.priority.ordinal }
                    .thenByDescending { it.createdAt }
            )
    }

    val totalCount = tasks.size
    val doneCount = remember(tasks) { tasks.count { it.isDone } }
    val activeCount = totalCount - doneCount
    val targetProgress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "progressFill"
    )

    // Subtle breathing shift along the black-to-white monochrome gradient
    val infiniteTransition = rememberInfiniteTransition(label = "monoGradient")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    val backgroundBrush = Brush.verticalGradient(
        0.00f to MonoPalette.PureBlack,
        (0.22f + gradientShift * 0.05f) to MonoPalette.Obsidian,
        (0.45f + gradientShift * 0.06f) to Color(0xFF2B2B30),
        (0.72f + gradientShift * 0.05f) to Color(0xFFAEAEB4),
        0.92f to MonoPalette.OffWhite,
        1.00f to MonoPalette.PureWhite
    )

    val fabScale by animateFloatAsState(
        targetValue = if (showEditor) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        showEditor = true
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .scale(fabScale),
                    shape = CircleShape,
                    containerColor = MonoPalette.PureBlack,
                    contentColor = MonoPalette.PureWhite,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add task",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Monochrome Obsidian Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MONOTODO",
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 3.sp,
                            color = MonoPalette.Silver
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Focus List",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoPalette.PureWhite
                        )
                    }

                    AnimatedVisibility(
                        visible = doneCount > 0,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut() + scaleOut(targetScale = 0.85f)
                    ) {
                        Surface(
                            onClick = {
                                val removed = tasks.filter { it.isDone }
                                tasks = tasks.filterNot { it.isDone }
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Cleared ${removed.size} completed task(s)",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        tasks = tasks + removed
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                        ) {
                            Text(
                                text = "Clear Done",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MonoPalette.PureWhite,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Progress Bar & Counter Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = "$activeCount active  ·  $doneCount completed",
                                transitionSpec = {
                                    (slideInVertically { it / 2 } + fadeIn())
                                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                                },
                                label = "countAnim"
                            ) { summary ->
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MonoPalette.Mist
                                )
                            }

                            val percent = (animatedProgress * 100).toInt()
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MonoPalette.PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(MonoPalette.Silver, MonoPalette.PureWhite)
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search tasks...",
                            color = MonoPalette.Silver.copy(alpha = 0.8f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MonoPalette.Silver
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MonoPalette.PureWhite
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MonoPalette.PureWhite,
                        unfocusedTextColor = MonoPalette.PureWhite,
                        focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        focusedBorderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                        cursorColor = MonoPalette.PureWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Animated Segmented Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TodoFilter.entries.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                MonoPalette.PureWhite
                            } else {
                                Color.Black.copy(alpha = 0.28f)
                            },
                            animationSpec = tween(240),
                            label = "pillBg"
                        )
                        val pillText by animateColorAsState(
                            targetValue = if (isSelected) {
                                MonoPalette.PureBlack
                            } else {
                                MonoPalette.Mist
                            },
                            animationSpec = tween(240),
                            label = "pillText"
                        )

                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(50),
                            color = pillBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MonoPalette.PureWhite else Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = filter.label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = pillText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Task List or Animated Empty State
                if (filteredTasks.isEmpty()) {
                    EmptyMonoState(isCompletelyEmpty = tasks.isEmpty())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 2.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            AnimatedMonoTaskCard(
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                                task = task,
                                onToggle = {
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(isDone = !it.isDone) else it
                                    }
                                },
                                onEdit = {
                                    editingTask = task
                                    showEditor = true
                                },
                                onDelete = {
                                    tasks = tasks.filterNot { it.id == task.id }
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Removed "${task.title}"",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            tasks = tasks + task
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showEditor) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showEditor = false },
                sheetState = sheetState,
                containerColor = MonoPalette.Obsidian,
                contentColor = MonoPalette.PureWhite,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                MonoTaskEditorSheet(
                    initialTask = editingTask,
                    onCancel = { showEditor = false },
                    onSave = { title, note, priority, category, dueLabel ->
                        val current = editingTask
                        tasks = if (current == null) {
                            listOf(
                                TodoItem(
                                    title = title,
                                    note = note,
                                    priority = priority,
                                    category = category,
                                    dueLabel = dueLabel
                                )
                            ) + tasks
                        } else {
                            tasks.map {
                                if (it.id == current.id) {
                                    it.copy(
                                        title = title,
                                        note = note,
                                        priority = priority,
                                        category = category,
                                        dueLabel = dueLabel
                                    )
                                } else {
                                    it
                                }
                            }
                        }
                        showEditor = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyMonoState(isCompletelyEmpty: Boolean) {
    val pulse = rememberInfiniteTransition(label = "emptyPulse")
    val iconScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 64.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .scale(iconScale),
                shape = CircleShape,
                color = MonoPalette.PureBlack.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = MonoPalette.PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isCompletelyEmpty) "All clear" else "No matching tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MonoPalette.PureBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isCompletelyEmpty) {
                    "Tap + to add a new task."
                } else {
                    "Try a different filter or search keyword."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MonoPalette.Graphite
            )
        }
    }
}

@Composable
private fun AnimatedMonoTaskCard(
    modifier: Modifier = Modifier,
    task: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.72f else 0.96f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (task.isDone) 0.dp else 4.dp,
        animationSpec = tween(300),
        label = "cardElevation"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (task.isDone) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )
    val checkBg by animateColorAsState(
        targetValue = if (task.isDone) MonoPalette.PureBlack else Color.Transparent,
        animationSpec = tween(250),
        label = "checkBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) MonoPalette.Mist else MonoPalette.PureWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isDone) MonoPalette.Silver else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(checkBg)
                    .border(
                        width = 2.dp,
                        color = if (task.isDone) MonoPalette.PureBlack else MonoPalette.Graphite,
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = task.isDone,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MonoPalette.PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isDone) MonoPalette.SlateGray else MonoPalette.PureBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MonoPalette.Graphite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isHigh = task.priority == Priority.HIGH && !task.isDone
                    MonoTag(
                        text = task.priority.label,
                        inverted = isHigh
                    )
                    MonoTag(text = task.category.label, inverted = false)
                    if (task.dueLabel.isNotBlank()) {
                        MonoTag(text = task.dueLabel, inverted = false)
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MonoPalette.SlateGray
                )
            }
        }
    }
}

@Composable
private fun MonoTag(text: String, inverted: Boolean) {
    Surface(
        color = if (inverted) MonoPalette.PureBlack else MonoPalette.Mist,
        shape = RoundedCornerShape(8.dp),
        border = if (inverted) null else BorderStroke(0.8.dp, MonoPalette.Silver)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (inverted) MonoPalette.PureWhite else MonoPalette.Charcoal,
            fontWeight = if (inverted) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonoTaskEditorSheet(
    initialTask: TodoItem?,
    onCancel: () -> Unit,
    onSave: (String, String, Priority, TodoCategory, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title.orEmpty()) }
    var note by remember { mutableStateOf(initialTask?.note.orEmpty()) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(initialTask?.category ?: TodoCategory.PERSONAL) }
    var dueLabel by remember { mutableStateOf(initialTask?.dueLabel.orEmpty()) }

    val dueOptions = listOf("", "Today", "Tomorrow", "This Week")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initialTask == null) "New Task" else "Edit Task",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MonoPalette.PureWhite
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MonoPalette.PureWhite,
                unfocusedTextColor = MonoPalette.PureWhite,
                focusedBorderColor = MonoPalette.PureWhite,
                unfocusedBorderColor = MonoPalette.Graphite,
                focusedLabelColor = MonoPalette.PureWhite,
                unfocusedLabelColor = MonoPalette.Silver,
                cursorColor = MonoPalette.PureWhite
            )
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Notes (optional)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MonoPalette.PureWhite,
                unfocusedTextColor = MonoPalette.PureWhite,
                focusedBorderColor = MonoPalette.PureWhite,
                unfocusedBorderColor = MonoPalette.Graphite,
                focusedLabelColor = MonoPalette.PureWhite,
                unfocusedLabelColor = MonoPalette.Silver,
                cursorColor = MonoPalette.PureWhite
            )
        )

        Text(
            text = "PRIORITY",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MonoPalette.Silver
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { option ->
                SelectableMonoChip(
                    label = option.label,
                    selected = priority == option,
                    onClick = { priority = option }
                )
            }
        }

        Text(
            text = "CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MonoPalette.Silver
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoCategory.entries.forEach { option ->
                SelectableMonoChip(
                    label = option.label,
                    selected = category == option,
                    onClick = { category = option }
                )
            }
        }

        Text(
            text = "DUE",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MonoPalette.Silver
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dueOptions.forEach { option ->
                SelectableMonoChip(
                    label = option.ifEmpty { "None" },
                    selected = dueLabel == option,
                    onClick = { dueLabel = option }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = MonoPalette.Silver)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = title.trim().isNotEmpty(),
                onClick = {
                    onSave(title.trim(), note.trim(), priority, category, dueLabel)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonoPalette.PureWhite,
                    contentColor = MonoPalette.PureBlack,
                    disabledContainerColor = MonoPalette.Graphite,
                    disabledContentColor = MonoPalette.Silver
                )
            ) {
                Text(
                    text = if (initialTask == null) "Add Task" else "Save Changes",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SelectableMonoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) MonoPalette.PureWhite else MonoPalette.Charcoal,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) MonoPalette.PureBlack else MonoPalette.Mist,
        animationSpec = tween(200),
        label = "chipFg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, if (selected) MonoPalette.PureWhite else MonoPalette.Graphite)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
