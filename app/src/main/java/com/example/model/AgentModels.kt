package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RankARank
import com.example.ui.theme.RankBRank
import com.example.ui.theme.RankCRank
import com.example.ui.theme.RankSRank

data class AgentRank(
    val name: String,
    val minPoints: Int,
    val color: Color,
    val tag: String
)

val RANKS = listOf(
    AgentRank(name = "C-Rank (Thực tập sinh)", minPoints = 0, color = RankCRank, tag = "C-RANK"),
    AgentRank(name = "B-Rank (Đặc vụ chính thức)", minPoints = 4000, color = RankBRank, tag = "B-RANK"),
    AgentRank(name = "A-Rank (Tinh anh)", minPoints = 8000, color = RankARank, tag = "A-RANK"),
    AgentRank(name = "S-Rank (Black Trigger)", minPoints = 15000, color = RankSRank, tag = "S-RANK")
)

fun getRankForPoints(points: Int): AgentRank {
    var current = RANKS.first()
    for (rank in RANKS) {
        if (points >= rank.minPoints) {
            current = rank
        }
    }
    return current
}

data class TrainingMission(
    val id: Int,
    val week: String,
    val title: String,
    val completed: Boolean = false,
    val top15Achieved: Boolean = false,
    val reward: Int = 10
)

val DEFAULT_MISSIONS = listOf(
    TrainingMission(
        id = 1,
        week = "Tuần 1",
        title = "Phân tích tài liệu quân sự Neighborhood (Ngữ pháp nâng cao)",
        reward = 10
    ),
    TrainingMission(
        id = 2,
        week = "Tuần 2",
        title = "Giải mã tín hiệu trinh sát Aftokrator (Từ vựng chuyên ngành)",
        reward = 10
    ),
    TrainingMission(
        id = 3,
        week = "Tuần 3",
        title = "Báo cáo điểm yếu kẻ địch qua đàm phán ngoại ngữ (Reading/Listening)",
        reward = 10
    ),
    TrainingMission(
        id = 4,
        week = "Tuần 4",
        title = "Dịch thuật chiến thuật ngắm bắn Ibis tầm xa (Advanced Syntax)",
        reward = 10
    ),
    TrainingMission(
        id = 5,
        week = "Tuần 5",
        title = "Bài tập mô phỏng chiến đấu ngôn ngữ tốc độ cao (Idioms & Phrasal Verbs)",
        reward = 10
    )
)

data class SoloMatchResult(
    val playerRoll: Int,
    val enemyRoll: Int,
    val won: Boolean,
    val pointsDiff: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class ParameterItem(
    val key: String,
    val displayLines: List<String>,
    val value: Int
)

val DEFAULT_PARAMETERS = listOf(
    ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 5),
    ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 7),
    ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 3),
    ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 5),
    ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 9),
    ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 9),
    ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 6),
    ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 5)
)

data class AgentProfile(
    val name: String = "Belén Salinas",
    val age: Int = 15,
    val gender: String = "Nữ", // "Nữ", "Nam", "Khác"
    val position: String = "Sniper", // "Sniper", "Attacker", "Gunner", "Shooter", "All-Rounder", "Operator"
    val pictureUri: String? = null,
    val isRegistered: Boolean = false
)

data class AgentPositionInfo(
    val title: String,
    val code: String,
    val description: String,
    val defaultTrigger: String
)

