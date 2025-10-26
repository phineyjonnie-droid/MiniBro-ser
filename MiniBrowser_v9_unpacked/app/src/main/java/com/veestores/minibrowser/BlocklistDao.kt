
package com.veestores.minibrowser

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlocklistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<BlocklistEntity>)

    @Query("SELECT pattern FROM blocklist")
    suspend fun getAllPatterns(): List<String>

    @Query("DELETE FROM blocklist")
    suspend fun clearAll()
}
