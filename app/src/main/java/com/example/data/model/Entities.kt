package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // email or "guest"
    val displayName: String,
    val email: String,
    val provider: String, // "Google", "Apple", "Microsoft", "Email", "Guest", "Anonymous"
    val password: String = "" // Supporting real email login/register
)

data class WheelSlice(
    val name: String,
    val colorHex: String
)

@Entity(tableName = "wheel_configs")
data class WheelConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // associated user id
    val title: String,
    val slicesSerialized: String, // serialized list of WheelSlices
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val wheelName: String,
    val winnerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_flip_history")
data class CoinFlipHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val result: String, // "Heads" or "Tails"
    val timestamp: Long = System.currentTimeMillis()
)

object SliceSerializer {
    fun serialize(slices: List<WheelSlice>): String {
        return slices.joinToString(";;") { "${it.name}||${it.colorHex}" }
    }

    fun deserialize(serialized: String): List<WheelSlice> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split(";;").mapNotNull {
            val parts = it.split("||")
            if (parts.size == 2) {
                WheelSlice(parts[0], parts[1])
            } else {
                null
            }
        }
    }
}
