package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AgentRank
import com.example.model.AgentTriggerSet
import com.example.model.BORDER_TRIGGER_CATALOG
import com.example.model.TriggerCategory
import com.example.model.TriggerDefinition
import com.example.model.TriggerSlot
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan900
import com.example.ui.theme.Cyan950
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

/**
 * World Trigger Authentic Trigger Holder (8-Slot) Interface
 * Replicates canonical Trigger Set layout:
 * [SUB TRIGGER サブトリガー] <--- [TRIGGER HOLDER CORE] ---> [MAIN TRIGGER メイントリガー]
 */
@Composable
fun WorldTriggerSetHolder(
    triggerSet: AgentTriggerSet,
    canCustomize: Boolean,
    currentRank: AgentRank,
    userSpecialization: String,
    onSlotClick: (isMain: Boolean, slotNumber: Int, currentSlot: TriggerSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, if (canCustomize) Cyan500.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(12.dp))
            .testTag("world_trigger_holder_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Banner: ▷▷▷ TRIGGER SET ◁◁◁ (トリガーセット)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950, RoundedCornerShape(6.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "▷▷▷ ",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TRIGGER SET",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Slate100,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = " ◁◁◁",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "トリガーセット",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }

                // Rank clearance badge
                Box(
                    modifier = Modifier
                        .background(
                            if (canCustomize) Cyan950 else Slate950,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (canCustomize) Cyan400 else Slate700,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!canCustomize) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = OrangeWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (canCustomize) "CUSTOM AUTHORIZED" else "C-RANK LOCKED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (canCustomize) Cyan300 else OrangeWarning
                        )
                    }
                }
            }

            // Dual Paned Holder Layout (SUB on left, MAIN on right, Center metallic connector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: SUB TRIGGER (Slots 5-8)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900, RoundedCornerShape(4.dp))
                            .border(1.dp, OrangeWarning.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUB TRIGGER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = OrangeWarning
                        )
                        Text(
                            text = "サブ",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                    }

                    // 4 Sub Slots
                    triggerSet.subTriggers.forEach { slot ->
                        TriggerHolderSlotBox(
                            slot = slot,
                            isSub = true,
                            canCustomize = canCustomize,
                            onClick = { onSlotClick(false, slot.slotNumber, slot) }
                        )
                    }
                }

                // CENTER: Tactical Circuit / Holder Connector Core
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(236.dp)
                        .background(Slate900, RoundedCornerShape(4.dp))
                        .border(1.dp, Slate700, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f

                        // Vertical trunk circuit
                        drawLine(
                            color = Cyan500.copy(alpha = 0.7f),
                            start = Offset(cx, 12f),
                            end = Offset(cx, h - 12f),
                            strokeWidth = 2f
                        )

                        // Top & Bottom ports
                        drawCircle(color = Cyan400, radius = 4f, center = Offset(cx, 16f))
                        drawCircle(color = Cyan400, radius = 4f, center = Offset(cx, h - 16f))

                        // Center Trion Core
                        drawCircle(color = Cyan500, radius = 6f, center = Offset(cx, h / 2f))
                        drawCircle(color = Slate950, radius = 3f, center = Offset(cx, h / 2f))

                        // 4 connection rungs to Left (Sub) and Right (Main)
                        for (i in 0 until 4) {
                            val slotY = 32f + i * 56f
                            // Left wing connector
                            drawLine(
                                color = OrangeWarning.copy(alpha = 0.6f),
                                start = Offset(cx, slotY),
                                end = Offset(2f, slotY),
                                strokeWidth = 2f
                            )
                            // Right wing connector
                            drawLine(
                                color = Cyan400.copy(alpha = 0.6f),
                                start = Offset(cx, slotY),
                                end = Offset(w - 2f, slotY),
                                strokeWidth = 2f
                            )
                        }
                    }

                    // Vertical labels
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "●",
                            fontSize = 7.sp,
                            color = Cyan400,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "B",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                        Text(
                            text = "D",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                        Text(
                            text = "A",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                        Text(
                            text = "●",
                            fontSize = 7.sp,
                            color = Cyan400,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // RIGHT: MAIN TRIGGER (Slots 1-4)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900, RoundedCornerShape(4.dp))
                            .border(1.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MAIN TRIGGER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400
                        )
                        Text(
                            text = "メイン",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                    }

                    // 4 Main Slots
                    triggerSet.mainTriggers.forEach { slot ->
                        TriggerHolderSlotBox(
                            slot = slot,
                            isSub = false,
                            canCustomize = canCustomize,
                            onClick = { onSlotClick(true, slot.slotNumber, slot) }
                        )
                    }
                }
            }

            // Footer hint / status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (canCustomize)
                        "Chạm vào bất kỳ Slot nào để đổi Trigger (B-Rank+)"
                    else
                        "Cần đạt B-Rank (4.000 PTS) để mở khóa tùy biến Trigger",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (canCustomize) Cyan400 else OrangeWarning
                )

                Text(
                    text = "SPEC: ${userSpecialization.uppercase()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
            }
        }
    }
}

