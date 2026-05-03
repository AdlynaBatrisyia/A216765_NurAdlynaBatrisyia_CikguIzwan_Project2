package com.example.a216765_wan_lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

// ── NAVIGATION ROUTES ──
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Inbox : Screen("inbox")
    object History : Screen("history")
    object Insights : Screen("insights")
    object Sleep : Screen("sleep")
    object SimpleActivity : Screen("simple_activity")
    object InboxDetail : Screen("inbox_detail/{messageIndex}") {
        fun createRoute(index: Int) = "inbox_detail/$index"
    }
}

// ── COLORS ──
val MintLight     = Color(0xFFf8fdf9)
val MintSurface   = Color(0xFFe8f5ee)
val MintBorder    = Color(0xFFc8e6d4)
val MintGreen     = Color(0xFF3a9e6a)
val SageDark      = Color(0xFF2d6b4a)
val SageMid       = Color(0xFF4a7a60)
val SageMuted     = Color(0xFF7aaa90)
val White         = Color(0xFFFFFFFF)
val GoalBlue      = Color(0xFFe8f5ee)
val GoalBlueTxt   = Color(0xFF2d6b4a)
val GoalPink      = Color(0xFFfce8f0)
val GoalPinkTxt   = Color(0xFFa0526d)
val GoalPurple    = Color(0xFFede8fc)
val GoalPurpleTxt = Color(0xFF6b52b8)
val GoalEmpty     = Color(0xFFf5f5f5)
val Black         = Color(0xFF000000)
val UnreadRed     = Color(0xFFE53935)
val ActivityOrange = Color(0xFFFF9800)
val ActivityOrangeBg = Color(0xFFFFF3E0)

// ── MAIN ACTIVITY ──
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MoodSpaceNavigation()
            }
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
}

fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

// ── NAVIGATION HOST ──
@Composable
fun MoodSpaceNavigation() {
    val navController = rememberNavController()
    val viewModel: MoodSpaceViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            MoodSpaceApp(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Inbox.route) {
            InboxScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Sleep.route) {
            SleepScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.SimpleActivity.route) {
            SimpleActivityScreen(navController = navController, viewModel = viewModel)
        }
        composable("inbox_detail/{messageIndex}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("messageIndex")?.toIntOrNull() ?: 0
            InboxDetailScreen(navController = navController, viewModel = viewModel, messageIndex = index)
        }
    }
}

// ══════════════════════════════════════════════
// ── REUSABLE COMPOSABLE 1: GoalCardImage ──
// Used for: Mood, Gratitude, Sleep cards on Home
// ══════════════════════════════════════════════
@Composable
fun GoalCardImage(
    icon: Painter,
    label: String,
    bgColor: Color,
    textColor: Color,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.fillMaxWidth(0.75f).fillMaxHeight(0.75f),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = icon, contentDescription = label,
                    modifier = Modifier.fillMaxSize(), tint = Color.Unspecified)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
        if (isDone) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    .size(18.dp).clip(CircleShape).background(MintGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 10.sp, color = White)
            }
        }
    }
}

// ══════════════════════════════════════════════
// ── REUSABLE COMPOSABLE 2: ActivityCard ──
// Used for: History screen entries (Mood, Gratitude, Sleep, Simple Activity)
// ══════════════════════════════════════════════
@Composable
fun ActivityCard(
    bgColor: Color,
    title: String,
    subtitle: String = "",
    timeText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(bgColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Black)
                    if (subtitle.isNotEmpty()) {
                        Text(" $subtitle", fontSize = 15.sp, color = SageMuted)
                    }
                }
                Text(timeText, fontSize = 11.sp, color = SageMuted)
            }
            Text("›", fontSize = 20.sp, color = SageMuted)
        }
        HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
    }
}