val AVAILABLE_POSITIONS = listOf(
    AgentPositionInfo(
        title = "Sniper",
        code = "SNP",
        description = "Tác chiến tầm xa siêu cấp (Ibis / Egret / Lightning)",
        defaultTrigger = "Ibis (Sniper)"
    ),
    AgentPositionInfo(
        title = "Attacker",
        code = "ATK",
        description = "Cận chiến tốc độ cao (Kogetsu / Scorpion / Raygust)",
        defaultTrigger = "Kogetsu (Attacker)"
    ),
    AgentPositionInfo(
        title = "Gunner",
        code = "GNR",
        description = "Hỏa lực súng liên thanh tầm trung (Submachine Gun / Shotgun)",
        defaultTrigger = "Submachine Gun (Gunner)"
    ),
    AgentPositionInfo(
        title = "Shooter",
        code = "SHT",
        description = "Điều khiển khối đạn Trion ma trận (Asteroid / Hound / Meteor)",
        defaultTrigger = "Asteroid (Shooter)"
    ),
    AgentPositionInfo(
        title = "All-Rounder",
        code = "ALR",
        description = "Linh hoạt đa năng kết hợp cận chiến và tầm trung",
        defaultTrigger = "Raygust & Gunner"
    ),
    AgentPositionInfo(
        title = "Operator",
        code = "OPR",
        description = "Phân tích chiến thuật, quét sóng radar và bảo mật tổng hành dinh",
        defaultTrigger = "Border HQ Terminal"
    )
)

val GENDER_OPTIONS = listOf("Nữ", "Nam", "Khác")

data class TriggerSlot(
    val slotNumber: Int,
    val name: String,
    val type: String, // "Main Weapon", "Sub Weapon", "Defense", "Option / Utility", "Radar", "Special"
    val description: String,
    val tag: String
)

enum class TriggerCategory(val title: String, val jpTitle: String) {
    ATTACKER("Attacker Triggers", "攻撃手トリガー"),
    GUNNER("Gunner Triggers", "銃手トリガー"),
    FIREARMS("Firearms", "銃火器"),
    SNIPER("Sniper Triggers", "狙撃手トリガー"),
    TRAP("Trap Triggers", "トラップトリガー"),
    DEFENSE("Defense Triggers", "防御トリガー"),
    OPTIONAL("Optional Triggers", "オプショントリガー")
}

data class TriggerDefinition(
    val id: String,
    val name: String,
    val category: TriggerCategory,
    val type: String,
    val description: String,
    val tag: String,
    val suitableSpecializations: List<String> = emptyList()
)

