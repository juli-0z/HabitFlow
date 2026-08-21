package cn.zjl.habitflow.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 偏好数据源（TECH_DESIGN_v1.1 §6.2：Preferences DataStore，仅存 isDarkMode）
 * Room 存"可结构化查询的业务数据"，DataStore 存"轻量偏好"，一条数据绝不双写两处。
 */
class SettingsDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val isDarkMode: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[KEY_IS_DARK_MODE] ?: false
            }

        suspend fun setDarkMode(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[KEY_IS_DARK_MODE] = enabled
            }
        }

        private companion object {
            val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        }
    }
