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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.Audio

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class AudiosRepoTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: AudiosRepo

    private val testItems = (1..5).map { SampleData.audio() }
    private val entriesComparator = compareBy(Audio::page, Audio::searchIndex)

    override fun tearDown() {
        super.tearDown()
        database.close()
    }

    @Test
    fun entry() = testScope.runBlockingTest {
        val item = testItems.first()
        repo.insert(item)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(item)
        }
    }

    @Test
    fun entries() = testScope.runBlockingTest {
        repo.insertAll(testItems)

        repo.entries().test {
            assertThat(awaitItem())
                .isEqualTo(testItems.sortedWith(entriesComparator))
        }
    }

    @Test
    fun entries_byId() = testScope.runBlockingTest {
        repo.insertAll(testItems)

        repo.entries(testItems.map { it.id }).test {
            assertThat(awaitItem())
                .containsExactlyElementsIn(testItems)
        }
    }

    @Test
    fun update() = testScope.runBlockingTest {
        val item = testItems.first()
        repo.insert(item)

        val updated = SampleData.audio().copy(id = item.id, primaryKey = item.primaryKey)
        repo.update(updated)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(updated)
        }
    }

    @Test
    fun isEmpty() = testScope.runBlockingTest {
        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }

        repo.insertAll(testItems)

        repo.isEmpty().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun count() = testScope.runBlockingTest {
        repo.count().test {
            assertThat(awaitItem()).isEqualTo(0)
        }

        repo.insertAll(testItems)

        repo.count().test {
            assertThat(awaitItem()).isEqualTo(testItems.size)
        }
    }

    @Test
    fun has() = testScope.runBlockingTest {
        val item = testItems.first()

        repo.has(item.id).test {
            assertThat(awaitItem()).isFalse()
            repo.insert(item)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun exists() = testScope.runBlockingTest {
        val item = testItems.first()

        assertThat(repo.exists(item.id)).isFalse()
        repo.insert(item)
        assertThat(repo.exists(item.id)).isTrue()
    }

    @Test
    fun delete() = testScope.runBlockingTest {
        val item = testItems.first()
        repo.insert(item)

        repo.delete(item.id)

        assertThat(repo.exists(item.id)).isFalse()
    }

    @Test
    fun deleteAll() = testScope.runBlockingTest {
        repo.insertAll(testItems)

        repo.deleteAll()

        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }
    }
}
