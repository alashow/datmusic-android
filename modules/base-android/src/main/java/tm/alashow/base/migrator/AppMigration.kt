/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.migrator

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.PreferencesStore

typealias AppMigrations = Set<AppMigration>

abstract class AppMigration(val id: String) {

    val appliedKey get() = booleanPreferencesKey("migration_applied_$id")
    abstract suspend fun apply()
}

class AppMigrator @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val preferencesStore: PreferencesStore,
    private val migrations: AppMigrations = emptySet()
) : CoroutineScope by ProcessLifecycleOwner.get().lifecycleScope {

    @OptIn(ExperimentalTime::class)
    fun migrate() = launch(dispatchers.io) {
        migrations
            .filterNot {
                preferencesStore.get(it.appliedKey, defaultValue = false).first()
            }
            .forEach { migration ->
                Timber.i("Migrating ${migration.id}..")
                migration.apply()
                preferencesStore.save(migration.appliedKey, true)
            }
    }
}
