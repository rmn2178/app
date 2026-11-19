package com.example.monotodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.monotodo.model.TodoCategory
import com.example.monotodo.model.TodoFilter
import com.example.monotodo.model.TodoItem
import com.example.monotodo.ui.components.AnimatedMonoProgressRing
import com.example.monotodo.ui.components.AnimatedMonochromeBackdrop
import com.example.monotodo.ui.components.CheckSparkleBurst
import com.example.monotodo.ui.components.MonochromeCelebrationOverlay
import com.example.monotodo.ui.components.bounceClick
import com.example.monotodo.ui.theme.MonoPalette
import com.example.monotodo.ui.theme.MonoTodoTheme
import kotlinx.coroutines.delay
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundPlayer = remember { SoundEffectPlayer(context.applicationContext) }

    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled()) }
    LaunchedEffect(soundEnabled) {
        soundPlayer.enabled = soundEnabled
        repository.setSoundEnabled(soundEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    var tasks by remember { mutableStateOf(repository.loadTasks()) }
    var selectedFilter by remember { mutableStateOf(TodoFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoItem?>(null) }
    var celebrationNonce by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tasks) {
        repository.saveTasks(tasks)
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
    val targetProgress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "barProgress"
    )

    // Smooth glossy shimmer gliding along the progress bar
    val shimmerTransition = rememberInfiniteTransition(label = "barShimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // FAB rotation & scale when opening editor
    val fabRotation by animateFloatAsState(
        targetValue = if (showEditor) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )
    val fabScale by animateFloatAsState(
        targetValue = if (showEditor) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Living black-to-white vertical gradient with drifting monochrome orbs
        AnimatedMonochromeBackdrop()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        soundPlayer.playTap()
                        editingTask = null
                        showEditor = true
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                        },
                    shape = CircleShape,
                    containerColor = MonoPalette.PureBlack,
                    contentColor = MonoPalette.PureWhite,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 14.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add task",
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer { rotationZ = fabRotation }
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

                // Monochrome Obsidian Header with Sound Toggle & Clear Done
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = doneCount > 0,
                            enter = fadeIn() + scaleIn(initialScale = 0.85f),
                            exit = fadeOut() + scaleOut(targetScale = 0.85f)
                        ) {
                            Surface(
                                modifier = Modifier.bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    soundPlayer.playDelete()
                                    val removed = tasks.filter { it.isDone }
                                    tasks = tasks.filterNot { it.isDone }
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Cleared ${removed.size} completed task(s)",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            soundPlayer.playAdd()
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
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MonoPalette.PureWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Sound toggle button
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val next = !soundEnabled
                                    soundEnabled = next
                                    if (next) {
                                        soundPlayer.enabled = true
                                        soundPlayer.playTap()
                                    }
                                },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = if (soundEnabled) 0.16f else 0.07f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AnimatedContent(
                                    targetState = soundEnabled,
                                    transitionSpec = {
                                        (scaleIn(initialScale = 0.7f) + fadeIn())
                                            .togetherWith(scaleOut(targetScale = 0.7f) + fadeOut())
                                    },
                                    label = "soundIconAnim"
                                ) { isOn ->
                                    Icon(
                                        imageVector = if (isOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        contentDescription = if (isOn) "Mute sounds" else "Enable sounds",
                                        tint = if (isOn) MonoPalette.PureWhite else MonoPalette.SlateGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frosted Obsidian Progress Card with Ring + Shimmering Bar + Rolling Counter
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.17f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedMonoProgressRing(
                            progress = targetProgress,
                            ringSize = 54.dp,
                            strokeWidth = 5.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = if (totalCount > 0 && activeCount == 0) {
                                    "All tasks complete"
                                } else {
                                    "$activeCount active  ·  $doneCount done"
                                },
                                transitionSpec = {
                                    (slideInVertically { it / 2 } + fadeIn())
                                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                                },
                                label = "summaryCounter"
                            ) { summary ->
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MonoPalette.PureWhite
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                val fillFraction = animatedProgress.coerceIn(0f, 1f)
                                if (fillFraction > 0.01f) {
                                    val shimmerBrush = Brush.horizontalGradient(
                                        0.0f to MonoPalette.Silver,
                                        (shimmerOffset * 0.8f + 0.1f).coerceIn(0.1f, 0.9f) to MonoPalette.PureWhite,
                                        1.0f to MonoPalette.Silver
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fillFraction)
                                            .height(7.dp)
                                            .clip(CircleShape)
                                            .background(shimmerBrush)
                                    )
                                }
                            }
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
                        androidx.compose.animation.AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(onClick = {
                                soundPlayer.playTap()
                                searchQuery = ""
                            }) {
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
                        focusedContainerColor = Color.Black.copy(alpha = 0.36f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.24f),
                        focusedBorderColor = Color.White.copy(alpha = 0.72f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                        cursorColor = MonoPalette.PureWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Animated Segmented Filter Pills with live counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TodoFilter.entries.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val countForFilter = when (filter) {
                            TodoFilter.ALL -> totalCount
                            TodoFilter.ACTIVE -> activeCount
                            TodoFilter.DONE -> doneCount
                        }
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                MonoPalette.PureWhite
                            } else {
                                Color.Black.copy(alpha = 0.28f)
                            },
                            animationSpec = tween(220),
                            label = "pillBg"
                        )
                        val pillText by animateColorAsState(
                            targetValue = if (isSelected) {
                                MonoPalette.PureBlack
                            } else {
                                MonoPalette.Mist
                            },
                            animationSpec = tween(220),
                            label = "pillText"
                        )

                        Surface(
                            modifier = Modifier.bounceClick(scaleDown = 0.93f) {
                                if (selectedFilter != filter) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    soundPlayer.playTap()
                                    selectedFilter = filter
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = pillBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MonoPalette.PureWhite else Color.White.copy(alpha = 0.20f)
                            )
                        ) {
                            Text(
                                text = "${filter.label} ($countForFilter)",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = pillText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smooth GPU-accelerated Task List
                if (filteredTasks.isEmpty()) {
                    EmptyMonoState(isCompletelyEmpty = tasks.isEmpty())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 2.dp, bottom = 108.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = filteredTasks,
                            key = { _, task -> task.id },
                            contentType = { _, task -> task.isDone }
                        ) { index, task ->
                            AnimatedMonoTaskCard(
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ),
                                entryIndex = index,
                                task = task,
                                onToggle = {
                                    val willBeDone = !task.isDone
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val updated = tasks.map {
                                        if (it.id == task.id) it.copy(isDone = willBeDone) else it
                                    }
                                    tasks = updated
                                    if (willBeDone) {
                                        val allCompleteNow = updated.isNotEmpty() && updated.all { it.isDone }
                                        if (allCompleteNow) {
                                            celebrationNonce += 1
                                            soundPlayer.playAllDone()
                                        } else {
                                            soundPlayer.playComplete()
                                        }
                                    } else {
                                        soundPlayer.playUncheck()
                                    }
                                },
                                onEdit = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    soundPlayer.playTap()
                                    editingTask = task
                                    showEditor = true
                                },
                                onDelete = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

        // Full-screen monochrome particle burst on 100% completion
        MonochromeCelebrationOverlay(burstNonce = celebrationNonce)

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
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        soundPlayer.playTap()
                    },
                    onCancel = {
                        soundPlayer.playTap()
                        showEditor = false
                    },
                    onSave = { title, note, priority, category, dueLabel ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
    }
}

@Composable
private fun EmptyMonoState(isCompletelyEmpty: Boolean) {
    val pulse = rememberInfiniteTransition(label = "emptyPulse")
    val iconScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )
    val floatOffset by pulse.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
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
                    .size(66.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        translationY = floatOffset
                    },
                shape = CircleShape,
                color = MonoPalette.PureBlack.copy(alpha = 0.88f),
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
                    "Tap + to create your next task."
                } else {
                    "Try another filter or search keyword."
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
    entryIndex: Int,
    task: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Staggered GPU entrance animation
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((entryIndex.coerceAtMost(8) * 35).toLong())
        enterAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.74f else 0.98f,
        animationSpec = tween(260),
        label = "cardAlpha"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (task.isDone) 0.dp else 5.dp,
        animationSpec = tween(260),
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
        animationSpec = tween(220),
        label = "checkBg"
    )
    val cardContainer by animateColorAsState(
        targetValue = if (task.isDone) MonoPalette.Mist else MonoPalette.PureWhite,
        animationSpec = tween(260),
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = enterAnim.value
                alpha = p * cardAlpha
                translationY = (1f - p) * 28f
                scaleX = 0.96f + 0.04f * p
                scaleY = 0.96f + 0.04f * p
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .bounceClick(scaleDown = 0.98f, onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
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
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                CheckSparkleBurst(trigger = task.isDone)

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                        }
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
                    androidx.compose.animation.AnimatedVisibility(
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
            }

            Spacer(modifier = Modifier.width(8.dp))

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
    onChipTap: () -> Unit,
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
        animationSpec = tween(190),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) MonoPalette.PureBlack else MonoPalette.Mist,
        animationSpec = tween(190),
        label = "chipFg"
    )

    Surface(
        modifier = Modifier.bounceClick(scaleDown = 0.93f, onClick = onClick),
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
