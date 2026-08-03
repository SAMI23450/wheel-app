package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.db.AppDao
import com.example.data.model.CoinFlipHistoryEntity
import com.example.data.model.SpinHistoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.WheelConfigEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppRepository(
    private val appDao: AppDao,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)

    fun saveLastUserId(userId: String?) {
        prefs.edit().putString("last_user_id", userId).apply()
    }

    fun getLastUserId(): String? {
        return prefs.getString("last_user_id", null)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_effects_enabled", enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_effects_enabled", true)
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun getAppTheme(): String {
        return prefs.getString("app_theme", "Dark") ?: "Dark"
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
    }

    fun getAppLanguage(): String {
        return prefs.getString("app_language", "English") ?: "English"
    }

    // Reactive flow of online status
    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        // Check current status immediately
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isInitiallyConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(isInitiallyConnected)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Fallback for security exceptions or restrictions
            trySend(true)
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Users
    suspend fun getUser(userId: String): UserEntity? {
        return appDao.getUserById(userId)
    }

    suspend fun saveUser(user: UserEntity) {
        appDao.insertUser(user)
    }

    // Wheel configurations
    fun getWheelConfigs(userId: String): Flow<List<WheelConfigEntity>> {
        return appDao.getWheelConfigs(userId)
    }

    fun getFavoriteWheelConfigs(userId: String): Flow<List<WheelConfigEntity>> {
        return appDao.getFavoriteWheelConfigs(userId)
    }

    suspend fun saveWheelConfig(config: WheelConfigEntity): Int {
        return appDao.insertWheelConfig(config).toInt()
    }

    suspend fun updateFavoriteStatus(configId: Int, isFavorite: Boolean) {
        appDao.updateFavoriteStatus(configId, isFavorite)
    }

    suspend fun deleteWheelConfig(config: WheelConfigEntity) {
        appDao.deleteWheelConfig(config)
    }

    // Spin History
    fun getSpinHistory(userId: String): Flow<List<SpinHistoryEntity>> {
        return appDao.getSpinHistory(userId)
    }

    suspend fun insertSpinHistory(spin: SpinHistoryEntity) {
        appDao.insertSpinHistory(spin)
    }

    suspend fun clearSpinHistory(userId: String) {
        appDao.clearSpinHistory(userId)
    }

    // Coin Flip History
    fun getCoinFlipHistory(userId: String): Flow<List<CoinFlipHistoryEntity>> {
        return appDao.getCoinFlipHistory(userId)
    }

    suspend fun insertCoinFlipHistory(flip: CoinFlipHistoryEntity) {
        appDao.insertCoinFlipHistory(flip)
    }

    suspend fun clearCoinFlipHistory(userId: String) {
        appDao.clearCoinFlipHistory(userId)
    }
}
