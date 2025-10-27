package com.empresa.libra_users.data.local.database

import android.content.Context
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
    version = 2, // VERSIÓN AUMENTADA
    exportSchema = false
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
                    "libra_users_db" // Nombre del archivo de la base de datos
                )
                .fallbackToDestructiveMigration() // Permite recrear la BD si la versión cambia
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
