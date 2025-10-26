
package com.veestores.minibrowser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistRepository @Inject constructor(private val dao: BlocklistDao) {

    suspend fun replaceAll(patterns: List<String>) {
        dao.clearAll()
        val entities = patterns.map { BlocklistEntity(pattern = it) }
        dao.insertAll(entities)
    }

    suspend fun getAllPatterns(): List<String> = dao.getAllPatterns()
}
