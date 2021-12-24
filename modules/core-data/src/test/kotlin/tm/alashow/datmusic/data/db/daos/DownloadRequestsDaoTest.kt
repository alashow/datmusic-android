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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.DownloadRequest

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class DownloadRequestsDaoTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var dao: DownloadRequestsDao

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getByIdsAndType() = runTest {
        val audioDownloadRequests = testItems.sortedWith(entriesComparator)
        dao.insertAll(audioDownloadRequests)

        val itemsByIdAndType = dao.getByIdAndType(
            ids = audioDownloadRequests.map { it.id },
            type = audioDownloadRequests.first().entityType
        )
        assertThat(itemsByIdAndType)
            .isEqualTo(audioDownloadRequests.sortedWith(entriesComparator))
    }

    @Test
    fun getByType() = runTest {
        val audioDownloadRequests = testItems.sortedWith(entriesComparator)
        dao.insertAll(audioDownloadRequests)

        val itemsByType = dao.getByType(audioDownloadRequests.first().entityType)
        assertThat(itemsByType).isEqualTo(audioDownloadRequests)
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
    fun entries_withCountAndOffset() = runTest {
        val items = testItems.sortedWith(entriesComparator)
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
            dao.insert(SampleData.downloadRequest())
            assertThat(awaitItem()).isEqualTo(testItems.size + 1)
        }
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
