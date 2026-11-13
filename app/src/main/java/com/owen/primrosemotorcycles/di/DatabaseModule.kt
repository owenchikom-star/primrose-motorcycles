package com.owen.primrosemotorcycles.di

import android.content.Context
import com.owen.primrosemotorcycles.data.AppDatabase
import com.owen.primrosemotorcycles.data.CheckupDao
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.data.SparePartDao
import com.owen.primrosemotorcycles.data.StockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideJobCardDao(database: AppDatabase): JobCardDao {
        return database.jobCardDao()
    }

    @Provides
    fun provideSparePartDao(database: AppDatabase): SparePartDao {
        return database.sparePartDao()
    }

    @Provides
    fun provideStockDao(database: AppDatabase): StockDao {
        return database.stockDao()
    }

    @Provides
    fun provideCheckupDao(database: AppDatabase): CheckupDao {
        return database.checkupDao()
    }
}
