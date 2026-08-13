package cn.zjl.habitflow.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.zjl.habitflow.network.TokenStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * TokenStore 的 DataStore 实现（TECH_DESIGN_v1.1 §7.1：注入进 network 模块，体现依赖倒置）
 */
class DataStoreTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TokenStore {

    override suspend fun getToken(): String? =
        dataStore.data.first()[KEY_TOKEN]

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
        }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
    }
}