val BORDER_TRIGGER_CATALOG: List<TriggerDefinition> = listOf(
    // Attacker Triggers: Kogetsu, Raygust, Scorpion
    TriggerDefinition(
        id = "kogetsu",
        name = "Kogetsu",
        category = TriggerCategory.ATTACKER,
        type = "Attacker / Blade",
        description = "Thanh kiếm katana Trion truyền thống, độ sắc bén và độ bền vượt trội nhất.",
        tag = "BLADE",
        suitableSpecializations = listOf("Attacker", "All-Rounder")
    ),
    TriggerDefinition(
        id = "raygust",
        name = "Raygust",
        category = TriggerCategory.ATTACKER,
        type = "Attacker / Shield Blade",
        description = "Vũ khí cận chiến phòng thủ hình lưỡi dao lớn, có thể hoán chuyển chế độ Khiên (Shield Mode) và Đao (Blade Mode).",
        tag = "DEF/BLADE",
        suitableSpecializations = listOf("Attacker", "All-Rounder")
    ),
    TriggerDefinition(
        id = "scorpion",
        name = "Scorpion",
        category = TriggerCategory.ATTACKER,
        type = "Attacker / Blade",
        description = "Lưỡi dao Trion siêu nhẹ, có thể biến hình và kéo dài tự do từ mọi điểm trên cơ thể.",
        tag = "ASSASSIN",
        suitableSpecializations = listOf("Attacker", "All-Rounder")
    ),

    // Gunner Triggers: Asteroid, Hound, Meteor, Viper
    TriggerDefinition(
        id = "asteroid",
        name = "Asteroid",
        category = TriggerCategory.GUNNER,
        type = "Gunner / Bullets",
        description = "Đạn Trion cơ bản tiêu chuẩn của Border, uy lực hỏa lực và tốc độ bay cao nhất vì không tiêu tốn thuộc tính phụ.",
        tag = "BULLET",
        suitableSpecializations = listOf("Gunner", "Shooter", "All-Rounder")
    ),
    TriggerDefinition(
        id = "hound",
        name = "Hound",
        category = TriggerCategory.GUNNER,
        type = "Gunner / Homing",
        description = "Đạn Trion tự động truy đuổi mục tiêu qua sóng nhiệt hoặc tín hiệu radar của đối phương.",
        tag = "HOMING",
        suitableSpecializations = listOf("Gunner", "Shooter", "All-Rounder")
    ),
    TriggerDefinition(
        id = "meteor",
        name = "Meteor",
        category = TriggerCategory.GUNNER,
        type = "Gunner / Explosive",
        description = "Đạn nổ diện rộng cực mạnh, phá hủy công trình và đánh bật kẻ địch ẩn nấp.",
        tag = "AOE",
        suitableSpecializations = listOf("Gunner", "Shooter", "All-Rounder")
    ),
    TriggerDefinition(
        id = "viper",
        name = "Viper",
        category = TriggerCategory.GUNNER,
        type = "Gunner / Trajectory",
        description = "Đạn Trion điều khiển quỹ đạo bay biến ảo, có thể vẽ đường cong tấn công sau vật chắn.",
        tag = "CURVE",
        suitableSpecializations = listOf("Gunner", "Shooter", "All-Rounder")
    ),

    // Firearms: Assault Rifle, Grenade Launcher, Handgun, Shotgun
    TriggerDefinition(
        id = "assault_rifle",
        name = "Assault Rifle",
        category = TriggerCategory.FIREARMS,
        type = "Firearm / Rifle",
        description = "Súng trường tấn công tầm trung, tốc độ xả đạn ổn định và độ chính xác lý tưởng.",
        tag = "RIFLE",
        suitableSpecializations = listOf("Gunner", "All-Rounder")
    ),
    TriggerDefinition(
        id = "grenade_launcher",
        name = "Grenade Launcher",
        category = TriggerCategory.FIREARMS,
        type = "Firearm / Launcher",
        description = "Súng phóng lựu bắn đạn nổ Meteor tầm xa tạo hỏa lực áp đảo diện rộng.",
        tag = "LAUNCHER",
        suitableSpecializations = listOf("Gunner", "Shooter")
    ),
    TriggerDefinition(
        id = "handgun",
        name = "Handgun",
        category = TriggerCategory.FIREARMS,
        type = "Firearm / Pistol",
        description = "Súng ngắn gọn nhẹ, tốc độ rút và nhắm cực nhanh cho các tình huống bất ngờ.",
        tag = "PISTOL",
        suitableSpecializations = listOf("Gunner", "All-Rounder")
    ),
    TriggerDefinition(
        id = "shotgun",
        name = "Shotgun",
        category = TriggerCategory.FIREARMS,
        type = "Firearm / Shotgun",
        description = "Súng hoa cải tầm gần, uy lực tàn phá dội bão đạn trong một phát bắn.",
        tag = "SHOTGUN",
        suitableSpecializations = listOf("Gunner", "All-Rounder")
    ),

    // Sniper Triggers: Egret, Ibis, Lightning
    TriggerDefinition(
        id = "egret",
        name = "Egret",
        category = TriggerCategory.SNIPER,
        type = "Sniper / Balanced",
        description = "Súng bắn tỉa tiêu chuẩn cân bằng giữa tầm bắn, tốc độ bay và uy lực ngắm bắn.",
        tag = "STD SNIPER",
        suitableSpecializations = listOf("Sniper")
    ),
    TriggerDefinition(
        id = "ibis",
        name = "Ibis",
        category = TriggerCategory.SNIPER,
        type = "Sniper / Heavy",
        description = "Khẩu pháo bắn tỉa công phá hạng nặng, xuyên thủng khiên chắn và lớp giáp kiên cố.",
        tag = "HEAVY",
        suitableSpecializations = listOf("Sniper")
    ),
    TriggerDefinition(
        id = "lightning",
        name = "Lightning",
        category = TriggerCategory.SNIPER,
        type = "Sniper / Rapid Speed",
        description = "Súng bắn tỉa tốc độ đạn siêu thanh, cự ly ngắm tức thời giảm thiểu thời gian địch né tránh.",
        tag = "RAPID",
        suitableSpecializations = listOf("Sniper")
    ),

    // Trap triggers: Switchbox
    TriggerDefinition(
        id = "switchbox",
        name = "Switchbox",
        category = TriggerCategory.TRAP,
        type = "Trap / Switch",
        description = "Hộp bẫy liên kết chiến thuật của Trapper, giăng cạm bẫy dịch chuyển và kích nổ từ xa.",
        tag = "TRAP",
        suitableSpecializations = listOf("Trapper", "Operator")
    ),

    // Defense Triggers: Escudo, Shield
    TriggerDefinition(
        id = "escudo",
        name = "Escudo",
        category = TriggerCategory.DEFENSE,
        type = "Defense / Barrier",
        description = "Dựng bức tường rào chắn Trion kiên cố mọc lên từ mặt đất hoặc bề mặt bất kỳ.",
        tag = "WALL",
        suitableSpecializations = listOf("Attacker", "Gunner", "Shooter", "Sniper", "All-Rounder")
    ),
    TriggerDefinition(
        id = "shield",
        name = "Shield",
        category = TriggerCategory.DEFENSE,
        type = "Defense / Directional",
        description = "Tấm khiên Trion định hướng linh hoạt, có thể tập trung thu nhỏ để tăng độ cứng cáp.",
        tag = "SHIELD",
        suitableSpecializations = listOf("Attacker", "Gunner", "Shooter", "Sniper", "All-Rounder", "Operator")
    ),

    // Optional Triggers: Bagworm, Chameleon, Dummy Beacon, Grasshopper, Idaten, Spider, Teleporter, Lead Bullet
    TriggerDefinition(
        id = "bagworm",
        name = "Bagworm",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Stealth",
        description = "Áo choàng tàng hình tiêu thụ ít Trion, che giấu hoàn toàn phản xạ tín hiệu trên radar Border.",
        tag = "STEALTH",
        suitableSpecializations = listOf("Sniper", "Attacker", "Gunner", "Shooter", "All-Rounder")
    ),
    TriggerDefinition(
        id = "chameleon",
        name = "Chameleon",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Optical Stealth",
        description = "Tàng hình quang học hoàn toàn khỏi mắt nhìn thông thường trong môi trường thực địa.",
        tag = "CAMO",
        suitableSpecializations = listOf("Attacker", "Sniper")
    ),
    TriggerDefinition(
        id = "dummy_beacon",
        name = "Dummy Beacon",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Decoy",
        description = "Thiết bị tạo tín hiệu giả lập mục tiêu trên radar để đánh lừa đối phương.",
        tag = "DECOY",
        suitableSpecializations = listOf("Trapper", "Shooter", "Sniper")
    ),
    TriggerDefinition(
        id = "grasshopper",
        name = "Grasshopper",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Mobility",
        description = "Tạo bệ nhảy Trion đẩy bản thân hoặc đồng đội di chuyển lập tức với tốc độ cao.",
        tag = "MOBILITY",
        suitableSpecializations = listOf("Attacker", "All-Rounder", "Gunner", "Sniper")
    ),
    TriggerDefinition(
        id = "idaten",
        name = "Idaten",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Speed Boost",
        description = "Tăng tốc độ di chuyển trên một đường thẳng định trước với vận tốc siêu âm.",
        tag = "SPEED",
        suitableSpecializations = listOf("Attacker")
    ),
    TriggerDefinition(
        id = "spider",
        name = "Spider",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Wire Trap",
        description = "Giăng lưới dây Trion tàng hình cản bước kẻ thù hoặc làm điểm tựa di chuyển cho đồng đội.",
        tag = "WIRE",
        suitableSpecializations = listOf("Shooter", "All-Rounder", "Trapper")
    ),
    TriggerDefinition(
        id = "teleporter",
        name = "Teleporter",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Warp",
        description = "Dịch chuyển không gian tức thời ở cự ly ngắn (tối đa 30m) để thoát thân hoặc tập kích.",
        tag = "WARP",
        suitableSpecializations = listOf("Shooter", "Sniper", "Attacker")
    ),
    TriggerDefinition(
        id = "lead_bullet",
        name = "Lead Bullet",
        category = TriggerCategory.OPTIONAL,
        type = "Option / Heavy Bullet",
        description = "Đạn chì ghim trực tiếp vào cơ thể đối phương, không gây sát thương nhưng làm tăng trọng lượng tột độ.",
        tag = "LEAD",
        suitableSpecializations = listOf("Sniper", "Gunner", "Shooter")
    ),

    // Free Slot Option
    TriggerDefinition(
        id = "free_trigger",
        name = "FREE TRIGGER",
        category = TriggerCategory.OPTIONAL,
        type = "Empty / Unassigned",
        description = "Khe cắm dự phòng để trống, tiết kiệm dung lượng tiêu thụ Trion của Trigger Holder.",
        tag = "EMPTY",
        suitableSpecializations = listOf("Attacker", "Gunner", "Shooter", "Sniper", "All-Rounder", "Operator")
    )
)

