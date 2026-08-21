package cn.zjl.habitflow.data.repository

import cn.zjl.habitflow.data.dao.HabitDao
import cn.zjl.habitflow.data.entity.HabitEntity
import cn.zjl.habitflow.data.entity.HabitRecordEntity
import cn.zjl.habitflow.model.Frequency
import cn.zjl.habitflow.model.Habit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * HabitRepositoryImpl 映射与 Flow 组装单测（M3 3.9，§6.4：entity->model 映射在 Repository 层）
 *
 * mock DAO（不触碰 Room），验证：
 * - entity -> model 全字段映射（observeHabits）；
 * - LocalDate <-> epochDay 转换（observeChecked / checkIn / checkOut）；
 * - 完成窗口组装（observeCompletionStats，最老->最新，打卡日置 true）；
 * - 模型 -> entity 映射与写操作委派（saveHabit / archiveHabit / checkIn / checkOut）。
 */
class HabitRepositoryImplTest {
    private val dao = mockk<HabitDao>()
    private val repository = HabitRepositoryImpl(dao)

    private fun entity(
        id: Long = 1,
        name: String = "晨跑",
        frequency: Frequency = Frequency.DAILY,
        targetPerWeek: Int = 0,
        iconRes: String? = "star",
        colorHex: String = "#2E7D32",
        isArchived: Boolean = false,
        createdAt: Long = 1000L,
    ) = HabitEntity(
        id = id,
        name = name,
        frequency = frequency,
        targetPerWeek = targetPerWeek,
        iconRes = iconRes,
        colorHex = colorHex,
        isArchived = isArchived,
        createdAt = createdAt,
    )

    private fun model(
        id: Long = 1,
        name: String = "晨跑",
        frequency: Frequency = Frequency.DAILY,
        targetPerWeek: Int = 0,
        iconRes: String? = "star",
        colorHex: String = "#2E7D32",
        isArchived: Boolean = false,
        createdAt: Long = 1000L,
    ) = Habit(
        id = id,
        name = name,
        frequency = frequency,
        targetPerWeek = targetPerWeek,
        iconRes = iconRes,
        colorHex = colorHex,
        isArchived = isArchived,
        createdAt = createdAt,
    )

    @Test
    fun `observeHabits maps entities to models with all fields`() =
        runTest {
            val e =
                entity(
                    id = 7,
                    name = "阅读",
                    frequency = Frequency.WEEKLY,
                    targetPerWeek = 3,
                    iconRes = "book",
                    colorHex = "#1565C0",
                    isArchived = false,
                    createdAt = 1234L,
                )
            coEvery { dao.observeActiveHabits() } returns flowOf(listOf(e))

            val result = repository.observeHabits().first()

            assertEquals(1, result.size)
            val mapped = result.first()
            assertEquals(7L, mapped.id)
            assertEquals("阅读", mapped.name)
            assertEquals(Frequency.WEEKLY, mapped.frequency)
            assertEquals(3, mapped.targetPerWeek)
            assertEquals("book", mapped.iconRes)
            assertEquals("#1565C0", mapped.colorHex)
            assertEquals(false, mapped.isArchived)
            assertEquals(1234L, mapped.createdAt)
        }

    @Test
    fun `observeChecked converts date to epochDay before dao call`() =
        runTest {
            val date = LocalDate.of(2026, 8, 21)
            coEvery { dao.observeChecked(5L, date.toEpochDay()) } returns flowOf(true)

            val checked = repository.observeChecked(5L, date).first()

            assertEquals(true, checked)
            coVerify(exactly = 1) { dao.observeChecked(5L, date.toEpochDay()) }
        }

    @Test
    fun `observeCompletionStats builds full day window oldest to newest`() =
        runTest {
            val today = LocalDate.now()
            val todayEpoch = today.toEpochDay()
            val threeDaysAgoEpoch = today.minusDays(3).toEpochDay()
            coEvery { dao.observeRecordsBetween(1L, any(), any()) } returns
                flowOf(
                    listOf(
                        HabitRecordEntity(habitId = 1, date = threeDaysAgoEpoch, completedAt = 0L),
                        HabitRecordEntity(habitId = 1, date = todayEpoch, completedAt = 0L),
                    ),
                )

            val result = repository.observeCompletionStats(1L, days = 7).first()

            assertEquals(7, result.size)
            assertEquals(today.minusDays(6), result.first().first) // 最早一天
            assertEquals(today, result.last().first) // 今天
            assertEquals(false, result[0].second) // 窗口首日未打卡
            assertEquals(true, result[3].second) // 三天前已打卡
            assertEquals(true, result[6].second) // 今天已打卡
        }

    @Test
    fun `saveHabit maps model to entity and upserts`() =
        runTest {
            val habit =
                model(
                    id = 9,
                    name = "晨跑",
                    frequency = Frequency.DAILY,
                    iconRes = "star",
                    colorHex = "#2E7D32",
                    createdAt = 999L,
                )
            coEvery { dao.upsertHabit(any()) } returns 9L

            val rowId = repository.saveHabit(habit)

            assertEquals(9L, rowId)
            val slot = slot<HabitEntity>()
            coVerify(exactly = 1) { dao.upsertHabit(capture(slot)) }
            assertEquals(9L, slot.captured.id)
            assertEquals("晨跑", slot.captured.name)
            assertEquals(Frequency.DAILY, slot.captured.frequency)
            assertEquals("star", slot.captured.iconRes)
            assertEquals("#2E7D32", slot.captured.colorHex)
            assertEquals(999L, slot.captured.createdAt)
        }

    @Test
    fun `archiveHabit delegates to dao`() =
        runTest {
            coEvery { dao.archiveHabit(3L) } returns Unit

            repository.archiveHabit(3L)

            coVerify(exactly = 1) { dao.archiveHabit(3L) }
        }

    @Test
    fun `checkIn inserts record with epochDay date and timestamp`() =
        runTest {
            val date = LocalDate.of(2026, 8, 21)
            coEvery { dao.insertRecord(any()) } returns 0L

            repository.checkIn(11L, date)

            val slot = slot<HabitRecordEntity>()
            coVerify(exactly = 1) { dao.insertRecord(capture(slot)) }
            assertEquals(11L, slot.captured.habitId)
            assertEquals(date.toEpochDay(), slot.captured.date)
            assertTrue(slot.captured.completedAt > 0L)
        }

    @Test
    fun `checkOut deletes record for date`() =
        runTest {
            val date = LocalDate.of(2026, 8, 21)
            coEvery { dao.deleteRecord(11L, date.toEpochDay()) } returns Unit

            repository.checkOut(11L, date)

            coVerify(exactly = 1) { dao.deleteRecord(11L, date.toEpochDay()) }
        }
}
