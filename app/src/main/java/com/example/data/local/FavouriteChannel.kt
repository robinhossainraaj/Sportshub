package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favourites")
data class FavouriteChannel(
    @PrimaryKey val streamUrl: String,
    val name: String,
    val logoUrl: String,
    val category: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    fun getAllFavourites(): Flow<List<FavouriteChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: FavouriteChannel)

    @Delete
    suspend fun delete(channel: FavouriteChannel)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE streamUrl = :streamUrl LIMIT 1)")
    suspend fun isFavourite(streamUrl: String): Boolean
}

@Database(entities = [FavouriteChannel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sports24_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
