package me.rerere.rikkahub.data.db.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.rerere.rikkahub.data.db.AppDatabase
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration_27_28_Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun createsDailyTablesAndIndexes() {
        val dbName = "daily-migration-test"
        helper.createDatabase(dbName, 27).close()
        val db = helper.runMigrationsAndValidate(dbName, 28, true, Migration_27_28)

        for (table in listOf("daily_entries", "daily_events", "journal_drafts", "daily_room_settings")) {
            db.query("SELECT * FROM $table LIMIT 0").use { cursor ->
                assertTrue("$table should exist", cursor.columnNames.isNotEmpty())
            }
        }
        db.query("PRAGMA index_list('daily_entries')").use { cursor ->
            val indexes = buildList {
                val nameColumn = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) add(cursor.getString(nameColumn))
            }
            assertTrue(indexes.contains("index_daily_entries_occurred_at"))
            assertTrue(indexes.contains("index_daily_entries_status"))
        }
        db.close()
    }
}
