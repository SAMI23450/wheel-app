package com.example.data.db

import androidx.room.*
import com.example.data.model.CoinFlipHistoryEntity
import com.example.data.model.SpinHistoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.WheelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Wheel configurations
    @Query("SELECT * FROM wheel_configs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWheelConfigs(userId: String): Flow<List<WheelConfigEntity>>

    @Query("SELECT * FROM wheel_configs WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWheelConfigs(userId: String): Flow<List<WheelConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWheelConfig(config: WheelConfigEntity): Long

    @Query("UPDATE wheel_configs SET isFavorite = :isFavorite WHERE id = :configId")
    suspend fun updateFavoriteStatus(configId: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteWheelConfig(config: WheelConfigEntity)

    // Spin History
    @Query("SELECT * FROM spin_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSpinHistory(userId: String): Flow<List<SpinHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpinHistory(spin: SpinHistoryEntity)

    @Query("DELETE FROM spin_history WHERE userId = :userId")
    suspend fun clearSpinHistory(userId: String)

    // Coin Flip History
    @Query("SELECT * FROM coin_flip_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCoinFlipHistory(userId: String): Flow<List<CoinFlipHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinFlipHistory(flip: CoinFlipHistoryEntity)

    @Query("DELETE FROM coin_flip_history WHERE userId = :userId")
    suspend fun clearCoinFlipHistory(userId: String)
}
