/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db.daos

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.domain.models.Params

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class PlaylistsDaoTest : BaseTest() {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var dao: PlaylistsDao

    private val testItems = (1..5).map { SampleData.playlist() }
    private val testParams = Params()
    private val entriesComparator = compareByDescending(Playlist::id)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getByName() = runTest {
        val item = testItems.first()
        dao.insert(item)

        assertThat(dao.getByName(item.name)).isEqualTo(item)
    }

    @Test
    fun getByName_nonExisting() = runTest {
        val item = testItems.first()
        assertThat(dao.getByName(item.name)).isNull()
    }

    @Test
    fun entries() = runTest {
        val items = testItems.sortedWith(entriesComparator)
        dao.insertAll(items)

        dao.entries().test {
            assertThat(awaitItem()).isEqualTo(items)
        }
    }

    @Test
    fun entries_withParams() = runTest {
        val items = testItems.map { it.copy(params = testParams.toString()) }
            .sortedWith(entriesComparator)
        dao.insertAll(items)

        dao.entries(testParams).test {
            assertThat(awaitItem()).isEqualTo(items)
        }
    }

    @Test
    fun entries_withCountAndOffset() = runTest {
        val items = testItems.map { it.copy(params = testParams.toString()) }
            .sortedWith(entriesComparator)
        dao.insertAll(items)

        val count = 1
        val offset = 2
        dao.entries(count = count, offset = offset).test {
            assertThat(awaitItem()).isEqualTo(items.drop(offset).take(count))
        }
    }

    @Test
    fun entry() = runTest {
        val item = testItems.first()
        dao.insert(item)

        dao.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(item)
        }
    }

    @Test
    fun entryNullable() = runTest {
        val item = testItems.first()
        dao.entryNullable(item.getIdentifier()).test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun entriesById() = runTest {
        dao.insertAll(testItems)

        dao.entriesById(testItems.map { it.getIdentifier() }).test {
            assertThat(awaitItem()).containsExactlyElementsIn(testItems)
        }
    }

    @Test
    fun delete() = runTest {
        val item = testItems.first()
        dao.insert(item)
        dao.delete(item.getIdentifier())

        assertThat(dao.exists(item.getIdentifier())).isEqualTo(0)
    }

    @Test
    fun delete_withParams() = runTest {
        val item = testItems.first().copy(params = testParams.toString())
        dao.insert(item)
        dao.delete(testParams)

        assertThat(dao.exists(item.getIdentifier())).isEqualTo(0)
    }

    @Test
    fun deleteAll() = runTest {
        dao.insertAll(testItems)
        dao.deleteAll()

        assertThat(dao.count()).isEqualTo(0)
    }

    @Test
    fun count() = runTest {
        dao.insertAll(testItems)

        assertThat(dao.count()).isEqualTo(testItems.size)
    }

    @Test
    fun observeCount() = runTest {
        dao.insertAll(testItems)

        dao.observeCount().test {
            assertThat(awaitItem()).isEqualTo(testItems.size)
            dao.insert(SampleData.playlist())
            assertThat(awaitItem()).isEqualTo(testItems.size + 1)
        }
    }

    @Test
    fun count_withParams() = runTest {
        val paramlessItems = (1..5).map { SampleData.playlist() }
        val items = testItems.map { it.copy(params = testParams.toString()) }

        dao.insertAll(paramlessItems)
        dao.insertAll(items)

        assertThat(dao.count()).isEqualTo(paramlessItems.size + items.size)
        assertThat(dao.count(testParams)).isEqualTo(items.size)
    }

    @Test
    fun exists() = runTest {
        val item = testItems.first()
        dao.insert(item)

        assertThat(dao.exists(item.getIdentifier())).isEqualTo(1)
    }

    @Test
    fun has() = runTest {
        val item = testItems.first()
        dao.insert(item)

        dao.has(item.getIdentifier()).test {
            assertThat(awaitItem()).isEqualTo(1)
            dao.delete(item.getIdentifier())
            assertThat(awaitItem()).isEqualTo(0)
        }
    }
}
