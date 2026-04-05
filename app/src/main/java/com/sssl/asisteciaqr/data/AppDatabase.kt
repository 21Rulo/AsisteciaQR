package com.sssl.asisteciaqr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sssl.asisteciaqr.data.dao.AlumnoDao
import com.sssl.asisteciaqr.data.dao.AsistenciaDao
import com.sssl.asisteciaqr.data.dao.GrupoDao
import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia
import com.sssl.asisteciaqr.data.entity.Grupo

@Database(
    entities = [Grupo::class, Alumno::class, Asistencia::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun grupoDao(): GrupoDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun asistenciaDao(): AsistenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "asistencia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}