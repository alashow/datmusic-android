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
import tm.alashow.datmusic.data.*
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.*

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DownloadRequestsRepoTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: DownloadRequestsRepo

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)

    @After
    fun tearDown() = runTest {
        database.close()
    }

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
