package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.model.computeHistoryStats
import com.example.model.generateLeaderboardWithUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Border Agent OS", appName)
  }

  @Test
  fun `history stats calculation test`() {
    val matches = listOf(
      SoloRankMatchEntity(
        id = 1,
        agentName = "Belén Salinas",
        opponentName = "Border Combat AI",
        opponentRank = "B-RANK",
        playerRoll = 45,
        enemyRoll = 20,
        won = true,
        pointsDiff = 45,
        finalPoints = 1045,
        tacticNote = "Snipe"
      ),
      SoloRankMatchEntity(
        id = 2,
        agentName = "Belén Salinas",
        opponentName = "Ko Murakami",
        opponentRank = "A-RANK",
        playerRoll = 15,
        enemyRoll = 35,
        won = false,
        pointsDiff = 15,
        finalPoints = 1030,
        tacticNote = "Flanked"
      )
    )

    val stats = computeHistoryStats(matches)
    assertEquals(2, stats.totalMatches)
    assertEquals(1, stats.wins)
    assertEquals(1, stats.losses)
    assertEquals(50, stats.winRatePercent)
    assertEquals(45, stats.highestRoll)
    assertEquals(45, stats.totalPointsGained)
    assertEquals(15, stats.totalPointsLost)
  }

  @Test
  fun `leaderboard placement test`() {
    val board = generateLeaderboardWithUser(
      userName = "Belén Salinas",
      userPoints = 12000,
      userWinRate = "80%"
    )

    val userEntry = board.firstOrNull { it.isUser }
    assertNotNull(userEntry)
    assertTrue(userEntry!!.points == 12000)
    // Points 12,000 should put user in top ranks
    assertTrue(userEntry.rankPosition in 1..10)
  }

  @Test
  fun `room database solo rank match insertion test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.soloRankMatchDao()
    val match = SoloRankMatchEntity(
      agentName = "Belén Salinas",
      opponentName = "Isami Toma",
      opponentRank = "A-RANK",
      playerRoll = 48,
      enemyRoll = 22,
      won = true,
      pointsDiff = 48,
      finalPoints = 1048,
      tacticNote = "Bagworm snipe"
    )

    val id = dao.insertMatch(match)
    assertTrue(id > 0)

    val list = dao.getAllMatches().first()
    assertEquals(1, list.size)
    assertEquals("Isami Toma", list[0].opponentName)
    assertEquals(48, list[0].playerRoll)
    assertTrue(list[0].won)

    db.close()
  }

  @Test
  fun `agent profile creation and repository persistence test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.AgentRepository(context)

    val customProfile = com.example.model.AgentProfile(
      name = "Yuma Kuga",
      age = 15,
      gender = "Nam",
      position = "Attacker",
      pictureUri = "content://media/external/images/media/42",
      isRegistered = true
    )

    repository.saveAgentProfile(customProfile)

    val retrieved = repository.getAgentProfile()
    assertEquals("Yuma Kuga", retrieved.name)
    assertEquals(15, retrieved.age)
    assertEquals("Nam", retrieved.gender)
    assertEquals("Attacker", retrieved.position)
    assertEquals("content://media/external/images/media/42", retrieved.pictureUri)
    assertTrue(retrieved.isRegistered)
    assertTrue(repository.isProfileRegistered())

    // Test picture update
    repository.updatePictureUri("content://media/external/images/media/99")
    val updated = repository.getAgentProfile()
    assertEquals("content://media/external/images/media/99", updated.pictureUri)
  }

  @Test
  fun `radar chart stats and trigger set resolution for positions`() {
    val sniperParams = com.example.model.getParametersForPosition("Sniper")
    val rangeItem = sniperParams.first { it.key == "Range" }
    assertEquals(10, rangeItem.value)

    val attackerParams = com.example.model.getParametersForPosition("Attacker")
    val attackItem = attackerParams.first { it.key == "Attack" }
    assertEquals(10, attackItem.value)
    val mobilityItem = attackerParams.first { it.key == "Mobility" }
    assertEquals(9, mobilityItem.value)

    val triggerSet = com.example.model.getTriggerSetForPosition("Sniper", "Ibis (Sniper)")
    assertEquals(4, triggerSet.mainTriggers.size)
    assertEquals(4, triggerSet.subTriggers.size)
    assertEquals("Ibis", triggerSet.mainTriggers[0].name)
    assertEquals("Egret", triggerSet.mainTriggers[1].name)
    assertEquals("Shield", triggerSet.mainTriggers[2].name)
    assertEquals("Bagworm", triggerSet.mainTriggers[3].name)
  }

  @Test
  fun `border trigger catalog categories and specializations verification`() {
    val catalog = com.example.model.BORDER_TRIGGER_CATALOG

    // Check Attacker Triggers: Kogetsu, Raygust, Scorpion
    val attackerTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.ATTACKER }
    val attackerNames = attackerTriggers.map { it.name }
    assertTrue(attackerNames.contains("Kogetsu"))
    assertTrue(attackerNames.contains("Raygust"))
    assertTrue(attackerNames.contains("Scorpion"))

    // Check Gunner Triggers: Asteroid, Hound, Meteor, Viper
    val gunnerTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.GUNNER }
    val gunnerNames = gunnerTriggers.map { it.name }
    assertTrue(gunnerNames.contains("Asteroid"))
    assertTrue(gunnerNames.contains("Hound"))
    assertTrue(gunnerNames.contains("Meteor"))
    assertTrue(gunnerNames.contains("Viper"))

    // Check Firearms: Assault Rifle, Grenade Launcher, Handgun, Shotgun
    val firearms = catalog.filter { it.category == com.example.model.TriggerCategory.FIREARMS }
    val firearmNames = firearms.map { it.name }
    assertTrue(firearmNames.contains("Assault Rifle"))
    assertTrue(firearmNames.contains("Grenade Launcher"))
    assertTrue(firearmNames.contains("Handgun"))
    assertTrue(firearmNames.contains("Shotgun"))

    // Check Sniper Triggers: Egret, Ibis, Lightning
    val sniperTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.SNIPER }
    val sniperNames = sniperTriggers.map { it.name }
    assertTrue(sniperNames.contains("Egret"))
    assertTrue(sniperNames.contains("Ibis"))
    assertTrue(sniperNames.contains("Lightning"))

    // Check Trap triggers: Switchbox
    val trapTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.TRAP }
    val trapNames = trapTriggers.map { it.name }
    assertTrue(trapNames.contains("Switchbox"))

    // Check Defense Triggers: Escudo, Shield
    val defenseTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.DEFENSE }
    val defenseNames = defenseTriggers.map { it.name }
    assertTrue(defenseNames.contains("Escudo"))
    assertTrue(defenseNames.contains("Shield"))

    // Check Optional Triggers: Bagworm, Chameleon, Dummy Beacon, Grasshopper, Idaten, Spider, Teleporter, Lead Bullet
    val optionalTriggers = catalog.filter { it.category == com.example.model.TriggerCategory.OPTIONAL }
    val optionalNames = optionalTriggers.map { it.name }
    assertTrue(optionalNames.contains("Bagworm"))
    assertTrue(optionalNames.contains("Chameleon"))
    assertTrue(optionalNames.contains("Dummy Beacon"))
    assertTrue(optionalNames.contains("Grasshopper"))
    assertTrue(optionalNames.contains("Idaten"))
    assertTrue(optionalNames.contains("Spider"))
    assertTrue(optionalNames.contains("Teleporter"))
    assertTrue(optionalNames.contains("Lead Bullet"))
  }

  @Test
  fun `trigger customization rank requirement test`() {
    // C-Rank (1000 points) cannot customize
    val cRankState = com.example.ui.AgentUiState(trionPoints = 1000)
    assertEquals(false, cRankState.canCustomizeTriggers)

    // B-Rank (4000 points) CAN customize
    val bRankState = com.example.ui.AgentUiState(trionPoints = 4000)
    assertEquals(true, bRankState.canCustomizeTriggers)

    // A-Rank (8000 points) CAN customize
    val aRankState = com.example.ui.AgentUiState(trionPoints = 8000)
    assertEquals(true, aRankState.canCustomizeTriggers)
  }

  @Test
  fun `repository custom trigger set persistence test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.AgentRepository(context)

    val customSet = com.example.model.AgentTriggerSet(
      title = "Custom Attack & Mobility Set",
      tacticalRole = "Đột kích cự ly gần",
      mainTriggers = listOf(
        com.example.model.TriggerSlot(1, "Scorpion", "Main Weapon", "Lưỡi dao", "ASSASSIN"),
        com.example.model.TriggerSlot(2, "Grasshopper", "Option", "Bệ nhảy", "MOB"),
        com.example.model.TriggerSlot(3, "Shield", "Defense", "Khiên", "DEF"),
        com.example.model.TriggerSlot(4, "Bagworm", "Option", "Áo choàng", "STEALTH")
      ),
      subTriggers = listOf(
        com.example.model.TriggerSlot(5, "Escudo", "Defense", "Rào chắn", "WALL"),
        com.example.model.TriggerSlot(6, "Teleporter", "Option", "Dịch chuyển", "WARP"),
        com.example.model.TriggerSlot(7, "Shield", "Defense", "Khiên", "DEF"),
        com.example.model.TriggerSlot(8, "FREE TRIGGER", "Option", "Trống", "EMPTY")
      )
    )

    repository.saveCustomTriggerSet(customSet)
    val retrieved = repository.getCustomTriggerSet()
    assertNotNull(retrieved)
    assertEquals("Custom Attack & Mobility Set", retrieved!!.title)
    assertEquals("Scorpion", retrieved.mainTriggers[0].name)
    assertEquals("Escudo", retrieved.subTriggers[0].name)
    assertEquals("Teleporter", retrieved.subTriggers[1].name)

    // Clear / reset
    repository.saveCustomTriggerSet(null)
    val cleared = repository.getCustomTriggerSet()
    assertEquals(null, cleared)
  }

  @Test
  fun `pomodoro state progress and repository logging test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.AgentRepository(context)

    // Verify initial Pomodoro state logic
    val pomodoroState = com.example.model.PomodoroState(
      phase = com.example.model.PomodoroPhase.FOCUS,
      remainingSeconds = 15 * 60, // 10 minutes elapsed of 25 min total
      totalSecondsForPhase = 25 * 60
    )
    val expectedProgress = 10f / 25f
    assertTrue(Math.abs(pomodoroState.progressFraction - expectedProgress) < 0.01f)

    // Test session logs persistence
    val sessionLog = com.example.model.PomodoroSessionLog(
      subject = "Ngoại ngữ Sniper",
      durationMinutes = 25,
      cycleNumber = 1,
      earnedPoints = 2
    )
    repository.savePomodoroLogs(listOf(sessionLog))
    repository.savePomodoroTotalCount(3)

    val retrievedLogs = repository.getPomodoroLogs()
    assertEquals(1, retrievedLogs.size)
    assertEquals("Ngoại ngữ Sniper", retrievedLogs[0].subject)
    assertEquals(2, retrievedLogs[0].earnedPoints)
    assertEquals(3, repository.getPomodoroTotalCount())
  }

  @Test
  fun `room study task dao operations test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val studyTaskDao = db.studyTaskDao()

    // Insert task
    val task = com.example.data.local.entity.StudyTaskEntity(
      title = "Học 20 từ vựng Radar Trion",
      category = "Ngoại ngữ",
      targetPomodoros = 2,
      completedPomodoros = 0,
      isCompleted = false
    )
    val taskId = studyTaskDao.insertTask(task)
    assertTrue(taskId > 0)

    // Retrieve task by id
    val fetched = studyTaskDao.getTaskById(taskId)
    assertNotNull(fetched)
    assertEquals("Học 20 từ vựng Radar Trion", fetched!!.title)
    assertEquals(2, fetched.targetPomodoros)
    assertEquals(0, fetched.completedPomodoros)
    assertEquals(false, fetched.isCompleted)

    // Increment Pomodoro count
    studyTaskDao.incrementTaskPomodoro(taskId)
    val afterPomodoro = studyTaskDao.getTaskById(taskId)
    assertEquals(1, afterPomodoro!!.completedPomodoros)

    // Complete task
    studyTaskDao.updateTaskCompletion(taskId, true, System.currentTimeMillis())
    val completedTask = studyTaskDao.getTaskById(taskId)
    assertTrue(completedTask!!.isCompleted)
    assertNotNull(completedTask.completedAt)

    // Delete task
    studyTaskDao.deleteTaskById(taskId)
    val deleted = studyTaskDao.getTaskById(taskId)
    assertEquals(null, deleted)

    db.close()
  }

  @Test
  fun `pomodoro timer room task tracking integration test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.studyTaskDao()

    // 1. Create a task in Room
    val task = com.example.data.local.entity.StudyTaskEntity(
      title = "Giải mã tín hiệu Radar Aftokrator",
      category = "Chiến thuật",
      targetPomodoros = 3,
      completedPomodoros = 0
    )
    val taskId = dao.insertTask(task)

    // 2. Simulate focus session completion on the selected active task
    dao.incrementTaskPomodoro(taskId)
    var updated = dao.getTaskById(taskId)
    assertNotNull(updated)
    assertEquals(1, updated!!.completedPomodoros)

    // 3. Increment again
    dao.incrementTaskPomodoro(taskId)
    updated = dao.getTaskById(taskId)
    assertEquals(2, updated!!.completedPomodoros)

    // 4. Increment to target
    dao.incrementTaskPomodoro(taskId)
    updated = dao.getTaskById(taskId)
    assertEquals(3, updated!!.completedPomodoros)
    assertTrue(updated.completedPomodoros >= updated.targetPomodoros)

    db.close()
  }

  @Test
  fun `study task custom tags and dao filtering test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.studyTaskDao()

    val task1 = com.example.data.local.entity.StudyTaskEntity(
      title = "Implement Room Migration and Tag Queries",
      category = "Coding",
      tags = "Coding, Android, Architecture",
      targetPomodoros = 2
    )
    val task2 = com.example.data.local.entity.StudyTaskEntity(
      title = "Read World Trigger Tactics Document",
      category = "Reading",
      tags = "Reading, Strategy, Tactics",
      targetPomodoros = 3
    )
    val task3 = com.example.data.local.entity.StudyTaskEntity(
      title = "Practice Kotlin Coroutines Flow",
      category = "Coding",
      tags = "Coding, Kotlin, Async",
      targetPomodoros = 1
    )

    dao.insertTask(task1)
    dao.insertTask(task2)
    dao.insertTask(task3)

    // Verify tagList parsed properly
    assertTrue(task1.tagList.contains("Coding"))
    assertTrue(task1.tagList.contains("Android"))
    assertTrue(task2.tagList.contains("Reading"))
    assertTrue(task2.tagList.contains("Strategy"))

    // Verify DAO filtering by tag
    val codingTasks = dao.getTasksByTag("Coding").first()
    assertEquals(2, codingTasks.size)

    val readingTasks = dao.getTasksByTag("Reading").first()
    assertEquals(1, readingTasks.size)
    assertEquals("Read World Trigger Tactics Document", readingTasks.first().title)

    val strategyTasks = dao.getFilteredTasks("Strategy").first()
    assertEquals(1, strategyTasks.size)

    val allCategories = dao.getAllCategories().first()
    assertTrue(allCategories.contains("Coding"))
    assertTrue(allCategories.contains("Reading"))

    db.close()
  }

  @Test
  fun `timer sound manager and notification preference test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = com.example.util.TimerSoundManager(context)

    // Verify safe playback and release without crashing
    soundManager.playTestSound()
    soundManager.playFocusSessionEndSound()
    soundManager.stopAndReleasePlayer()

    val repository = com.example.data.AgentRepository(context)
    repository.saveTimerSoundEnabled(true)
    assertTrue(repository.isTimerSoundEnabled())

    repository.saveTimerSoundEnabled(false)
    assertFalse(repository.isTimerSoundEnabled())

    repository.saveTimerSoundEnabled(true)
  }

  @Test
  fun `rank cloud sync service instantiation and graceful offline test`() = runBlocking {
    val service = com.example.data.RankCloudSyncService()
    assertNotNull(service)

    val profile = com.example.model.AgentProfile(name = "Test Agent", position = "Sniper")
    // In unit test environment without active Firebase backend, sync fails gracefully without throwing
    val result = service.syncUserRankData(profile = profile, points = 4500, matchesWon = 5, totalMatches = 8)
    assertNotNull(result)
  }

  @Test
  fun `room database daily solo rank missions tracking test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = inMemoryDb.dailySoloRankMissionDao()

    val testDate = "2026-09-17"
    val mission = com.example.data.local.entity.DailySoloRankMissionEntity(
      title = "Tham gia 2 trận Solo Rank War",
      description = "Thử nghiệm chiến thuật mới tại sàn đấu Rank",
      targetCount = 2,
      currentProgress = 0,
      rewardPoints = 150,
      isCompleted = false,
      dateString = testDate,
      missionCategory = "SOLO_WAR"
    )

    val id = dao.insertMission(mission)
    assertTrue(id > 0)

    val missionsSync = dao.getMissionsByDateSync(testDate)
    assertEquals(1, missionsSync.size)

    val missions = dao.getMissionsByDate(testDate).first()
    assertEquals(1, missions.size)
    assertEquals("Tham gia 2 trận Solo Rank War", missions[0].title)
    assertFalse(missions[0].isCompleted)

    // Progress update
    dao.updateProgressAndCompletion(id, 2, true, System.currentTimeMillis())
    val updatedMission = dao.getMissionById(id)
    assertNotNull(updatedMission)
    assertEquals(2, updatedMission?.currentProgress)
    assertTrue(updatedMission?.isCompleted == true)

    // Complete toggle
    dao.updateCompletionStatus(id, false, null)
    val toggledMission = dao.getMissionById(id)
    assertFalse(toggledMission?.isCompleted == true)

    inMemoryDb.close()
  }

  @Test
  fun `daily mission reminder preference and notification scheduling test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.AgentRepository(context)

    // Test default reminder values
    val defaultTime = repository.getDailyMissionReminderTime()
    assertEquals(20, defaultTime.first)
    assertEquals(0, defaultTime.second)
    assertTrue(repository.isDailyMissionReminderEnabled())

    // Update reminder settings
    repository.setDailyMissionReminderTime(21, 30)
    val updatedTime = repository.getDailyMissionReminderTime()
    assertEquals(21, updatedTime.first)
    assertEquals(30, updatedTime.second)

    repository.setDailyMissionReminderEnabled(false)
    assertFalse(repository.isDailyMissionReminderEnabled())

    // Test notification channel creation
    com.example.util.DailyMissionNotificationManager.createNotificationChannel(context)

    // Test scheduling and cancellation
    com.example.util.DailyMissionNotificationManager.scheduleDailyReminder(context, 21, 30)
    com.example.util.DailyMissionNotificationManager.cancelDailyReminder(context)
  }

  @Test
  fun `mission reward sound manager initializes and plays without error`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = com.example.util.MissionRewardSoundManager(context)
    // Verify playback and release do not throw exceptions in JVM environment
    soundManager.playMissionCompletedSound()
    soundManager.release()
  }

  @Test
  fun `room database completed missions history query test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = inMemoryDb.dailySoloRankMissionDao()

    val completedMission = com.example.data.local.entity.DailySoloRankMissionEntity(
      title = "Hoàn thành 30 phút rèn luyện Trion",
      description = "Duy trì trạng thái tập trung sâu",
      targetCount = 1,
      currentProgress = 1,
      rewardPoints = 100,
      isCompleted = true,
      dateString = "2026-09-17",
      completedAt = System.currentTimeMillis(),
      missionCategory = "TRION_TRAINING"
    )
    val incompleteMission = com.example.data.local.entity.DailySoloRankMissionEntity(
      title = "Chiến thắng 1 trận Solo Rank",
      description = "Hạ gục đối thủ",
      targetCount = 1,
      currentProgress = 0,
      rewardPoints = 200,
      isCompleted = false,
      dateString = "2026-09-17",
      completedAt = null,
      missionCategory = "SOLO_WAR"
    )

    dao.insertMission(completedMission)
    dao.insertMission(incompleteMission)

    val completedList = dao.getCompletedMissions().first()
    assertEquals(1, completedList.size)
    assertEquals("Hoàn thành 30 phút rèn luyện Trion", completedList[0].title)
    assertTrue(completedList[0].isCompleted)

    inMemoryDb.close()
  }

  @Test
  fun `mission csv exporter generates valid formatted csv with headers and escapes`() {
    val missions = listOf(
      com.example.data.local.entity.DailySoloRankMissionEntity(
        id = 101,
        title = "Chiến thắng 2 trận Rank, bảo toàn điểm số",
        description = "Sử dụng chiến thuật \"Kugisaki Snipe\", phản công nhanh",
        targetCount = 2,
        currentProgress = 2,
        rewardPoints = 250,
        isCompleted = true,
        dateString = "2026-09-18",
        completedAt = 1774000000000L,
        createdAt = 1773990000000L,
        missionCategory = "SOLO_WAR",
        customNotes = "Ghi chú: Giữ khoảng cách tầm trung",
        customTags = "Solo War, Kogetsu, Sniper"
      ),
      com.example.data.local.entity.DailySoloRankMissionEntity(
        id = 102,
        title = "Rèn luyện Pomodoro 45 phút",
        description = "Không gián đoạn",
        targetCount = 1,
        currentProgress = 1,
        rewardPoints = 150,
        isCompleted = true,
        dateString = "2026-09-18",
        completedAt = 1774010000000L,
        createdAt = 1773995000000L,
        missionCategory = "FOCUS_TRAINING",
        customNotes = "Tập trung cao độ",
        customTags = "Pomodoro, Trion"
      )
    )

    val csv = com.example.util.MissionCsvExporter.generateCsv(missions)
    assertTrue(csv.contains("ID,Ngay,Tieu De,Danh Muc,Mo Ta,Ghi Chu (Notes),Nhan Tag (Tags),Tien Do,Muc Tieu,Trang Thai,Diem Thuong (PTS),Thoi Gian Hoan Tat,Thoi Gian Tao"))
    // Verify comma escaping with quotes
    assertTrue(csv.contains("\"Chiến thắng 2 trận Rank, bảo toàn điểm số\""))
    // Verify quotes inside description escaped
    assertTrue(csv.contains("\"Sử dụng chiến thuật \"\"Kugisaki Snipe\"\", phản công nhanh\""))
    // Verify custom notes and tags exported
    assertTrue(csv.contains("Ghi chú: Giữ khoảng cách tầm trung"))
    assertTrue(csv.contains("\"Solo War, Kogetsu, Sniper\""))
    // Verify category mapping
    assertTrue(csv.contains("Đấu Rank (Solo War)"))
    assertTrue(csv.contains("Rèn luyện Pomodoro"))
    assertTrue(csv.contains("250"))
    assertTrue(csv.contains("HOÀN TẤT"))

    val fileName = com.example.util.MissionCsvExporter.getDefaultFileName()
    assertTrue(fileName.startsWith("border_missions_history_"))
    assertTrue(fileName.endsWith(".csv"))

    val bom = com.example.util.MissionCsvExporter.UTF8_BOM
    assertEquals(3, bom.size)
    assertEquals(0xEF.toByte(), bom[0])
    assertEquals(0xBB.toByte(), bom[1])
    assertEquals(0xBF.toByte(), bom[2])
  }

  @Test
  fun `default source files provider has valid project structure`() {
    val defaults = com.example.data.DefaultSourceFiles.getInitialProjectFiles()
    assertTrue(defaults.isNotEmpty())
    assertTrue(defaults.size >= 8)

    val fileNames = defaults.map { it.fileName }
    assertTrue(fileNames.contains("AgentViewModel.kt"))
    assertTrue(fileNames.contains("TriggerModel.kt"))
    assertTrue(fileNames.contains("build.gradle.kts"))
    assertTrue(fileNames.contains("strings.xml"))
    assertTrue(fileNames.contains("metadata.json"))

    // Ensure all have non-empty content
    defaults.forEach { file ->
      assertTrue(file.content.isNotBlank())
      assertEquals(file.content, file.originalContent)
      assertFalse(file.isModified)
      assertNotNull(file.parentDir)
    }
  }

  @Test
  fun `room database source file dao operations test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.sourceFileDao()
    assertEquals(0, dao.getFilesCount())

    val testFile = com.example.data.local.entity.SourceFileEntity(
      filePath = "app/src/main/java/com/example/CustomTrigger.kt",
      fileName = "CustomTrigger.kt",
      fileExtension = "kt",
      parentDir = "app/src/main/java/com/example",
      content = "class CustomTrigger { fun snipe() = 100 }",
      originalContent = "class CustomTrigger { fun snipe() = 100 }",
      isModified = false,
      lastModifiedTimestamp = System.currentTimeMillis()
    )

    val id = dao.insertFile(testFile)
    assertTrue(id > 0)
    assertEquals(1, dao.getFilesCount())

    val loaded = dao.getFileById(id)
    assertNotNull(loaded)
    assertEquals("CustomTrigger.kt", loaded!!.fileName)
    assertEquals("kt", loaded.fileExtension)
    assertFalse(loaded.isModified)

    // Update content
    val updated = loaded.copy(
      content = "class CustomTrigger { fun snipe() = 250 }",
      isModified = true,
      lastModifiedTimestamp = System.currentTimeMillis()
    )
    dao.updateFile(updated)

    val reloaded = dao.getFileById(id)
    assertNotNull(reloaded)
    assertEquals("class CustomTrigger { fun snipe() = 250 }", reloaded!!.content)
    assertTrue(reloaded.isModified)

    // Delete
    dao.deleteFileById(id)
    assertEquals(0, dao.getFilesCount())

    db.close()
  }

  @Test
  fun `editor line count and gutter width calculation test`() {
    val sampleCode = """
      package com.example
      
      class TestTrigger {
          val power = 100
          fun activate() = true
      }
    """.trimIndent()

    val lines = sampleCode.split("\n")
    assertEquals(6, lines.size)

    val gutterWidthSmall = when {
      lines.size >= 10000 -> 56
      lines.size >= 1000 -> 48
      lines.size >= 100 -> 42
      else -> 38
    }
    assertEquals(38, gutterWidthSmall)

    val gutterWidthLarge = when {
      1250 >= 10000 -> 56
      1250 >= 1000 -> 48
      1250 >= 100 -> 42
      else -> 38
    }
    assertEquals(48, gutterWidthLarge)
  }

  @Test
  fun `auto indent preserves indentation on enter`() {
    val oldVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    val attackPower = 80",
      selection = androidx.compose.ui.text.TextRange(24)
    )
    val newVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    val attackPower = 80\n",
      selection = androidx.compose.ui.text.TextRange(25)
    )

    val result = com.example.ui.components.codeeditor.AutoIndentHelper.computeAutoIndent(oldVal, newVal)
    assertEquals("    val attackPower = 80\n    ", result.text)
    assertEquals(29, result.selection.start)
    assertEquals(29, result.selection.end)
  }

  @Test
  fun `auto indent increases indentation when opening brace entered`() {
    val oldVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    class KogetsuTrigger {",
      selection = androidx.compose.ui.text.TextRange(26)
    )
    val newVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    class KogetsuTrigger {\n",
      selection = androidx.compose.ui.text.TextRange(27)
    )

    val result = com.example.ui.components.codeeditor.AutoIndentHelper.computeAutoIndent(oldVal, newVal)
    // 4 base spaces + 4 nested spaces = 8 spaces
    assertEquals("    class KogetsuTrigger {\n        ", result.text)
    assertEquals(35, result.selection.start)
  }

  @Test
  fun `auto indent expands matching braces with indented line between`() {
    val oldVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    fun snipe() {}",
      selection = androidx.compose.ui.text.TextRange(17) // cursor between { and }
    )
    val newVal = androidx.compose.ui.text.input.TextFieldValue(
      text = "    fun snipe() {\n}",
      selection = androidx.compose.ui.text.TextRange(18)
    )

    val result = com.example.ui.components.codeeditor.AutoIndentHelper.computeAutoIndent(oldVal, newVal)
    val expected = "    fun snipe() {\n        \n    }"
    assertEquals(expected, result.text)
    // Cursor should be at the end of the indented second line (index 26)
    assertEquals(26, result.selection.start)
  }

  @Test
  fun `auto indent helper manual indent and unindent operations`() {
    val val1 = androidx.compose.ui.text.input.TextFieldValue(
      text = "val x = 1",
      selection = androidx.compose.ui.text.TextRange(0)
    )
    val indented = com.example.ui.components.codeeditor.AutoIndentHelper.indent(val1)
    assertEquals("    val x = 1", indented.text)
    assertEquals(4, indented.selection.start)

    val unindented = com.example.ui.components.codeeditor.AutoIndentHelper.unindent(indented)
    assertEquals("val x = 1", unindented.text)
    assertEquals(0, unindented.selection.start)
  }

  @Test
  fun `syntax highlighting parser highlights keywords`() {
    val code = "package com.example\nfun executeAttack(): Boolean { return true }"
    val annotated = com.example.ui.components.codeeditor.SyntaxHighlightingParser.parse(code, "kt")

    assertEquals(code, annotated.text)
    // Find span styles applied
    val spanStyles = annotated.spanStyles
    assertTrue("Should have span styles applied", spanStyles.isNotEmpty())

    // "package" starts at 0, ends at 7
    val packageSpan = spanStyles.find { it.start == 0 && it.end == 7 }
    assertNotNull("Should style 'package' keyword", packageSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorKeyword, packageSpan?.item?.color)

    // "fun" starts at 20, ends at 23
    val funSpan = spanStyles.find { it.start == 20 && it.end == 23 }
    assertNotNull("Should style 'fun' keyword", funSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorKeyword, funSpan?.item?.color)

    // "return" starts at 51, ends at 57
    val returnSpan = spanStyles.find { it.start == 51 && it.end == 57 }
    assertNotNull("Should style 'return' keyword", returnSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorKeyword, returnSpan?.item?.color)
  }

  @Test
  fun `syntax highlighting parser highlights strings and comments`() {
    val code = "// This is an agent comment\nval message = \"Border Defense Trigger\"\n/* multi\nline */"
    val annotated = com.example.ui.components.codeeditor.SyntaxHighlightingParser.parse(code, "kt")

    assertEquals(code, annotated.text)
    val spanStyles = annotated.spanStyles

    // Single-line comment starts at 0, ends at 27
    val commentSpan = spanStyles.find { it.start == 0 && it.end == 27 }
    assertNotNull("Should style single-line comment", commentSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorComment, commentSpan?.item?.color)

    // String literal "Border Defense Trigger" starts at 42, ends at 66
    val stringSpan = spanStyles.find { it.start == 42 && it.end == 66 }
    assertNotNull("Should style double-quoted string", stringSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorString, stringSpan?.item?.color)

    // Multi-line comment starts at 67, ends at 83
    val blockCommentSpan = spanStyles.find { it.start == 67 && it.end == 83 }
    assertNotNull("Should style multi-line comment", blockCommentSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorComment, blockCommentSpan?.item?.color)
  }

  @Test
  fun `syntax highlighting visual transformation preserves cursor mapping`() {
    val code = "val trionPoints = 9500"
    val visualTransformation = com.example.ui.components.codeeditor.CodeSyntaxVisualTransformation("kt")
    val transformed = visualTransformation.filter(androidx.compose.ui.text.AnnotatedString(code))

    assertEquals(code, transformed.text.text)
    // Offset mapping must be identity 1:1
    assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
    assertEquals(10, transformed.offsetMapping.originalToTransformed(10))
    assertEquals(code.length, transformed.offsetMapping.originalToTransformed(code.length))
    assertEquals(10, transformed.offsetMapping.transformedToOriginal(10))
  }

  @Test
  fun `syntax highlighting parser highlights annotations and search query`() {
    val code = "@Composable\nfun RadarMapScreen() {}"
    val annotated = com.example.ui.components.codeeditor.SyntaxHighlightingParser.parse(
      text = code,
      fileExtension = "kt",
      searchQuery = "Radar"
    )

    val spanStyles = annotated.spanStyles
    // Annotation @Composable starts at 0, ends at 11
    val annotSpan = spanStyles.find { it.start == 0 && it.end == 11 }
    assertNotNull("Should style annotation", annotSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorAnnotation, annotSpan?.item?.color)

    // Search match for "Radar" starts at 16, ends at 21
    val searchSpan = spanStyles.find { it.start == 16 && it.end == 21 }
    assertNotNull("Should highlight search query match", searchSpan)
    assertEquals(com.example.ui.components.codeeditor.SyntaxHighlightingParser.ColorSearchBg, searchSpan?.item?.background)
  }

  @Test
  fun `code diagnostics service detects unbalanced delimiters as syntax error`() {
    val invalidCode = "class BorderAgent {\n  fun shoot() {\n    val active = true\n"
    val diagnostics = com.example.ui.components.codeeditor.CodeDiagnosticsService.analyze(invalidCode, "kt")

    assertTrue("Diagnostics should not be empty", diagnostics.isNotEmpty())
    val braceError = diagnostics.find { it.rule == "SYNTAX_UNBALANCED_DELIM" || it.rule == "SYNTAX_UNCLOSED_DELIM" }
    assertNotNull("Should detect unclosed brace", braceError)
    assertEquals(com.example.ui.components.codeeditor.DiagnosticSeverity.ERROR, braceError?.severity)
    assertTrue(braceError?.message?.contains("ngoặc nhọn", ignoreCase = true) == true)
  }

  @Test
  fun `code diagnostics service detects unclosed strings as syntax error`() {
    val unclosedStringCode = "val triggerName = \"Kogetsu Blade\nval rank = 10"
    val diagnostics = com.example.ui.components.codeeditor.CodeDiagnosticsService.analyze(unclosedStringCode, "kt")

    val stringError = diagnostics.find { it.rule == "SYNTAX_UNCLOSED_STRING" }
    assertNotNull("Should detect unclosed string literal", stringError)
    assertEquals(com.example.ui.components.codeeditor.DiagnosticSeverity.ERROR, stringError?.severity)
    assertEquals(1, stringError?.line)
  }

  @Test
  fun `code diagnostics service detects assignment in if condition as logic warning`() {
    val logicErrorCode = "fun checkStatus(hp: Int) {\n  if (hp = 0) {\n    bailOut()\n  }\n}"
    val diagnostics = com.example.ui.components.codeeditor.CodeDiagnosticsService.analyze(logicErrorCode, "kt")

    val assignmentWarning = diagnostics.find { it.rule == "LOGIC_ASSIGNMENT_IN_CONDITION" }
    assertNotNull("Should detect suspicious single = assignment inside if statement", assignmentWarning)
    assertEquals(com.example.ui.components.codeeditor.DiagnosticSeverity.WARNING, assignmentWarning?.severity)
    assertEquals(2, assignmentWarning?.line)
  }

  @Test
  fun `code diagnostics service returns empty for valid kotlin code`() {
    val validCode = """
      class AgentManager {
          fun activate(trigger: String): Boolean {
              return if (trigger.isNotEmpty()) {
                  true
              } else {
                  false
              }
          }
      }
    """.trimIndent()
    val diagnostics = com.example.ui.components.codeeditor.CodeDiagnosticsService.analyze(validCode, "kt")
    val errors = diagnostics.filter { it.severity == com.example.ui.components.codeeditor.DiagnosticSeverity.ERROR }
    assertTrue("Valid code should have zero syntax errors, found: $errors", errors.isEmpty())
  }

  @Test
  fun `wavy underline drawer provides distinct colors per severity`() {
    val errColor = com.example.ui.components.codeeditor.WavyUnderlineDrawer.ColorError
    val warnColor = com.example.ui.components.codeeditor.WavyUnderlineDrawer.ColorWarning
    val infoColor = com.example.ui.components.codeeditor.WavyUnderlineDrawer.ColorInfo

    assertTrue("Error and warning colors must differ", errColor != warnColor)
    assertTrue("Warning and info colors must differ", warnColor != infoColor)
  }
}

