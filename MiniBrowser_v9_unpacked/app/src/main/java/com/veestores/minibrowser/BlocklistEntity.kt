
package com.veestores.minibrowser

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocklist")
data class BlocklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String
)
