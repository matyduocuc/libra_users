package com.empresa.libra_users.di

import android.content.Context
import androidx.room.Room
import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.database.AppDatabase
import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.LoanDao
import com.empresa.libra_users.data.local.user.NotificationDao
import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "libra_users_db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries() // SOLUCIÓN TEMPORAL AL CRASH PRINCIPAL
            .build()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    fun provideLoanDao(db: AppDatabase): LoanDao = db.loanDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()


    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideBookRepository(bookDao: BookDao): BookRepository {
        return BookRepository(bookDao)
    }

    @Provides
    @Singleton
    fun provideLoanRepository(loanDao: LoanDao): LoanRepository {
        return LoanRepository(loanDao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationDao: NotificationDao): NotificationRepository {
        return NotificationRepository(notificationDao)
    }
}