fun getTriggerDefinitionByName(name: String): TriggerDefinition {
    val cleanName = name.trim().lowercase()
    return BORDER_TRIGGER_CATALOG.firstOrNull {
        it.name.equals(name.trim(), ignoreCase = true) ||
        cleanName.contains(it.id) ||
        cleanName.contains(it.name.lowercase())
    } ?: TriggerDefinition(
        id = name.lowercase().replace(" ", "_"),
        name = name,
        category = TriggerCategory.OPTIONAL,
        type = "Custom Trigger",
        description = "Trigger chuyên dụng đặc vụ Border",
        tag = "TRIGGER"
    )
}

data class AgentTriggerSet(
    val title: String,
    val mainTriggers: List<TriggerSlot>,
    val subTriggers: List<TriggerSlot>,
    val tacticalRole: String
)

fun TriggerDefinition.toTriggerSlot(slotNumber: Int): TriggerSlot {
    return TriggerSlot(
        slotNumber = slotNumber,
        name = name,
        type = type,
        description = description,
        tag = tag
    )
}


fun getParametersForPosition(position: String): List<ParameterItem> {
    return when (position.lowercase()) {
        "sniper" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 6),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 8),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 4),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 5),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 10),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 9),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 5),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 5)
        )
        "attacker" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 7),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 10),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 6),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 4),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 2),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 8),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 7),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 9)
        )
        "gunner" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 7),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 8),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 5),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 6),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 6),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 7),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 6),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 7)
        )
        "shooter" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 9),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 9),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 8),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 5),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 7),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 8),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 6),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 6)
        )
        "all-rounder" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 8),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 8),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 7),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 7),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 7),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 8),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 8),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 7)
        )
        "operator" -> listOf(
            ParameterItem(key = "Trion", displayLines = listOf("TRION"), value = 5),
            ParameterItem(key = "Attack", displayLines = listOf("ATTACK"), value = 1),
            ParameterItem(key = "Special Tactics", displayLines = listOf("SPECIAL", "TACTICS"), value = 9),
            ParameterItem(key = "Command", displayLines = listOf("COMMAND"), value = 10),
            ParameterItem(key = "Range", displayLines = listOf("RANGE"), value = 8),
            ParameterItem(key = "Skill", displayLines = listOf("SKILL"), value = 9),
            ParameterItem(key = "Defence/Support", displayLines = listOf("DEFENCE", "SUPPORT"), value = 7),
            ParameterItem(key = "Mobility", displayLines = listOf("MOBILITY"), value = 4)
        )
        else -> DEFAULT_PARAMETERS
    }
}

