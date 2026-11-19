package com.example.monotodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monotodo.audio.SoundEffectPlayer
import com.example.monotodo.data.TodoRepository
import com.example.monotodo.model.Priority
import com.example.monotodo.model.SoundPack
import com.example.monotodo.model.SoundSettings
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoFilter
import com.example.monotodo.model.TodoItem
import com.example.monotodo.ui.components.MinimalProgressHairline
import com.example.monotodo.ui.components.MonochromeGradientBackdrop
import com.example.monotodo.ui.theme.MonoPalette
import com.example.monotodo.ui.theme.MonoTodoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundPlayer = remember { SoundEffectPlayer(context.applicationContext) }

    var soundSettings by remember { mutableStateOf(repository.loadSoundSettings()) }
    var tasks by remember { mutableStateOf(repository.loadTasks()) }
    var selectedFilter by remember { mutableStateOf(TodoFilter.ALL) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var showSoundSettings by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val todayLabel = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
    }

    // Load WAV sound assets off the UI thread so startup is instant
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            soundPlayer.preload()
        }
    }

    // Persist sound settings off the UI thread
    LaunchedEffect(soundSettings) {
        soundPlayer.settings = soundSettings
        withContext(Dispatchers.IO + NonCancellable) {
            repository.saveSoundSettings(soundSettings)
        }
    }

    // Persist tasks off the UI thread so tapping checkboxes never drops a frame
    LaunchedEffect(tasks) {
        withContext(Dispatchers.IO + NonCancellable) {
            repository.saveTasks(tasks)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    fun triggerHaptic(type: HapticFeedbackType) {
        if (soundSettings.hapticsEnabled) {
            haptic.performHapticFeedback(type)
        }
    }

    val filteredTasks by remember(tasks, selectedFilter, searchQuery) {
        derivedStateOf {
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
    }

    val totalCount by remember(tasks) { derivedStateOf { tasks.size } }
    val doneCount by remember(tasks) { derivedStateOf { tasks.count { it.isDone } } }
    val activeCount by remember(tasks) { derivedStateOf { tasks.count { !it.isDone } } }
    val progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()

    val fabRotation by animateFloatAsState(
        targetValue = if (showEditor) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabRotation"
    )

    val sheetBrush = remember {
        Brush.verticalGradient(
            0.0f to Color(0xFFF3F3F6),
            0.55f to Color(0xFFF9F9FB),
            1.0f to MonoPalette.PureWhite
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MonochromeGradientBackdrop()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        triggerHaptic(HapticFeedbackType.TextHandleMove)
                        soundPlayer.playTap()
                        editingTask = null
                        showEditor = true
                    },
                    modifier = Modifier.navigationBarsPadding(),
                    shape = CircleShape,
                    containerColor = MonoPalette.PureBlack,
                    contentColor = MonoPalette.PureWhite,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add task",
                        modifier = Modifier
                            .size(25.dp)
                            .graphicsLayer { rotationZ = fabRotation }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Clean Obsidian Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = todayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 2.sp,
                                color = MonoPalette.Silver
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MonoPalette.PureWhite
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderIconButton(
                                icon = if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (searchOpen) "Close search" else "Search tasks",
                                active = searchOpen,
                                onClick = {
                                    triggerHaptic(HapticFeedbackType.TextHandleMove)
                                    soundPlayer.playTap()
                                    if (searchOpen) {
                                        searchQuery = ""
                                    }
                                    searchOpen = !searchOpen
                                }
                            )
                            HeaderIconButton(
                                icon = Icons.Default.Tune,
                                contentDescription = "Sound and feel settings",
                                active = false,
                                onClick = {
                                    triggerHaptic(HapticFeedbackType.TextHandleMove)
                                    soundPlayer.playTap()
                                    showSoundSettings = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Minimal Progress Line + Subtle Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = when {
                                totalCount == 0 -> "No tasks yet"
                                activeCount == 0 -> "All done"
                                else -> "$activeCount remaining"
                            },
                            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                            label = "statusAnim"
                        ) { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = MonoPalette.Silver
                            )
                        }

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MonoPalette.PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MinimalProgressHairline(progress = progress)

                    // Collapsible Search Bar (only takes space when user taps Search)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = searchOpen,
                        enter = expandVertically(tween(200)) + fadeIn(tween(180)),
                        exit = shrinkVertically(tween(160)) + fadeOut(tween(120))
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            placeholder = {
                                Text(
                                    text = "Search...",
                                    color = MonoPalette.MutedGray
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MonoPalette.PureWhite,
                                unfocusedTextColor = MonoPalette.PureWhite,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                                cursorColor = MonoPalette.PureWhite
                            )
                        )
                    }
                }

                // Clean Luminous White-Gradient Sheet for Tasks
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(sheetBrush)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    // Clean Minimal Segmented Filter Row + Subtle Clear Done
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE7E7EC))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TodoFilter.entries.forEach { filter ->
                                val isSelected = selectedFilter == filter
                                val bg by animateColorAsState(
                                    targetValue = if (isSelected) MonoPalette.PureBlack else Color.Transparent,
                                    animationSpec = tween(160),
                                    label = "tabBg"
                                )
                                val fg by animateColorAsState(
                                    targetValue = if (isSelected) MonoPalette.PureWhite else MonoPalette.Graphite,
                                    animationSpec = tween(160),
                                    label = "tabFg"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(bg)
                                        .clickable {
                                            if (selectedFilter != filter) {
                                                triggerHaptic(HapticFeedbackType.TextHandleMove)
                                                soundPlayer.playTap()
                                                selectedFilter = filter
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = fg
                                    )
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = doneCount > 0,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(120))
                        ) {
                            Text(
                                text = "Clear done",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        triggerHaptic(HapticFeedbackType.LongPress)
                                        soundPlayer.playDelete()
                                        val removed = tasks.filter { it.isDone }
                                        tasks = tasks.filterNot { it.isDone }
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Cleared ${removed.size} completed",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                soundPlayer.playAdd()
                                                tasks = tasks + removed
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MonoPalette.MutedGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Clean, Airy, High-FPS Task List
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 72.dp)
                            ) {
                                Text(
                                    text = if (tasks.isEmpty()) "All clear" else "Nothing here",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MonoPalette.PureBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (tasks.isEmpty()) {
                                        "Tap + to add your first task."
                                    } else {
                                        "Try another filter or search term."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MonoPalette.MutedGray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 104.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = filteredTasks,
                                key = { it.id },
                                contentType = { it.isDone }
                            ) { task ->
                                CleanMonoTaskRow(
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ),
                                    task = task,
                                    onToggle = {
                                        val willBeDone = !task.isDone
                                        triggerHaptic(HapticFeedbackType.LongPress)
                                        tasks = tasks.map {
                                            if (it.id == task.id) it.copy(isDone = willBeDone) else it
                                        }
                                        if (willBeDone) {
                                            soundPlayer.playComplete()
                                        } else {
                                            soundPlayer.playTap()
                                        }
                                    },
                                    onEdit = {
                                        triggerHaptic(HapticFeedbackType.TextHandleMove)
                                        soundPlayer.playTap()
                                        editingTask = task
                                        showEditor = true
                                    },
                                    onDelete = {
                                        triggerHaptic(HapticFeedbackType.LongPress)
                                        soundPlayer.playDelete()
                                        tasks = tasks.filterNot { it.id == task.id }
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Removed '${task.title}'",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                soundPlayer.playAdd()
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
                    onChipTap = {
                        triggerHaptic(HapticFeedbackType.TextHandleMove)
                        soundPlayer.playTap()
                    },
                    onCancel = { showEditor = false },
                    onSave = { title, note, priority, category, dueLabel ->
                        triggerHaptic(HapticFeedbackType.LongPress)
                        soundPlayer.playAdd()
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

        if (showSoundSettings) {
            val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSoundSettings = false },
                sheetState = settingsSheetState,
                containerColor = MonoPalette.Obsidian,
                contentColor = MonoPalette.PureWhite,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                SoundSettingsSheet(
                    settings = soundSettings,
                    onUpdate = { updated -> soundSettings = updated },
                    onPreviewPack = { pack, vol ->
                        triggerHaptic(HapticFeedbackType.TextHandleMove)
                        soundPlayer.previewPack(pack, vol)
                    },
                    onDone = { showSoundSettings = false }
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (active) MonoPalette.PureWhite else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) MonoPalette.PureBlack else MonoPalette.PureWhite,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CleanMonoTaskRow(
    modifier: Modifier = Modifier,
    task: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rowAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.56f else 1f,
        animationSpec = tween(180),
        label = "rowAlpha"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (task.isDone) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val metaText = remember(task.category, task.dueLabel) {
        buildString {
            append(task.category.label)
            if (task.dueLabel.isNotBlank()) {
                append(" · ")
                append(task.dueLabel)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = rowAlpha },
        shape = RoundedCornerShape(16.dp),
        color = MonoPalette.PureWhite,
        border = BorderStroke(1.dp, MonoPalette.Hairline),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile minimalist circle checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = checkScale
                        scaleY = checkScale
                    }
                    .clip(CircleShape)
                    .background(if (task.isDone) MonoPalette.PureBlack else Color.Transparent)
                    .border(
                        width = 1.8.dp,
                        color = if (task.isDone) MonoPalette.PureBlack else MonoPalette.Graphite,
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = task.isDone,
                    enter = scaleIn(tween(150)) + fadeIn(tween(120)),
                    exit = scaleOut(tween(100)) + fadeOut(tween(100))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MonoPalette.PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority == Priority.HIGH && !task.isDone) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MonoPalette.PureBlack)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isDone) MonoPalette.MutedGray else MonoPalette.PureBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MonoPalette.MutedGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = metaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MonoPalette.MutedGray
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MonoPalette.Silver,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SoundSettingsSheet(
    settings: SoundSettings,
    onUpdate: (SoundSettings) -> Unit,
    onPreviewPack: (SoundPack, Float) -> Unit,
    onDone: () -> Unit
) {
    // Keep slider state local while dragging so SharedPreferences isn't spammed 60x/sec
    var sliderVolume by remember(settings.volume) { mutableFloatStateOf(settings.volume) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sound & Feel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MonoPalette.PureWhite
                )
                Text(
                    text = "Customize subtle audio & haptics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonoPalette.Silver
                )
            }
            TextButton(onClick = onDone) {
                Text("Done", color = MonoPalette.PureWhite, fontWeight = FontWeight.SemiBold)
            }
        }

        SettingToggleRow(
            title = "Sound Effects",
            subtitle = "Master audio toggle",
            checked = settings.enabled,
            onCheckedChange = { onUpdate(settings.copy(enabled = it)) }
        )

        if (settings.enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VOLUME",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MonoPalette.Silver
                    )
                    Text(
                        text = "${(sliderVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MonoPalette.PureWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
                Slider(
                    value = sliderVolume,
                    onValueChange = { sliderVolume = it },
                    onValueChangeFinished = {
                        onUpdate(settings.copy(volume = sliderVolume))
                        onPreviewPack(settings.pack, sliderVolume)
                    },
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = MonoPalette.PureWhite,
                        activeTrackColor = MonoPalette.PureWhite,
                        inactiveTrackColor = MonoPalette.Graphite
                    )
                )
            }

            Text(
                text = "STYLE (TAP TO PREVIEW)",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MonoPalette.Silver
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SoundPack.entries.forEach { pack ->
                    val isSelected = settings.pack == pack
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MonoPalette.PureWhite else MonoPalette.Charcoal,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MonoPalette.PureWhite else MonoPalette.Graphite
                        ),
                        onClick = {
                            onUpdate(settings.copy(pack = pack))
                            onPreviewPack(pack, sliderVolume)
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pack.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MonoPalette.PureBlack else MonoPalette.PureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pack.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MonoPalette.Graphite else MonoPalette.Silver,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Text(
                text = "PLAY SOUND ON",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = MonoPalette.Silver
            )
            SettingToggleRow(
                title = "Task completed",
                subtitle = "Soft chime on check-off",
                checked = settings.onComplete,
                onCheckedChange = { onUpdate(settings.copy(onComplete = it)) }
            )
            SettingToggleRow(
                title = "Task added",
                subtitle = "Gentle note on save",
                checked = settings.onAdd,
                onCheckedChange = { onUpdate(settings.copy(onAdd = it)) }
            )
            SettingToggleRow(
                title = "Task deleted",
                subtitle = "Muted low sweep",
                checked = settings.onDelete,
                onCheckedChange = { onUpdate(settings.copy(onDelete = it)) }
            )
            SettingToggleRow(
                title = "UI taps & filters",
                subtitle = "Micro-tick on buttons",
                checked = settings.onTap,
                onCheckedChange = { onUpdate(settings.copy(onTap = it)) }
            )
        }

        SettingToggleRow(
            title = "Haptic Feedback",
            subtitle = "Subtle vibration on actions",
            checked = settings.hapticsEnabled,
            onCheckedChange = { onUpdate(settings.copy(hapticsEnabled = it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MonoPalette.PureWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MonoPalette.Silver
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MonoPalette.PureBlack,
                checkedTrackColor = MonoPalette.PureWhite,
                uncheckedThumbColor = MonoPalette.Silver,
                uncheckedTrackColor = MonoPalette.Charcoal,
                uncheckedBorderColor = MonoPalette.Graphite
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonoTaskEditorSheet(
    initialTask: TodoItem?,
    onChipTap: () -> Unit,
    onCancel: () -> Unit,
    onSave: (String, String, Priority, TodoCategory, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title.orEmpty()) }
    var note by remember { mutableStateOf(initialTask?.note.orEmpty()) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(initialTask?.category ?: TodoCategory.PERSONAL) }
    var dueLabel by remember { mutableStateOf(initialTask?.dueLabel.orEmpty()) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (initialTask == null) {
            focusRequester.requestFocus()
        }
    }

    val dueOptions = listOf("", "Today", "Tomorrow", "This Week")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initialTask == null) "New Task" else "Edit Task",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MonoPalette.PureWhite
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("What needs to be done?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
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
                    onClick = {
                        onChipTap()
                        priority = option
                    }
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
                    onClick = {
                        onChipTap()
                        category = option
                    }
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
                    onClick = {
                        onChipTap()
                        dueLabel = option
                    }
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
                    text = if (initialTask == null) "Add Task" else "Save",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SelectableMonoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MonoPalette.PureWhite else MonoPalette.Charcoal,
        border = BorderStroke(1.dp, if (selected) MonoPalette.PureWhite else MonoPalette.Graphite),
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MonoPalette.PureBlack else MonoPalette.SoftSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
