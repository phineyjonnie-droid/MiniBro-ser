
package com.veestores.minibrowser

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BlocklistEntity::class], version = 1, exportSchema = false)
abstract class BlocklistDatabase : RoomDatabase() {
    abstract fun blocklistDao(): BlocklistDao
}
