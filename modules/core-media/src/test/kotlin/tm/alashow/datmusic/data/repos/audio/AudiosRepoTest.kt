/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.audio

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
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.Audio

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class AudiosRepoTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: AudiosRepo
    @Inject lateinit var downloadRequestsDao: DownloadRequestsDao

    private val testItems = (1..5).map { SampleData.audio() }
    private val testParams = DatmusicSearchParams("test")
    private val entriesComparator = compareBy(Audio::page, Audio::searchIndex)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun entriesByParams() = runTest {
        val params = testParams.toString()
        val items = testItems.map { it.copy(params = params) }
        repo.insertAll(items)

        repo.entriesByParams(testParams).test {
            assertThat(awaitItem())
                .isEqualTo(items.sortedWith(entriesComparator))
        }
    }

    @Test
    fun entriesByParams_empty() = runTest {
        repo.entriesByParams(testParams).test {
            assertThat(awaitItem())
                .isEqualTo(emptyList<Audio>())
        }
    }

    @Test
    fun `audiosById returns distinct audios`() = runTest {
        val items = testItems
        val itemIds = items.map { it.id }
        repo.insertAll(items)

        // re-save items to duplicate them
        repo.saveAudios(AudioSaveType.Download, items)

        assertThat(repo.audiosById(itemIds).map { it.id })
            .containsExactlyElementsIn(itemIds)
    }

    @Test
    fun `saveAudios saves with id as primaryKey and saveType params`() = runTest {
        val items = testItems
        val itemIds = testItems.map { it.id }
        repo.saveAudios(AudioSaveType.Download, items)

        val savedItems = repo.audiosById(itemIds)

        assertThat(savedItems.map { it.id })
            .containsExactlyElementsIn(itemIds)
        assertThat(savedItems.map { it.params }.toSet())
            .containsExactly(AudioSaveType.Download.toAudioParams())
    }

    @Test
    fun find() = runTest {
        repo.insertAll(testItems)
        val downloadRequestItems = (1..5).map { SampleData.downloadRequest() }
        downloadRequestsDao.insertAll(downloadRequestItems)

        val ids = testItems.map { it.id } + downloadRequestItems.map { it.id }
        assertThat(repo.find(ids))
            .containsExactlyElementsIn(downloadRequestItems.map { it.audio } + testItems)
    }

    @Test
    fun findMissingIds() = runTest {
        repo.insertAll(testItems)
        val downloadRequestItems = (1..5).map { SampleData.downloadRequest() }
        downloadRequestsDao.insertAll(downloadRequestItems)

        repo.count().test {
            // first we expect only testItems audios
            assertThat(awaitItem()).isEqualTo(testItems.size)

            // then findMissingIds recovers downloadRequestItems audio ids
            val ids = testItems.map { it.id } + downloadRequestItems.map { it.id }
            assertThat(repo.findMissingIds(ids))
                .isEmpty() // all ids are found

            // so now we expect all testItems + downloadRequestItems audios
            assertThat(awaitItem()).isEqualTo(ids.size)
        }
    }

    // region RoomRepo tests

    @Test
    fun entry() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.entry(item.id).test {
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun `entry returns null if item doesn't exist`() = runTest {
        val item = testItems.first()

        repo.entry(item.id).test {
            assertThat(awaitItem())
                .isNull()
            repo.insert(item)
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun `entryNotNull doesn't return anything it's available`() = runTest {
        val item = testItems.first()

        repo.entryNotNull(item.id).test {
            // awaiting here would get stuck since it won't return null items
            repo.insert(item)
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun entries() = runTest {
        repo.insertAll(testItems)

        repo.entries().test {
            assertThat(awaitItem())
                .isEqualTo(testItems.sortedWith(entriesComparator))
        }
    }

    @Test
    fun entries_byId() = runTest {
        repo.insertAll(testItems)

        repo.entries(testItems.map { it.id }).test {
            assertThat(awaitItem())
                .containsExactlyElementsIn(testItems)
        }
    }

    @Test
    fun update() = runTest {
        val item = testItems.first()
        repo.insert(item)

        val updated = SampleData.audio().copy(id = item.id, primaryKey = item.primaryKey)
        repo.update(updated)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(updated)
        }
    }

    @Test
    fun isEmpty() = runTest {
        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }

        repo.insertAll(testItems)

        repo.isEmpty().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun count() = runTest {
        repo.count().test {
            assertThat(awaitItem()).isEqualTo(0)
        }

        repo.insertAll(testItems)

        repo.count().test {
            assertThat(awaitItem()).isEqualTo(testItems.size)
        }
    }

    @Test
    fun has() = runTest {
        val item = testItems.first()

        repo.has(item.id).test {
            assertThat(awaitItem()).isFalse()
            repo.insert(item)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun exists() = runTest {
        val item = testItems.first()

        assertThat(repo.exists(item.id)).isFalse()
        repo.insert(item)
        assertThat(repo.exists(item.id)).isTrue()
    }

    @Test
    fun delete() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.delete(item.id)

        assertThat(repo.exists(item.id)).isFalse()
    }

    @Test
    fun deleteEntity() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.delete(item)

        assertThat(repo.exists(item.id)).isFalse()
    }

    @Test
    fun deleteAll() = runTest {
        repo.insertAll(testItems)

        repo.deleteAll()

        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    // endregion
}
