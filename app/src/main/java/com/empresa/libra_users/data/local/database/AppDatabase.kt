package com.empresa.libra_users.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.empresa.libra_users.data.local.user.*

@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        LoanEntity::class,
        NotificationEntity::class
    ],
    version = 2,                 // ⬅️ súbela cuando agregues/edites tablas
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun loanDao(): LoanDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "libra_users_db.db"

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // Para desarrollo: elimina la BD si el esquema cambia
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 👇 si quieres "seed data", hazlo aquí con coroutines
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
    }
}