/**
 * Individual Slot styled closely to the official Manga/Anime Trigger Set look
 */
@Composable
private fun TriggerHolderSlotBox(
    slot: TriggerSlot,
    isSub: Boolean,
    canCustomize: Boolean,
    onClick: () -> Unit
) {
    val isFree = slot.name.equals("FREE TRIGGER", ignoreCase = true) || slot.tag.equals("EMPTY", ignoreCase = true)
    val accentColor = if (isSub) OrangeWarning else Cyan400

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                if (isFree) Slate950 else Slate900,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isFree) Slate800 else accentColor.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("holder_slot_${slot.slotNumber}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#0${slot.slotNumber}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isFree) Slate600 else accentColor
                )
                Text(
                    text = slot.tag,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isFree) Slate700 else Slate400
                )
            }

            Text(
                text = slot.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isFree) Slate500 else Slate100,
                maxLines = 1
            )
        }
    }
}

/**
 * Trigger Swap & Selector Dialog / Sheet
 * Displays triggers filtered by:
 * - Attacker Triggers: Kogetsu, Raygust, Scorpion
 * - Gunner Triggers: Asteroid, Hound, Meteor, Viper
 * - Firearms: Assault Rifle, Grenade Launcher, Handgun, Shotgun
 * - Sniper Triggers: Egret, Ibis, Lightning
 * - Trap Triggers: Switchbox
 * - Defense Triggers: Escudo, Shield
 * - Optional Triggers: Bagworm, Chameleon, Dummy Beacon, Grasshopper, Idaten, Spider, Teleporter, Lead Bullet
 * - Plus Specialization recommendations and Free Slot
 */
@Composable
fun TriggerSelectionDialog(
    isMain: Boolean,
    slotNumber: Int,
    currentSlot: TriggerSlot,
    userSpecialization: String,
    onSelectTrigger: (TriggerDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TriggerCategory?>(null) }
    var filterBySpecOnly by remember { mutableStateOf(false) }

    val filteredList = remember(selectedCategory, filterBySpecOnly, userSpecialization) {
        BORDER_TRIGGER_CATALOG.filter { trigger ->
            val matchesCategory = selectedCategory == null || trigger.category == selectedCategory
            val matchesSpec = !filterBySpecOnly || trigger.suitableSpecializations.any {
                it.equals(userSpecialization, ignoreCase = true)
            }
            matchesCategory && matchesSpec
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .border(1.5.dp, Cyan500, RoundedCornerShape(14.dp))
                .testTag("trigger_selection_dialog"),
            color = Slate950,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isMain) Cyan400 else OrangeWarning, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRANG BỊ TRIGGER • SLOT #0$slotNumber (${if (isMain) "MAIN" else "SUB"})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (isMain) Cyan300 else OrangeWarning,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Đang chọn: ${currentSlot.name} (${currentSlot.type})",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Slate400
                        )
                    }
                }

                // Specialization Filter chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (filterBySpecOnly) Cyan950 else Slate900,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (filterBySpecOnly) Cyan400 else Slate800,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { filterBySpecOnly = !filterBySpecOnly }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (filterBySpecOnly) Cyan400 else Slate500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đề xuất cho vị trí: $userSpecialization",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (filterBySpecOnly) Cyan300 else Slate400
                            )
                        }
                    }
                }

                // Category chips filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CategoryPill(
                            label = "TẤT CẢ",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(TriggerCategory.values()) { category ->
                        CategoryPill(
                            label = category.title.uppercase(),
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                // Trigger Catalog list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { trigger ->
                        val isEquipped = currentSlot.name.equals(trigger.name, ignoreCase = true)
                        val isRecommended = trigger.suitableSpecializations.any {
                            it.equals(userSpecialization, ignoreCase = true)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    when {
                                        isEquipped -> Cyan400
                                        isRecommended -> Slate700
                                        else -> Slate800
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTrigger(trigger) }
                                .testTag("trigger_item_${trigger.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEquipped) Cyan950.copy(alpha = 0.4f) else Slate900
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = trigger.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isEquipped) Cyan300 else Slate100
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Slate950, RoundedCornerShape(4.dp))
                                                .border(1.dp, Slate800, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = trigger.tag,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Cyan400
                                            )
                                        }

                                        if (isRecommended) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Amber400.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, Amber400.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "RECOMMENDED",
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Amber400
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${trigger.category.title} • ${trigger.type}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate400,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Text(
                                        text = trigger.description,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate500,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Cyan500, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Đang trang bị",
                                            tint = Slate950,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ĐÓNG",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Cyan500 else Slate900,
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (selected) Cyan400 else Slate800,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (selected) Slate950 else Slate400
        )
    }
}
