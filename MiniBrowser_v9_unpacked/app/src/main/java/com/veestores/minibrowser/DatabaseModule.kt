
package com.veestores.minibrowser

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBlocklistDatabase(@ApplicationContext context: Context): BlocklistDatabase {
        return Room.databaseBuilder(context, BlocklistDatabase::class.java, "blocklist_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBlocklistDao(db: BlocklistDatabase): BlocklistDao = db.blocklistDao()
}
