package com.kevin.swipeactionlab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            secondary = Ochre,
            background = Paper,
            surface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            SwipeActionLab()
        }
    }
}

private enum class SwipeMode {
    Archive,
    Pin,
    Mute,
    Delete,
    Complete
}

private data class SwipePattern(
    val mode: SwipeMode,
    val title: String,
    val action: String,
    val rule: String,
    val accent: Color
)

private data class InboxItem(
    val title: String,
    val meta: String,
    val preview: String
)

private val Paper = Color(0xFFF6F0E6)
private val Ink = Color(0xFF101714)
private val Muted = Color(0xFF68736D)
private val Line = Color(0xFFE3D9CC)
private val Green = Color(0xFF0D735B)
private val Blue = Color(0xFF245FA8)
private val Ochre = Color(0xFFB86B16)
private val Violet = Color(0xFF7651B8)
private val Red = Color(0xFFD33F49)

private val patterns = listOf(
    SwipePattern(
        mode = SwipeMode.Archive,
        title = "Archive",
        action = "Archive thread",
        rule = "Great for frequent, reversible cleanup. Pair it with undo instead of a heavy confirm dialog.",
        accent = Green
    ),
    SwipePattern(
        mode = SwipeMode.Pin,
        title = "Pin",
        action = "Pin to top",
        rule = "Useful when swipe means prioritization, not removal. Keep the icon and copy unmistakable.",
        accent = Blue
    ),
    SwipePattern(
        mode = SwipeMode.Mute,
        title = "Mute",
        action = "Mute updates",
        rule = "Best for reducing noise. The feedback should explain what changes after the gesture.",
        accent = Violet
    ),
    SwipePattern(
        mode = SwipeMode.Delete,
        title = "Delete",
        action = "Delete draft",
        rule = "Destructive swipes need stronger color, a longer threshold, and an immediate recovery path.",
        accent = Red
    ),
    SwipePattern(
        mode = SwipeMode.Complete,
        title = "Complete",
        action = "Mark done",
        rule = "Positive completion can be quick, but the card should visibly resolve after the gesture.",
        accent = Ochre
    )
)

private val inboxItems = listOf(
    InboxItem("Design critique notes", "Today", "Review motion thresholds before posting the demo."),
    InboxItem("KMP mini project backlog", "Tomorrow", "Order the next experiments by complexity and learning value."),
    InboxItem("LinkedIn carousel draft", "Friday", "Frame the post around product tradeoffs, not just implementation.")
)

@Composable
private fun SwipeActionLab() {
    var selectedIndex by remember { mutableStateOf(0) }
    var snackbarText by remember { mutableStateOf("Archived. Undo available.") }
    val selected = patterns[selectedIndex]

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 14.dp),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item {
                PatternTabs(selectedIndex) {
                    selectedIndex = it
                    snackbarText = "${patterns[it].title} ready. Try the card gesture."
                }
            }
            item {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "swipe-pattern"
                ) { pattern ->
                    SwipePreview(
                        pattern = pattern,
                        onAction = { snackbarText = "${pattern.action}. Undo available." }
                    )
                }
            }
            item { RuleCard(selected) }
            item { Spacer(Modifier.height(74.dp)) }
        }

        Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
            containerColor = Ink,
            contentColor = Color.White,
            action = { Text("UNDO", color = Color(0xFFFFD166), fontWeight = FontWeight.Black) }
        ) {
            Text(snackbarText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Swipe Action Lab",
            color = Ink,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "A Compose experiment for making swipe actions discoverable, reversible and hard to trigger by accident.",
            color = Muted,
            lineHeight = 23.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PatternTabs(selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        patterns.forEachIndexed { index, pattern ->
            val selected = selectedIndex == index
            Surface(
                modifier = Modifier.clickable { onSelected(index) },
                shape = RoundedCornerShape(50),
                color = if (selected) pattern.accent else Color.White,
                border = BorderStroke(1.dp, if (selected) pattern.accent else Line)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    text = pattern.title,
                    color = if (selected) Color.White else Ink,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SwipePreview(pattern: SwipePattern, onAction: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = pattern.action,
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Drag the top card. The action reveals first, then commits after the threshold.",
                color = Muted,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                inboxItems.forEachIndexed { index, item ->
                    SwipeCard(
                        item = item,
                        pattern = pattern,
                        featured = index == 0,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeCard(
    item: InboxItem,
    pattern: SwipePattern,
    featured: Boolean,
    onAction: () -> Unit
) {
    var dragOffset by remember(pattern.mode) { mutableFloatStateOf(if (featured) -44f else 0f) }
    val threshold = if (pattern.mode == SwipeMode.Delete) 150f else 118f
    val progress = (abs(dragOffset) / threshold).coerceIn(0f, 1f)
    val targetOffset = IntOffset(dragOffset.roundToInt(), 0)
    val animatedOffset by animateIntOffsetAsState(targetOffset, label = "card-offset")
    val actionAlpha by animateFloatAsState(if (progress > 0.12f) 1f else 0.45f, label = "action-alpha")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (featured) 126.dp else 88.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(pattern.accent.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.alpha(actionAlpha)
            ) {
                ActionGlyph(pattern)
                Text(pattern.title, color = pattern.accent, fontWeight = FontWeight.Black)
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { animatedOffset }
                .pointerInput(pattern.mode) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(dragOffset) >= threshold) {
                                dragOffset = -96f
                                onAction()
                            } else {
                                dragOffset = if (featured) -44f else 0f
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount.x).coerceIn(-184f, 42f)
                    }
                },
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Line)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(if (featured) 8.dp else 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(pattern.accent)
                    Text(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .weight(1f),
                        text = item.title,
                        color = Ink,
                        fontWeight = FontWeight.Black
                    )
                    Text(item.meta, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(item.preview, color = Muted, lineHeight = 19.sp)
                if (featured) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = pattern.accent,
                        trackColor = pattern.accent.copy(alpha = 0.14f)
                    )
                    Text(
                        text = if (progress >= 1f) "Release to commit" else "Threshold ${(progress * 100).roundToInt()}%",
                        color = pattern.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionGlyph(pattern: SwipePattern) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(pattern.accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (pattern.mode == SwipeMode.Delete) Icons.Filled.Close else Icons.Filled.Check,
            contentDescription = null,
            tint = pattern.accent
        )
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun RuleCard(pattern: SwipePattern) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Ink
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Decision rule", color = Color.White, fontWeight = FontWeight.Black)
            Text(pattern.rule, color = Color(0xFFDDE4DE), lineHeight = 20.sp)
        }
    }
}
