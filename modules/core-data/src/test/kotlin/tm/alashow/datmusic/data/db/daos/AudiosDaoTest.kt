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
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.Audio

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class AudiosDaoTest : BaseTest() {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var dao: AudiosDao

    private val testItems = (1..5).map { SampleData.audio() }
    private val testParams = DatmusicSearchParams("test")
    private val entriesComparator = compareBy(Audio::page, Audio::searchIndex)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun audiosById() = runTest {
        dao.insertAll(testItems)
        dao.insert(SampleData.audio().copy(id = testItems.first().id))

        assertThat(dao.count()).isEqualTo(testItems.size + 1)

        val audiosById = dao.audiosById(testItems.map { it.id })
        assertThat(audiosById.size).isEqualTo(testItems.size)
    }

    @Test
    fun bulkDelete() = runTest {
        dao.insertAll(testItems)

        val idsToRemove = testItems.shuffled().map { it.id }.take(2)
        val idsToKeep = testItems.map { it.id }.filterNot { it in idsToRemove }
        dao.bulkDelete(idsToRemove)

        dao.entries().test {
            assertThat(awaitItem().map { it.id })
                .containsExactlyElementsIn(idsToKeep)
        }
    }

    @Test
    fun deleteExcept() = runTest {
        dao.insertAll(testItems)

        val idWhitelist = testItems.shuffled().map { it.id }.take(2)
        dao.deleteExcept(idWhitelist)

        dao.entries().test {
            assertThat(awaitItem().map { it.id })
                .containsExactlyElementsIn(idWhitelist)
        }
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
    fun entries_onSamePage() = runTest {
        val items = testItems.map { it.copy(page = 0) }.sortedWith(entriesComparator)
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
    fun entries_withParams_onSamePage() = runTest {
        val items = testItems.map { it.copy(params = testParams.toString(), page = 0) }
            .sortedWith(entriesComparator)
        dao.insertAll(items)

        dao.entries(testParams).test {
            assertThat(awaitItem()).isEqualTo(items)
        }
    }

    @Test
    fun entries_withParamsAndPage() = runTest {
        val page = 2
        val items = testItems.map { it.copy(params = testParams.toString(), page = page) }
            .sortedWith(entriesComparator)
        dao.insertAll(items)

        dao.entries(testParams, page).test {
            assertThat(awaitItem()).isEqualTo(items)
        }
        dao.entries(testParams, page + 1).test {
            assertThat(awaitItem()).isEmpty()
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
        dao.entryNullable(item.id).test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun entriesById() = runTest {
        dao.insertAll(testItems)

        dao.entriesById(testItems.map { it.id }).test {
            assertThat(awaitItem()).containsExactlyElementsIn(testItems)
        }
    }

    @Test
    fun delete() = runTest {
        val item = testItems.first()
        dao.insert(item)
        dao.delete(item.id)

        assertThat(dao.exists(item.id)).isEqualTo(0)
    }

    @Test
    fun delete_withParams() = runTest {
        val item = testItems.first().copy(params = testParams.toString())
        dao.insert(item)
        dao.delete(testParams)

        assertThat(dao.exists(item.id)).isEqualTo(0)
    }

    @Test
    fun delete_withParamsAndPage() = runTest {
        val page = 2
        val items = testItems.map { it.copy(params = testParams.toString(), page = page) }
        dao.insertAll(items)
        dao.delete(testParams, page)

        dao.entriesById(items.map { it.id }).test {
            assertThat(awaitItem()).isEmpty()
        }
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
            dao.insert(SampleData.audio())
            assertThat(awaitItem()).isEqualTo(testItems.size + 1)
        }
    }

    @Test
    fun count_withParams() = runTest {
        val paramlessItems = (1..5).map { SampleData.audio() }
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

        assertThat(dao.exists(item.id)).isEqualTo(1)
    }

    @Test
    fun has() = runTest {
        val item = testItems.first()
        dao.insert(item)

        dao.has(item.id).test {
            assertThat(awaitItem()).isEqualTo(1)
            dao.delete(item.id)
            assertThat(awaitItem()).isEqualTo(0)
        }
    }
}
