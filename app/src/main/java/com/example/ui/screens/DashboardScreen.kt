package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.RANKS
import com.example.ui.AgentUiState
import com.example.ui.components.RadarChartComposable
import com.example.ui.components.TriggerSetDisplay
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.IconButton
import coil.compose.AsyncImage
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.model.TriggerDefinition
import com.example.model.TriggerSlot
import com.example.ui.components.TriggerSelectionDialog
import com.example.ui.components.WorldTriggerSetHolder

@Composable
fun DashboardScreen(
    uiState: AgentUiState,
    onEditProfile: () -> Unit = {},
    onSwapTrigger: (isMain: Boolean, slotNumber: Int, newTrigger: TriggerDefinition) -> Unit = { _, _, _ -> },
    onResetTriggerSet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var activeDialogSlot by remember {
        mutableStateOf<Pair<Boolean, TriggerSlot>?>(null)
    }

    // Modal dialog for selecting/swapping trigger
    activeDialogSlot?.let { (isMain, slot) ->
        TriggerSelectionDialog(
            isMain = isMain,
            slotNumber = slot.slotNumber,
            currentSlot = slot,
            userSpecialization = uiState.profile.position,
            onSelectTrigger = { newTrigger ->
                onSwapTrigger(isMain, slot.slotNumber, newTrigger)
                activeDialogSlot = null
            },
            onDismiss = { activeDialogSlot = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Agent Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .testTag("agent_profile_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(2.dp, Cyan500, CircleShape)
                            .background(Cyan950)
                            .clickable(onClick = onEditProfile)
                            .testTag("dashboard_agent_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!uiState.profile.pictureUri.isNullOrBlank()) {
                            AsyncImage(
                                model = uiState.profile.pictureUri,
                                contentDescription = "Ảnh đặc vụ",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.avatar_belen),
                                contentDescription = "Agent Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Little edit badge in bottom corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(Cyan500, CircleShape)
                                .border(1.dp, Slate900, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Đổi ảnh / Sửa hồ sơ",
                                tint = Slate950,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.agentName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate200
                            )
                            IconButton(
                                onClick = onEditProfile,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                                    .testTag("btn_edit_profile")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa hồ sơ",
                                    tint = Cyan400,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "Vị trí: ${uiState.profile.position} • ${uiState.profile.gender} • ${uiState.agentAge} tuổi",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = "Trigger: ${uiState.triggerType}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = uiState.currentRank.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = uiState.currentRank.color,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "ĐIỂM HIỆN TẠI",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                    Text(
                        text = "${uiState.trionPoints} PTS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }
            }
        }

        // Radar Chart
        RadarChartComposable(
            parameters = uiState.parameters,
            title = "TRION PARAMETER (${uiState.profile.position.uppercase()})",
            subtitle = "CHỈ SỐ THỰC CHIẾN BORDER",
            modifier = Modifier.testTag("radar_chart")
        )

        // Selected Trigger Set (Authentic 8-Slot Holder with customization)
        WorldTriggerSetHolder(
            triggerSet = uiState.triggerSet,
            canCustomize = uiState.canCustomizeTriggers,
            currentRank = uiState.currentRank,
            userSpecialization = uiState.profile.position,
            onSlotClick = { isMain, slotNumber, currentSlot ->
                if (uiState.canCustomizeTriggers) {
                    activeDialogSlot = Pair(isMain, currentSlot)
                }
            },
            modifier = Modifier.testTag("dashboard_world_trigger_holder")
        )

        // Customization Controls for B-Rank and above
        if (uiState.canCustomizeTriggers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // Open quick picker for main slot 1
                        val firstSlot = uiState.triggerSet.mainTriggers.firstOrNull() ?: TriggerSlot(1, "Ibis", "Main Weapon", "", "MAIN")
                        activeDialogSlot = Pair(true, firstSlot)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan500,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_custom_swap_triggers")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TÙY BIẾN TRIGGER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onResetTriggerSet,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_reset_triggers")
                ) {
                    Text(
                        text = "VỀ MẶC ĐỊNH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
            }
        }


        // Agent Rank Status & Hierarchy Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .testTag("rank_specs_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HỆ THỐNG CẤP BẬC ĐẶC VỤ BORDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(uiState.currentRank.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, uiState.currentRank.color, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = uiState.currentRank.tag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = uiState.currentRank.color
                        )
                    }
                }

                Text(
                    text = "Đặc vụ ${uiState.agentName} (${uiState.agentAge} tuổi • ${uiState.profile.position}) hiện đang giữ cấp bậc ${uiState.currentRank.name} với ${uiState.trionPoints} điểm Trion. Tích lũy thêm điểm qua luyện tập tập trung và chiến thắng Solo Rank Wars để thăng hạng!",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Ranks hierarchy
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RANKS.forEach { r ->
                        val isCurrent = uiState.currentRank == r
                        val isAchieved = uiState.trionPoints >= r.minPoints
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    when {
                                        isCurrent -> Cyan950.copy(alpha = 0.5f)
                                        isAchieved -> Slate900
                                        else -> Slate950
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = when {
                                        isCurrent -> Cyan400
                                        isAchieved -> Cyan500.copy(alpha = 0.4f)
                                        else -> Slate800
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isAchieved) Cyan400 else Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = r.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isAchieved) r.color else Slate500
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCurrent) {
                                    Text(
                                        text = "HIỆN TẠI • ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Cyan300
                                    )
                                }
                                Text(
                                    text = "${r.minPoints} PTS",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isCurrent) Cyan300 else Slate400
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trigger Footer info card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(8.dp))
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ACTIVE TRIGGER: ${uiState.triggerType.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400
                    )
                    Text(
                        text = "${uiState.profile.position} Unit • Border Agency OS v3.4",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }

                Text(
                    text = "READY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
            }
        }
    }
}