fun getTriggerSetForPosition(position: String, customTrigger: String? = null): AgentTriggerSet {
    return when (position.lowercase()) {
        "sniper" -> AgentTriggerSet(
            title = customTrigger ?: "Ibis & Egret Tactical Sniper Set",
            tacticalRole = "Bắn tỉa tầm xa xuyên giáp & áp chế hỏa lực từ cứ điểm",
            mainTriggers = listOf(
                TriggerSlot(1, "Ibis", "Main Weapon", "Pháo bắn tỉa hạng nặng siêu sát thương", "HEAVY"),
                TriggerSlot(2, "Egret", "Sub Weapon", "Súng bắn tỉa cân bằng cự ly chuẩn mực", "STD"),
                TriggerSlot(3, "Shield", "Defense", "Khiên bảo vệ Trion định hướng", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo choàng tàng hình vô hiệu radar", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Lightning", "Main Weapon", "Súng bắn tỉa siêu tốc độ cao", "RAPID"),
                TriggerSlot(6, "Grasshopper", "Option", "Bệ nhảy Trion di chuyển lập tức", "MOB"),
                TriggerSlot(7, "Shield", "Defense", "Khiên bảo vệ thứ cấp", "DEF"),
                TriggerSlot(8, "Free Slot / Radar", "Option", "Máy quét sóng mục tiêu cơ bản", "RADAR")
            )
        )
        "attacker" -> AgentTriggerSet(
            title = customTrigger ?: "Kogetsu & Scorpion Melee Set",
            tacticalRole = "Đột phá tiền tuyến, cận chiến tốc độ cao & hạ gục mục tiêu",
            mainTriggers = listOf(
                TriggerSlot(1, "Kogetsu", "Main Weapon", "Thanh kiếm Trion bền bỉ, độ sắc bén cực cao", "BLADE"),
                TriggerSlot(2, "Senku", "Option", "Tuyệt kỹ chém mở rộng tầm xa kiếm khí", "BURST"),
                TriggerSlot(3, "Shield", "Defense", "Khiên phòng ngự Trion", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo choàng tàng hình", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Scorpion", "Sub Weapon", "Lưỡi dao biến hóa tự do từ mọi điểm", "ASSASSIN"),
                TriggerSlot(6, "Grasshopper", "Option", "Bàn đạp tăng tốc nhảy bật", "MOB"),
                TriggerSlot(7, "Shield", "Defense", "Khiên Trion phụ trợ", "DEF"),
                TriggerSlot(8, "Chameleon", "Option", "Tàng hình tuyệt đối quang học", "STEALTH")
            )
        )
        "gunner" -> AgentTriggerSet(
            title = customTrigger ?: "Dual Gunner Submachine Set",
            tacticalRole = "Dội bão đạn liên hồi tầm trung, yểm trợ hỏa lực toàn đội",
            mainTriggers = listOf(
                TriggerSlot(1, "Submachine Gun", "Main Weapon", "Súng ngắn liên thanh đạn Asteroid", "RAPID"),
                TriggerSlot(2, "Asteroid", "Option", "Đạn Trion tiêu chuẩn không điều hướng", "AMMO"),
                TriggerSlot(3, "Shield", "Defense", "Khiên Trion mặt trước", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo choàng ẩn thân trên radar", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Shotgun", "Sub Weapon", "Hỏa lực súng hoa cải uy lực cự ly gần", "HEAVY"),
                TriggerSlot(6, "Lead Bullet", "Option", "Đạn chì ghim nặng khóa chuyển động địch", "HEAVY"),
                TriggerSlot(7, "Shield", "Defense", "Khiên chắn dự phòng", "DEF"),
                TriggerSlot(8, "Free Slot", "Option", "Khe cắm phụ trợ tác chiến", "AUX")
            )
        )
        "shooter" -> AgentTriggerSet(
            title = customTrigger ?: "Compound Trion Matrix Shooter Set",
            tacticalRole = "Tổng hợp khối đạn Trion ma trận phức hợp chia tách & vây hãm",
            mainTriggers = listOf(
                TriggerSlot(1, "Asteroid", "Main Weapon", "Khối lập phương Trion uy lực cơ bản", "AMMO"),
                TriggerSlot(2, "Hound", "Sub Weapon", "Đạn truy vết tự động khóa mục tiêu", "HOMING"),
                TriggerSlot(3, "Shield", "Defense", "Khiên phòng ngự Trion", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo tàng hình tránh radar", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Meteor", "Main Weapon", "Đạn nổ diện rộng phá hủy vật cản", "AOE"),
                TriggerSlot(6, "Viper", "Sub Weapon", "Đạn bay quỹ đạo biến ảo khó lường", "CURVE"),
                TriggerSlot(7, "Shield", "Defense", "Khiên phòng hộ phản lực", "DEF"),
                TriggerSlot(8, "Teleporter", "Option", "Dịch chuyển không gian cự ly ngắn", "WARP")
            )
        )
        "all-rounder" -> AgentTriggerSet(
            title = customTrigger ?: "Raygust & Gunner Heavy All-Rounder Set",
            tacticalRole = "Linh hoạt hoán chuyển giữa cận chiến khiên đẩy và đạn pháo tầm trung",
            mainTriggers = listOf(
                TriggerSlot(1, "Raygust", "Main Weapon", "Khiên Trion biến hình thành lưỡi đao", "SHIELD/BLADE"),
                TriggerSlot(2, "Thruster", "Option", "Tên lửa phản lực đẩy Raygust tốc độ cực hạn", "BOOST"),
                TriggerSlot(3, "Shield", "Defense", "Khiên phòng thủ phụ trợ", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo choàng tàng hình", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Handgun (Asteroid)", "Sub Weapon", "Súng lục bắn phát một chính xác cao", "AIM"),
                TriggerSlot(6, "Scorpion", "Sub Weapon", "Lưỡi dao cận chiến linh hoạt", "BLADE"),
                TriggerSlot(7, "Shield", "Defense", "Khiên phòng hộ", "DEF"),
                TriggerSlot(8, "Grasshopper", "Option", "Bệ nhảy gia tốc cơ động", "MOB")
            )
        )
        "operator" -> AgentTriggerSet(
            title = customTrigger ?: "Border HQ Strategic Terminal",
            tacticalRole = "Phân tích địa hình, phát hiện sớm Neighbor, kết nối liên lạc toàn chiến trường",
            mainTriggers = listOf(
                TriggerSlot(1, "HQ Radar", "Special", "Mạng quét diện rộng phủ toàn bộ Mikado City", "SCAN"),
                TriggerSlot(2, "Data Link", "Special", "Mạng chia sẻ giác quan & dữ liệu Trion đồng đội", "LINK"),
                TriggerSlot(3, "HQ Shield", "Defense", "Lớp rào chắn năng lượng phòng thủ tổng bộ", "SHIELD"),
                TriggerSlot(4, "Encrypted Comms", "Utility", "Kênh đàm thoại bảo mật tuyệt đối", "COMMS")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Tactical Map", "Special", "Bản đồ số 3D tái hiện mô phỏng chiến sự", "MAP"),
                TriggerSlot(6, "Emergency Bail Out", "Utility", "Cổng dịch chuyển rút lui khẩn cấp về Border", "BAILOUT"),
                TriggerSlot(7, "Trion Monitor", "Special", "Đo lường dung lượng năng lượng thời gian thực", "MONITOR"),
                TriggerSlot(8, "Terminal Override", "Option", "Can thiệp tái cấu trúc cấu hình Trigger từ xa", "CONFIG")
            )
        )
        else -> AgentTriggerSet(
            title = "Standard Border Trigger Set",
            tacticalRole = "Bộ trang bị tác chiến tiêu chuẩn của Border Defense Agency",
            mainTriggers = listOf(
                TriggerSlot(1, "Ibis", "Main Weapon", "Súng bắn tỉa hạng nặng", "MAIN"),
                TriggerSlot(2, "Egret", "Sub Weapon", "Súng bắn tỉa chuẩn", "SUB"),
                TriggerSlot(3, "Shield", "Defense", "Khiên phòng ngự", "DEF"),
                TriggerSlot(4, "Bagworm", "Option", "Áo tàng hình", "STEALTH")
            ),
            subTriggers = listOf(
                TriggerSlot(5, "Lightning", "Main Weapon", "Súng tốc độ", "RAPID"),
                TriggerSlot(6, "Grasshopper", "Option", "Bệ nhảy", "MOB"),
                TriggerSlot(7, "Shield", "Defense", "Khiên dự phòng", "DEF"),
                TriggerSlot(8, "Radar", "Option", "Máy quét", "RADAR")
            )
        )
    }
}

