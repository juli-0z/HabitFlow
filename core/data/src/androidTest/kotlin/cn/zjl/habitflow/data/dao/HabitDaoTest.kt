package cn.zjl.habitflow.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.zjl.habitflow.data.db.AppDatabase
import cn.zjl.habitflow.data.entity.HabitEntity
import cn.zjl.habitflow.data.entity.HabitRecordEntity
import cn.zjl.habitflow.model.Frequency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * HabitDao 真库测试（TECH_DESIGN_v1.2 §8.2/§8.4，androidTest）
 *
 * 推荐方案：inMemory 真库（真 SQLite 行为，Robolectric shadow 有差异）；
 * 覆盖：upsert 插入/更新、observeActiveHabits 软删除过滤、打卡增删与
 * observeChecked 状态、外键 CASCADE 级联删除。
 */
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: HabitDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.habitDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun habit(
        id: Long = 0,
        name: String = "晨跑",
        archived: Boolean = false,
    ) = HabitEntity(
        id = id,
        name = name,
        frequency = Frequency.DAILY,
        createdAt = 0L,
        isArchived = archived,
    )

    private val today: Long get() = LocalDate.now().toEpochDay()

    @Test
    fun upsertInsertsAndObserveReturnsHabit() =
        runBlocking {
            val id = dao.upsertHabit(habit(name = "晨跑"))

            val habits = dao.observeActiveHabits().first()

            assertTrue(id > 0)
            assertEquals(1, habits.size)
            assertEquals("晨跑", habits.first().name)
        }

    @Test
    fun upsertWithExistingIdUpdatesInsteadOfDuplicating() =
        runBlocking {
            dao.upsertHabit(habit(id = 1, name = "晨跑"))
            dao.upsertHabit(habit(id = 1, name = "阅读"))

            val habits = dao.observeActiveHabits().first()

            assertEquals(1, habits.size)
            assertEquals("阅读", habits.first().name)
        }

    @Test
    fun observeActiveHabitsFiltersArchived() =
        runBlocking {
            dao.upsertHabit(habit(id = 1, name = "晨跑"))
            dao.upsertHabit(habit(id = 2, name = "阅读"))
            dao.archiveHabit(1)

            val habits = dao.observeActiveHabits().first()

            assertEquals(1, habits.size)
            assertEquals("阅读", habits.first().name)
        }

    @Test
    fun checkInAndCheckOutTogglesCheckedState() =
        runBlocking {
            val habitId = dao.upsertHabit(habit(name = "晨跑"))
            assertFalse(dao.observeChecked(habitId, today).first())

            dao.insertRecord(HabitRecordEntity(habitId = habitId, date = today, completedAt = 0L))
            assertTrue(dao.observeChecked(habitId, today).first())

            dao.deleteRecord(habitId, today)
            assertFalse(dao.observeChecked(habitId, today).first())
        }

    @Test
    fun recordCascadeDeletesWithHabit() =
        runBlocking {
            val habitId = dao.upsertHabit(habit(name = "晨跑"))
            dao.insertRecord(HabitRecordEntity(habitId = habitId, date = today, completedAt = 0L))

            // DAO 无物理删除入口（软删除设计，§5.1），直接用底层 SQL 验证外键 CASCADE（§6.1）
            db.openHelper.writableDatabase.execSQL("DELETE FROM habit WHERE id = $habitId")
            val recordCount =
                db.openHelper.writableDatabase
                    .query("SELECT COUNT(*) FROM habit_record WHERE habit_id = $habitId")
                    .use { cursor ->
                        cursor.moveToFirst()
                        cursor.getInt(0)
                    }

            assertEquals(0, recordCount)
        }
}
