package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AgentViewModel
import com.example.ui.screens.AgentRegistrationScreen
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SoloRankScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.TrainingScreen
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Hồ sơ", Icons.Default.Person, "tab_dashboard"),
    TIMER("Huấn luyện", Icons.Default.Timer, "tab_timer"),
    TRAINING("Ngoại ngữ", Icons.Default.MenuBook, "tab_training"),
    SOLO_RANK("Đấu Rank", Icons.Default.SportsKabaddi, "tab_solo_rank"),
    CODE_EDITOR("Mã nguồn", Icons.Default.Code, "tab_code_editor")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BorderAgentApp()
            }
        }
    }
}

@Composable
fun BorderAgentApp(
    viewModel: AgentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val context = LocalContext.current

    // Request POST_NOTIFICATIONS permission for Android 13+ (API 33+) if reminder is enabled
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled gracefully
    }

    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity
        val intent = activity?.intent
        if (intent?.getStringExtra("OPEN_TAB") == "SOLO_RANK") {
            currentTab = AppTab.SOLO_RANK
            val subTab = intent.getIntExtra("OPEN_SOLO_SUBTAB", -1)
            if (subTab >= 0) {
                viewModel.setSoloRankTab(subTab)
            }
        }
    }

    LaunchedEffect(uiState.isDailyMissionReminderEnabled) {
        if (uiState.isDailyMissionReminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // First Launch: Must input profile details before accessing Border Network
    if (!uiState.isProfileRegistered) {
        AgentRegistrationScreen(
            uiState = uiState,
            onSaveProfile = viewModel::registerProfile,
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
        )
        return
    }

    // Modal Profile Editor (allows updating name, age, gender, position, and picture)
    if (uiState.showEditProfileDialog) {
        AgentRegistrationScreen(
            uiState = uiState,
            onSaveProfile = viewModel::registerProfile,
            onDismiss = { viewModel.setShowEditProfileDialog(false) },
            isEditMode = true,
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
        topBar = {
            // Border Agency Tactical HUD Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .statusBarsPadding()
                    .border(
                        width = 1.dp,
                        color = Cyan500.copy(alpha = 0.3f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand: Shield + Unit & Agent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Cyan500.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "BORDER DEFENSE AGENCY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan400,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AGENT: ${uiState.agentName.uppercase()} (${uiState.agentAge})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate100
                            )
                        }
                    }

                    // Right Info: Trion Points & Rank Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Points Badge
                        Row(
                            modifier = Modifier
                                .background(Slate950, RoundedCornerShape(50))
                                .border(1.dp, Slate800, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.trionPoints} PTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }

                        // Rank Tag Pill
                        Box(
                            modifier = Modifier
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .border(1.dp, uiState.currentRank.color, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = uiState.currentRank.tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = uiState.currentRank.color
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .border(1.dp, Slate800)
                    .navigationBarsPadding(),
                containerColor = Slate900,
                contentColor = Slate100,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = Cyan300,
                            indicatorColor = Cyan500,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        uiState = uiState,
                        onEditProfile = { viewModel.setShowEditProfileDialog(true) },
                        onSwapTrigger = viewModel::swapTriggerSlot,
                        onResetTriggerSet = viewModel::resetTriggerSetToDefault
                    )
                    AppTab.TIMER -> TimerScreen(
                        uiState = uiState,
                        onModeChange = viewModel::setTimerMode,
                        onToggleTimer = viewModel::toggleTimer,
                        onResetCountdown = viewModel::resetCountdown,
                        onFinishStopwatch = viewModel::finishStopwatch,
                        onTogglePomodoro = viewModel::togglePomodoroTimer,
                        onResetPomodoro = viewModel::resetPomodoroTimer,
                        onSkipPomodoroPhase = viewModel::skipPomodoroPhase,
                        onSelectPomodoroPhase = viewModel::setPomodoroPhase,
                        onSelectPomodoroSubject = viewModel::setPomodoroSubject,
                        onSelectActiveTask = viewModel::setActiveStudyTask,
                        onAddStudyTask = { title, category, tags, pomodoros ->
                            viewModel.addStudyTask(
                                title = title,
                                category = category,
                                tags = tags,
                                targetPomodoros = pomodoros
                            )
                        },
                        onToggleStudyTask = viewModel::toggleStudyTask,
                        onDeleteStudyTask = viewModel::deleteStudyTask,
                        isSoundEnabled = uiState.isTimerSoundEnabled,
                        onToggleSound = viewModel::toggleTimerSound,
                        onTestSound = viewModel::playTestSound
                    )
                    AppTab.TRAINING -> TrainingScreen(
                        uiState = uiState,
                        onCompleteMission = viewModel::completeTrainingMission,
                        onStartPomodoroForMission = { missionTitle ->
                            viewModel.setPomodoroSubject(missionTitle)
                            viewModel.setTimerMode("pomodoro")
                            currentTab = AppTab.TIMER
                        }
                    )
                    AppTab.SOLO_RANK -> SoloRankScreen(
                        uiState = uiState,
                        onStartMatch = viewModel::handleSoloRankMatch,
                        onSelectTab = viewModel::setSoloRankTab,
                        onSelectHistoryFilter = viewModel::setHistoryFilter,
                        onClearHistory = viewModel::clearMatchHistory,
                        onSyncCloud = viewModel::syncUserRankToCloud,
                        onToggleDailyMission = viewModel::toggleDailyMission,
                        onAddDailyMission = viewModel::addDailyMission,
                        onUpdateMissionNotesAndTags = viewModel::updateMissionNotesAndTags,
                        onDeleteDailyMission = viewModel::deleteDailyMission,
                        onToggleDailyMissionReminder = viewModel::toggleDailyMissionReminder,
                        onSetDailyMissionReminderTime = viewModel::setDailyMissionReminderTime,
                        onTestDailyMissionReminder = viewModel::testTriggerDailyMissionReminder,
                        onTestBailOut = viewModel::playTestBailOut,
                        onToggleBailOutHaptics = viewModel::toggleBailOutHaptics,
                        onToggleSound = viewModel::toggleTimerSound
                    )
                    AppTab.CODE_EDITOR -> CodeEditorScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // Feedback Alert Dialog for Timer Completion / Points gained
    if (uiState.showCompletedDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCompletedDialog,
            title = {
                Text(
                    text = uiState.completedDialogTitle,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Cyan300
                )
            },
            text = {
                Text(
                    text = uiState.completedDialogBody,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Slate100,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::dismissCompletedDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan500,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "XÁC NHẬN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        )
    }
}