// ── BOTTOM NAV BAR ──
@Composable
fun AppBottomBar(
    navController: NavController,
    viewModel: MoodSpaceViewModel,
    onPlusClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val uiState by viewModel.uiState.collectAsState()
    val unreadCount = (0..2).count { it !in uiState.readMessages }

    val homeIcon:     Painter = painterResource(id = R.drawable.home_icon)
    val inboxIcon:    Painter = painterResource(id = R.drawable.inbox_icon)
    val plusIcon:     Painter = painterResource(id = R.drawable.plus_icon)
    val insightsIcon: Painter = painterResource(id = R.drawable.insight_icon)
    val historyIcon:  Painter = painterResource(id = R.drawable.history_icon)

    Row(
        modifier = Modifier.fillMaxWidth().background(White)
            .border(0.5.dp, MintBorder).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }) {
            Icon(painter = homeIcon, contentDescription = "Home",
                modifier = Modifier.size(42.dp), tint = Color.Unspecified)
            Text("Home", fontSize = 9.sp,
                color = if (currentRoute == Screen.Home.route) MintGreen else SageMuted,
                fontWeight = if (currentRoute == Screen.Home.route) FontWeight.Bold else FontWeight.Normal)
        }

        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate(Screen.Inbox.route) }) {
                Icon(painter = inboxIcon, contentDescription = "Inbox",
                    modifier = Modifier.size(42.dp), tint = Color.Unspecified)
                Text("Inbox", fontSize = 9.sp,
                    color = if (currentRoute == Screen.Inbox.route) MintGreen else SageMuted,
                    fontWeight = if (currentRoute == Screen.Inbox.route) FontWeight.Bold else FontWeight.Normal)
            }
            if (unreadCount > 0) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape)
                    .background(UnreadRed).align(Alignment.TopEnd))
            }
        }

        // Plus button — triggers popup
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MintGreen)
                .clickable { onPlusClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = plusIcon, contentDescription = "Add",
                modifier = Modifier.size(32.dp), tint = White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { navController.navigate(Screen.Insights.route) }) {
            Icon(painter = insightsIcon, contentDescription = "Insights",
                modifier = Modifier.size(42.dp), tint = Color.Unspecified)
            Text("Insights", fontSize = 9.sp,
                color = if (currentRoute == Screen.Insights.route) MintGreen else SageMuted,
                fontWeight = if (currentRoute == Screen.Insights.route) FontWeight.Bold else FontWeight.Normal)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { navController.navigate(Screen.History.route) }) {
            Icon(painter = historyIcon, contentDescription = "History",
                modifier = Modifier.size(42.dp), tint = Color.Unspecified)
            Text("History", fontSize = 9.sp,
                color = if (currentRoute == Screen.History.route) MintGreen else SageMuted,
                fontWeight = if (currentRoute == Screen.History.route) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// ── HOME SCREEN ──
@Composable
fun MoodSpaceApp(navController: NavController, viewModel: MoodSpaceViewModel) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    var nameInput by remember { mutableStateOf("") }
    var showMoodDialog by remember { mutableStateOf(false) }
    var showGratitudePage by remember { mutableStateOf(false) }
    var showPlusMenu by remember { mutableStateOf(false) }

    if (showGratitudePage) {
        GratitudePage(
            onSave = { viewModel.setGratitudeDone(); showGratitudePage = false },
            onBack = { showGratitudePage = false }
        )
        return
    }

    val settingsIcon:  Painter = painterResource(id = R.drawable.settings_icon)
    val homeIconTop:   Painter = painterResource(id = R.drawable.home_icon)
    val refreshIcon:   Painter = painterResource(id = R.drawable.refresh_icon)
    val smileFaceIcon: Painter = painterResource(id = R.drawable.smile_face_icon)
    val heartIcon:     Painter = painterResource(id = R.drawable.heart_icon)
    val sleepIcon:     Painter = painterResource(id = R.drawable.sleep_icon)

    if (showMoodDialog) {
        MoodDialog(
            currentMood = uiState.selectedMood,
            onSave = { chosenMood -> viewModel.setMoodDone(chosenMood); showMoodDialog = false },
            onDismiss = { showMoodDialog = false }
        )
    }

    if (uiState.showAllGoalsPopup) {
        Dialog(onDismissRequest = { viewModel.dismissAllGoalsPopup() }) {
            Card(shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { viewModel.dismissAllGoalsPopup() }) {
                            Text("✕", fontSize = 16.sp, color = SageMuted) }
                        Text("Daily Goals", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Black)
                        TextButton(onClick = { viewModel.dismissAllGoalsPopup() }) {
                            Text("↗", fontSize = 16.sp, color = SageMuted) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("GREAT JOB! 🎉", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(MintGreen),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DAILY", fontSize = 14.sp, color = White, fontWeight = FontWeight.Bold)
                            Text("100%", fontSize = 36.sp, color = White, fontWeight = FontWeight.Bold)
                            Text("GOALS", fontSize = 14.sp, color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("You completed all your daily goals!", fontSize = 13.sp, color = SageMuted)
                }
            }
        }
    }

    // Plus menu popup
    if (showPlusMenu) {
        Dialog(onDismissRequest = { showPlusMenu = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        showPlusMenu = false
                        navController.navigate(Screen.SimpleActivity.route)
                    }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Simple Activity", fontSize = 16.sp,
                                fontWeight = FontWeight.Medium, color = Black)
                            Text("Quickly add a simple activity.", fontSize = 12.sp, color = SageMuted)
                        }
                    }
                }
            }
        }
    }

    val doneCount = listOf(uiState.moodDone, uiState.gratitudeDone, uiState.sleepDone).count { it }
    val totalGoals = 3
    val percentage = ((doneCount.toFloat() / totalGoals) * 100).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MintLight).verticalScroll(scrollState)) {

            Row(modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = settingsIcon, contentDescription = "Settings",
                    modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Icon(painter = homeIconTop, contentDescription = "Home",
                        modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                }
                Icon(painter = refreshIcon, contentDescription = "Refresh",
                    modifier = Modifier.size(20.dp), tint = Color.Unspecified)
            }

            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text(
                    text = if (uiState.displayedName.isNotEmpty())
                        "${getGreeting()}, ${uiState.displayedName}! 🌿"
                    else "${getGreeting()}, User! 🌿",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SageDark)
                Text(text = "How are you feeling today?", fontSize = 13.sp, color = SageMuted)
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("What's your name?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SageDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it },
                        placeholder = { Text("Enter your name...", fontSize = 13.sp, color = SageMuted) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen, unfocusedBorderColor = MintBorder),
                        singleLine = true)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { if (nameInput.isNotEmpty()) viewModel.setDisplayedName(nameInput) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen)) {
                        Text("Save Name", fontSize = 14.sp, color = White) }
                    if (uiState.displayedName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Welcome back, ${uiState.displayedName}! 💚",
                            fontSize = 13.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MOOD STREAK", fontSize = 10.sp, color = SageMuted,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("1", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text("day in a row", fontSize = 11.sp, color = SageMuted)
                    }
                    Box(modifier = Modifier.width(0.5.dp).height(70.dp).background(MintBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DAILY GOALS", fontSize = 10.sp, color = SageMuted,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("$percentage%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                        Text("$doneCount of $totalGoals", fontSize = 11.sp, color = SageMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("DAILY GOALS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = SageDark, letterSpacing = 0.5.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(MintSurface).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("+ add", fontSize = 12.sp, color = MintGreen) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // GoalCardImage reusable composable — Usage 1
                        GoalCardImage(icon = smileFaceIcon, label = "Mood",
                            bgColor = GoalBlue, textColor = GoalBlueTxt, isDone = uiState.moodDone,
                            modifier = Modifier.weight(1f).clickable { showMoodDialog = true })
                        // GoalCardImage reusable composable — Usage 2
                        GoalCardImage(icon = heartIcon, label = "Gratitude",
                            bgColor = GoalPink, textColor = GoalPinkTxt, isDone = uiState.gratitudeDone,
                            modifier = Modifier.weight(1f).clickable { showGratitudePage = true })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // GoalCardImage reusable composable — Usage 3
                        GoalCardImage(icon = sleepIcon, label = "Sleep",
                            bgColor = GoalPurple, textColor = GoalPurpleTxt, isDone = uiState.sleepDone,
                            modifier = Modifier.weight(1f).clickable {
                                navController.navigate(Screen.Sleep.route) })
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)).background(GoalEmpty)
                            .border(1.5.dp, MintBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("＋", fontSize = 24.sp, color = SageMuted)
                                Text("Add goal", fontSize = 11.sp, color = SageMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("REMINDERS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = SageDark, letterSpacing = 0.5.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(MintSurface).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("+ add", fontSize = 12.sp, color = MintGreen) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            var isReminderExpanded by remember { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                .clickable { isReminderExpanded = !isReminderExpanded },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)) {
                Column(modifier = Modifier.padding(14.dp).animateContentSize(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow))) {
                    Text("5 slow belly breaths can help you let go of today. Extra long exhales. 🌬️",
                        fontSize = 13.sp, color = SageMid, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MintGreen),
                            border = BorderStroke(0.5.dp, MintGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                            Text("📓 Journal", fontSize = 11.sp) }
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MintGreen),
                            border = BorderStroke(0.5.dp, MintGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                            Text("↗ Share", fontSize = 11.sp) }
                        Text("hide", fontSize = 12.sp, color = SageMuted)
                    }
                    if (isReminderExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("💡 Tip of the day", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Taking a few minutes each day to check in with your emotions can significantly improve your mental well-being. You are doing great! 🌿",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tap to collapse ▲", fontSize = 11.sp, color = SageMuted,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to read more ▼", fontSize = 11.sp, color = SageMuted,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("A216765", fontSize = 11.sp, color = SageMuted,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AppBottomBar(
                navController = navController,
                viewModel = viewModel,
                onPlusClick = { showPlusMenu = true }
            )
        }
    }
}

// ── SIMPLE ACTIVITY SCREEN ──
@Composable
fun SimpleActivityScreen(navController: NavController, viewModel: MoodSpaceViewModel) {
    var activityTitle by remember { mutableStateOf("") }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSavedBanner by remember { mutableStateOf(false) }
    val currentTime = remember { getCurrentTime() }
    val currentDate = remember { getCurrentDateTime() }

    // Auto-hide saved banner after 2 seconds then navigate back
    LaunchedEffect(showSavedBanner) {
        if (showSavedBanner) {
            delay(1800)
            showSavedBanner = false
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    if (showUnsavedDialog) {
        Dialog(onDismissRequest = { showUnsavedDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖐 Unsaved Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do you want to save them?", fontSize = 13.sp, color = SageMuted)
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(onClick = {
                        if (activityTitle.isNotEmpty()) {
                            viewModel.addActivity(activityTitle, currentTime)
                        }
                        showUnsavedDialog = false
                        showSavedBanner = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save", fontSize = 15.sp, color = MintGreen) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        navController.popBackStack()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Don't Save", fontSize = 15.sp, color = Color(0xFFE57373)) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = { showUnsavedDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", fontSize = 15.sp, color = SageMuted) }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

            // Top bar
            Row(modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    if (activityTitle.isNotEmpty()) showUnsavedDialog = true
                    else navController.popBackStack()
                }) {
                    Text("✕", fontSize = 20.sp, color = Black, fontWeight = FontWeight.Bold) }
                Text("Simple Activity", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Black)
                TextButton(onClick = {
                    if (activityTitle.isNotEmpty()) {
                        viewModel.addActivity(activityTitle, currentTime)
                        showSavedBanner = true
                    }
                }) {
                    Text("Save", fontSize = 15.sp, color = if (activityTitle.isNotEmpty()) Black else SageMuted,
                        fontWeight = FontWeight.Bold) }
            }

            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Description row
                        Text("Enter your activity.", fontSize = 13.sp, color = SageMuted)
                        HorizontalDivider(color = MintBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Date row
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📅", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Date", fontSize = 13.sp, color = Black, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Today", fontSize = 13.sp, color = SageDark)
                                Text(currentTime, fontSize = 12.sp, color = SageMuted)
                            }
                        }

                        HorizontalDivider(color = MintBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Title input row
                        Row(verticalAlignment = Alignment.Top) {
                            Text("✏️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Title", fontSize = 13.sp, color = Black,
                                    fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = activityTitle,
                                    onValueChange = { activityTitle = it },
                                    placeholder = { Text("Enter activity title.", fontSize = 13.sp, color = SageMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MintGreen,
                                        unfocusedBorderColor = Color.Transparent),
                                    singleLine = true
                                )
                            }
                        }

                        HorizontalDivider(color = MintBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Optional section
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Optional", fontSize = 12.sp, color = SageMuted)
                            Text("show", fontSize = 12.sp, color = SageMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SageMuted),
                        border = BorderStroke(0.5.dp, MintBorder),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)) {
                        Text("↗ SHARE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SageMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About Simple Activity card
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("About Simple Activity", fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = Black)
                            Text("hide", fontSize = 12.sp, color = SageMuted)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("The Simple Activity entry type is a quick way to enter an item into your history that you want to track and note how it's affected your mood.",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("If it's an activity that you'll do regularly, then consider creating a Custom Activity which will show up in your Activity menu and on charts on the Insights screen.",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // SAVED banner — shows at top center, auto-dismisses
        AnimatedVisibility(
            visible = showSavedBanner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MintGreen)
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✓", fontSize = 16.sp, color = White, fontWeight = FontWeight.Bold)
                    Text("SAVED", fontSize = 15.sp, color = White, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ── INBOX SCREEN ──
@Composable
fun InboxScreen(navController: NavController, viewModel: MoodSpaceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMarkAllDialog by remember { mutableStateOf(false) }

    val messages = listOf(
        Triple("Free Guest Pass", "Share a free guest pass to MoodSpace Premium.", "2026-04-22"),
        Triple("How Daily Goals Work", "Progress not perfection.", "2026-04-22"),
        Triple("Keeping a Mood Journal", "Awareness of your thinking and feelings can lead to a better mood.", "2026-04-22")
    )

    val filteredMessages = if (searchQuery.isEmpty()) messages
    else messages.filter {
        it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.contains(searchQuery, ignoreCase = true)
    }

    if (showMarkAllDialog) {
        AlertDialog(onDismissRequest = { showMarkAllDialog = false },
            title = { Text("Unread Messages", fontWeight = FontWeight.Bold) },
            text = { Text("Mark all items as read?") },
            confirmButton = {
                TextButton(onClick = { viewModel.markAllRead(); showMarkAllDialog = false }) {
                    Text("Yes", color = MintGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllDialog = false }) {
                    Text("Cancel", color = SageMuted) }
            })
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().background(White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Inbox", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showMarkAllDialog = true }) {
                            Text("✓+", fontSize = 16.sp, color = SageDark, fontWeight = FontWeight.Bold) }
                        TextButton(onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" }) {
                            Text("🔍", fontSize = 18.sp) }
                    }
                }
                if (showSearch) {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Search...", color = SageMuted) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen, unfocusedBorderColor = MintBorder),
                        singleLine = true)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                filteredMessages.forEachIndexed { index, (title, subtitle, date) ->
                    val isRead = index in uiState.readMessages
                    Row(modifier = Modifier.fillMaxWidth().background(White)
                        .clickable {
                            viewModel.markMessageRead(index)
                            navController.navigate(Screen.InboxDetail.createRoute(index))
                        }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (!isRead) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(UnreadRed))
                        } else {
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontSize = 15.sp,
                                fontWeight = if (!isRead) FontWeight.Bold else FontWeight.Normal, color = Black)
                            Text(subtitle, fontSize = 12.sp, color = SageMuted, lineHeight = 16.sp, maxLines = 2)
                            Text(date, fontSize = 11.sp, color = SageMuted)
                        }
                        Text("›", fontSize = 20.sp, color = SageMuted)
                    }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AppBottomBar(navController = navController, viewModel = viewModel, onPlusClick = {
                navController.navigate(Screen.SimpleActivity.route)
            })
        }
    }
}

// ── INBOX DETAIL SCREEN ──
@Composable
fun InboxDetailScreen(navController: NavController, viewModel: MoodSpaceViewModel, messageIndex: Int) {
    val messages = listOf(
        Pair("Free Guest Pass", buildAnnotatedString {
            append("Help a friend stress less, sleep better, and feel happier.\n\n")
            append("When you send a Guest Pass to a new member, they'll get free access to all of MoodSpace for a week.\n\n")
            append("To share a Guest Pass simply share this notice or a screenshot.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("MoodSpace\nFree 7 Day Guest Pass\n\n") }
            append("Go to getmoodspace.com/redeem\n\nCreate account using program code ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("GUESTPASS7.") }
        }),
        Pair("How Daily Goals Work", buildAnnotatedString {
            append("Your Daily Goals are shown on your Home Screen. Think of them like your workout for your mental health.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("- Perform good practices: ") }
            append("The daily goals you do in MoodSpace are a collection of simple, good practices for your mental health.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("- Create momentum: ") }
            append("When you accomplish a goal it creates an important sense of agency.\n\n")
            append("If you get only 1% better every day for a year, you would be ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("37 times better.") }
        }),
        Pair("Keeping a Mood Journal", buildAnnotatedString {
            append("Keeping a mood journal is an integral part of working toward a better mood.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("- When you bring awareness ") }
            append("to how you're feeling it brings blood flow to the prefrontal cortex of your brain.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("- To get out of autopilot. ") }
            append("So often we operate on autopilot, that we're not aware of our feelings.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("- To look back in time ") }
            append("at your mood to gain insights into people, places and events that may have affected your mood.")
        })
    )

    val (title, content) = messages[messageIndex]

    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("‹", fontSize = 24.sp, color = Black, fontWeight = FontWeight.Bold) }
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                TextButton(onClick = {}) { Text("🗑", fontSize = 18.sp, color = SageMuted) }
            }
            HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
            Column(modifier = Modifier.fillMaxWidth().weight(1f)
                .verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text(text = content, fontSize = 13.sp, color = Black, lineHeight = 20.sp)
            }
        }
    }
}

// ── HISTORY SCREEN ──
@Composable
fun HistoryScreen(navController: NavController, viewModel: MoodSpaceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val todayTime = remember { SimpleDateFormat("h:mma", Locale.getDefault()).format(Date()) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text("History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Black)
            }
            HorizontalDivider(color = MintBorder, thickness = 0.5.dp)

            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0))
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Today", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Black)
                }

                // ActivityCard reusable composable — Usage 1 (Mood)
                if (uiState.moodDone) {
                    ActivityCard(
                        bgColor = GoalBlue,
                        title = "Mood",
                        subtitle = uiState.selectedMood,
                        timeText = todayTime
                    )
                }

                // ActivityCard reusable composable — Usage 2 (Gratitude)
                if (uiState.gratitudeDone) {
                    ActivityCard(
                        bgColor = GoalPink,
                        title = "Gratitude",
                        timeText = todayTime
                    )
                }

                // ActivityCard reusable composable — Usage 3 (Sleep)
                if (uiState.sleepDone) {
                    ActivityCard(
                        bgColor = GoalPurple,
                        title = "Sleep",
                        subtitle = "${uiState.sleepDuration}, ${uiState.inBedTime} - ${uiState.outOfBedTime}",
                        timeText = todayTime
                    )
                }

                // ActivityCard reusable composable — Usage 4+ (Simple Activities)
                uiState.activities.forEach { activity ->
                    ActivityCard(
                        bgColor = ActivityOrangeBg,
                        title = activity.title,
                        timeText = activity.time
                    )
                }

                if (!uiState.moodDone && !uiState.gratitudeDone && !uiState.sleepDone && uiState.activities.isEmpty()) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("No entries yet today. Complete your daily goals!",
                        fontSize = 13.sp, color = SageMuted,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AppBottomBar(navController = navController, viewModel = viewModel, onPlusClick = {
                navController.navigate(Screen.SimpleActivity.route)
            })
        }
    }
}

// ── INSIGHTS SCREEN ──
@Composable
fun InsightsScreen(navController: NavController, viewModel: MoodSpaceViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text("Insights", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Black)
            }
            HorizontalDivider(color = MintBorder, thickness = 0.5.dp)

            Column(modifier = Modifier.fillMaxWidth().weight(1f)
                .verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CHART VARIABLES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(GoalBlue))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mood", fontSize = 12.sp, color = SageDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val moodLabels = listOf("Bad", "Poor", "OK", "Good", "Great")
                        Column(modifier = Modifier.fillMaxWidth()) {
                            moodLabels.reversed().forEach { label ->
                                Row(modifier = Modifier.fillMaxWidth().height(40.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(label, fontSize = 11.sp, color = SageMuted, modifier = Modifier.width(40.dp))
                                    HorizontalDivider(color = MintBorder.copy(alpha = 0.5f),
                                        thickness = 0.5.dp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.moodDone && uiState.selectedMood.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MintSurface)) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GoalBlue))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Today's mood: ", fontSize = 13.sp, color = SageDark)
                                    Text(uiState.selectedMood, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                                }
                            }
                        } else {
                            Text("No mood recorded yet. Complete your Mood daily goal to see insights!",
                                fontSize = 12.sp, color = SageMuted, lineHeight = 18.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AppBottomBar(navController = navController, viewModel = viewModel, onPlusClick = {
                navController.navigate(Screen.SimpleActivity.route)
            })
        }
    }
}

// ── SLEEP SCREEN ──
@Composable
fun SleepScreen(navController: NavController, viewModel: MoodSpaceViewModel) {
    var inBedTime by remember { mutableStateOf("11:00 PM") }
    var outOfBedTime by remember { mutableStateOf("7:00 AM") }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    fun calcDuration(inBed: String, outOfBed: String): String {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val inTime = sdf.parse(inBed)
            val outTime = sdf.parse(outOfBed)
            if (inTime != null && outTime != null) {
                var diff = outTime.time - inTime.time
                if (diff < 0) diff += 24 * 60 * 60 * 1000
                val hours = diff / (1000 * 60 * 60)
                "$hours hr"
            } else "8 hr"
        } catch (e: Exception) { "8 hr" }
    }

    val duration = calcDuration(inBedTime, outOfBedTime)
    val hasChanges = inBedTime != "11:00 PM" || outOfBedTime != "7:00 AM"

    if (showUnsavedDialog) {
        Dialog(onDismissRequest = { showUnsavedDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖐 Unsaved Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do you want to save them?", fontSize = 13.sp, color = SageMuted)
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(onClick = {
                        viewModel.setSleepDone(inBedTime, outOfBedTime, duration)
                        showUnsavedDialog = false; navController.popBackStack()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save", fontSize = 15.sp, color = MintGreen) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = { showUnsavedDialog = false; navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Don't Save", fontSize = 15.sp, color = Color(0xFFE57373)) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = { showUnsavedDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", fontSize = 15.sp, color = SageMuted) }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Row(modifier = Modifier.fillMaxWidth().background(White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { if (hasChanges) showUnsavedDialog = true else navController.popBackStack() }) {
                Text("✕", fontSize = 20.sp, color = Black, fontWeight = FontWeight.Bold) }
            Text("Sleep", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Black)
            TextButton(onClick = { viewModel.setSleepDone(inBedTime, outOfBedTime, duration); navController.popBackStack() }) {
                Text("Save", fontSize = 15.sp, color = Black, fontWeight = FontWeight.Bold) }
        }

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Enter when you begin and end your sleep and the duration of the actual sleep.",
                        fontSize = 13.sp, color = SageMid, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
                        Text("🛏", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("In Bed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("10:00 PM", "11:00 PM", "12:00 AM", "1:00 AM").forEach { time ->
                                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                        .background(if (inBedTime == time) MintGreen else Color(0xFFEEEEEE))
                                        .clickable { inBedTime = time }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                        Text(time, fontSize = 11.sp, color = if (inBedTime == time) White else Black)
                                    }
                                }
                            }
                        }
                        Text(inBedTime, fontSize = 13.sp, color = SageDark, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
                        Text("🌅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Out of Bed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM").forEach { time ->
                                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                        .background(if (outOfBedTime == time) MintGreen else Color(0xFFEEEEEE))
                                        .clickable { outOfBedTime = time }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                        Text(time, fontSize = 11.sp, color = if (outOfBedTime == time) White else Black)
                                    }
                                }
                            }
                        }
                        Text(outOfBedTime, fontSize = 13.sp, color = SageDark, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏳", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Duration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                        }
                        Text(duration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    }

                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Optional", fontSize = 12.sp, color = SageMuted)
                        Text("hide", fontSize = 12.sp, color = SageMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quality", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("The quality of this activity", fontSize = 11.sp, color = SageMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("Great", "Good", "OK", "Poor", "Bad").forEach { q ->
                            Text(q, fontSize = 12.sp, color = SageDark, modifier = Modifier.clickable { })
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Comments", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("expand", fontSize = 11.sp, color = SageMuted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter comments or use shortcuts.", fontSize = 12.sp, color = SageMuted)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── GRATITUDE PAGE ──
@Composable
fun GratitudePage(onSave: () -> Unit, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val promptOptions = listOf(
        "What are you grateful for?",
        "What are you looking forward to in the future?",
        "What accomplishments (big or small) did you have today?",
        "What about today has been better than yesterday?",
        "What were the best moments from today (even if they were challenging)?",
        "What are very small things you're grateful for?",
        "What are things about you that you're grateful for?",
        "What made you smile or laugh today?",
        "Who has been helpful to you?",
        "What could have gone worse today but didn't?"
    )
    var selectedPrompt     by remember { mutableStateOf(promptOptions[0]) }
    var showPromptPicker   by remember { mutableStateOf(false) }
    var grateful1          by remember { mutableStateOf("") }
    var grateful2          by remember { mutableStateOf("") }
    var grateful3          by remember { mutableStateOf("") }
    var selectedEffect     by remember { mutableStateOf("") }
    var showAboutGratitude by remember { mutableStateOf(true) }
    var showUnsavedDialog  by remember { mutableStateOf(false) }
    val hasChanges = grateful1.isNotEmpty() || grateful2.isNotEmpty() ||
            grateful3.isNotEmpty() || selectedEffect.isNotEmpty()

    if (showPromptPicker) {
        Dialog(onDismissRequest = { showPromptPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showPromptPicker = false }) {
                            Text("＜", fontSize = 16.sp, color = SageDark) }
                        Text("Gratitude Prompts", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            color = Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select a prompt to enter gratitude about.", fontSize = 12.sp, color = SageMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())) {
                        promptOptions.forEach { prompt ->
                            HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                if (prompt != selectedPrompt) {
                                    grateful1 = ""; grateful2 = ""; grateful3 = ""
                                    selectedPrompt = prompt
                                }
                                showPromptPicker = false
                            }.padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(prompt, fontSize = 13.sp, color = SageDark, lineHeight = 18.sp)
                            }
                        }
                        HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (showUnsavedDialog) {
        Dialog(onDismissRequest = { showUnsavedDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖐 Unsaved Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do you want to save them?", fontSize = 13.sp, color = SageMuted)
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(onClick = { showUnsavedDialog = false; onSave() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save", fontSize = 15.sp, color = MintGreen) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = { showUnsavedDialog = false; onBack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Don't Save", fontSize = 15.sp, color = Color(0xFFE57373)) }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    TextButton(onClick = { showUnsavedDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", fontSize = 15.sp, color = SageMuted) }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Row(modifier = Modifier.fillMaxWidth().background(White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { if (hasChanges) showUnsavedDialog = true else onBack() }) {
                Text("✕", fontSize = 20.sp, color = Black, fontWeight = FontWeight.Bold) }
            Text("Gratitude", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Black)
            TextButton(onClick = { onSave() }) {
                Text("Save", fontSize = 15.sp, color = Black, fontWeight = FontWeight.Bold) }
        }
        Row(modifier = Modifier.fillMaxWidth().background(White).padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center) {
            Text("JOURNAL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SageDark,
                modifier = Modifier.border(width = 0.5.dp, color = MintBorder,
                    shape = RoundedCornerShape(4.dp)).padding(horizontal = 16.dp, vertical = 6.dp))
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(selectedPrompt, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = Black, modifier = Modifier.weight(1f), lineHeight = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { showPromptPicker = true }) {
                            Text("Select prompt", fontSize = 12.sp, color = SageMuted) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Date", fontSize = 13.sp, color = SageDark)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = getCurrentDateTime(), fontSize = 12.sp, color = SageMuted)
                    }
                    HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(grateful1, grateful2, grateful3).forEachIndexed { i, value ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("🤍", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = value,
                                onValueChange = { v -> when(i) { 0 -> grateful1 = v; 1 -> grateful2 = v; 2 -> grateful3 = v } },
                                placeholder = { Text("I'm grateful for...", fontSize = 13.sp, color = SageMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MintGreen, unfocusedBorderColor = Color.Transparent),
                                singleLine = true)
                        }
                        if (i < 2) { HorizontalDivider(color = MintBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("➕", fontSize = 16.sp, color = GoalPinkTxt)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD ITEM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoalPinkTxt)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🙂", fontSize = 16.sp); Spacer(modifier = Modifier.width(6.dp))
                        Text("Mood Effect", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("The effect on your mood", fontSize = 11.sp, color = SageMuted)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val effects = listOf("Very\nPositive", "Positive", "Neutral", "Negative", "Very\nNegative")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        effects.forEach { effect ->
                            val isSelected = selectedEffect == effect
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MintGreen else Color(0xFFEEEEEE))
                                .clickable { selectedEffect = effect }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center) {
                                Text(effect, fontSize = 10.sp,
                                    color = if (isSelected) White else Black,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = {}) {
                    Text("➡ SHARE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SageMuted) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp).animateContentSize(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium))) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("About Gratitude", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                        TextButton(onClick = { showAboutGratitude = !showAboutGratitude }) {
                            Text(if (showAboutGratitude) "hide" else "show", fontSize = 12.sp, color = SageMuted) }
                    }
                    if (showAboutGratitude) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Keeping a daily gratitude journal has been shown to increase positive emotions.",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Expressing why you're grateful for these items gives your brain additional opportunity to build a gratitude mindset.",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Finally, sending expressions of gratitude to others increases positive emotions for both the sender and receiver.",
                            fontSize = 12.sp, color = SageMid, lineHeight = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── MOOD DIALOG ──
@Composable
fun MoodDialog(currentMood: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var tempMood by remember { mutableStateOf(currentMood) }
    val greatEmoji: Painter = painterResource(id = R.drawable.great_emoji)
    val goodEmoji:  Painter = painterResource(id = R.drawable.good_emoji)
    val okEmoji:    Painter = painterResource(id = R.drawable.ok_emoji)
    val poorEmoji:  Painter = painterResource(id = R.drawable.poor_emoji)
    val badEmoji:   Painter = painterResource(id = R.drawable.bad_emoji)
    val moods = listOf(greatEmoji to "Great", goodEmoji to "Good", okEmoji to "OK",
        poorEmoji to "Poor", badEmoji to "Bad")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("✕", fontSize = 18.sp, color = SageDark) }
                    Text("Mood", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SageDark)
                    TextButton(onClick = { if (tempMood.isNotEmpty()) onSave(tempMood) }) {
                        Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintGreen) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("What is your mood?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SageDark)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    moods.forEach { (icon, label) ->
                        val isSelected = tempMood == label
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MintGreen else Color.Transparent)
                                .clickable { tempMood = label }.padding(10.dp)) {
                            Icon(painter = icon, contentDescription = label,
                                modifier = Modifier.size(36.dp), tint = Color.Unspecified)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, fontSize = 11.sp,
                                color = if (isSelected) White else Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Comments", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SageDark)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = "", onValueChange = {},
                    placeholder = { Text("Enter comments about what you're thinking and feeling...",
                        fontSize = 12.sp, color = SageMuted, lineHeight = 18.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen, unfocusedBorderColor = MintBorder))
            }
        }
    }
}