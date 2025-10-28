package com.empresa.libra_users.data.local.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanDao
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.NotificationDao
import com.empresa.libra_users.data.local.user.NotificationEntity
import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity

@Database(
    entities = [UserEntity::class, BookEntity::class, LoanEntity::class, NotificationEntity::class],
    version = 9, // Versión actualizada para la migración
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 8, to = 9)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun loanDao(): LoanDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "libra_users_db"
                )
                .build() // Ya no se necesita fallbackToDestructiveMigration
                INSTANCE = instance
                instance
            }
        }
    }
}
