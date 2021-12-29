/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos

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
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.Downloader

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DownloaderTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: Downloader

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)

    @After
    fun tearDown() {
        database.close()
    }

    // region RoomRepo tests

    @Test
    fun entry() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(item)
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

        val updated = SampleData.downloadRequest().copy(id = item.id)
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
    fun deleteAll() = runTest {
        repo.insertAll(testItems)

        repo.deleteAll()

        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    // endregion
}
