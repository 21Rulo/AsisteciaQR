package com.sssl.asisteciaqr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sssl.asisteciaqr.data.dao.AlumnoDao
import com.sssl.asisteciaqr.data.dao.AsistenciaDao
import com.sssl.asisteciaqr.data.dao.GrupoDao
import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia
import com.sssl.asisteciaqr.data.entity.Grupo

@Database(
    entities = [Grupo::class, Alumno::class, Asistencia::class],
    version = 2, // Incrementado de 1 a 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun grupoDao(): GrupoDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun asistenciaDao(): AsistenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 1 a 2 (agrega campo qrToken)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna qrToken a la tabla alumnos
                database.execSQL(
                    "ALTER TABLE alumnos ADD COLUMN qrToken TEXT NOT NULL DEFAULT ''"
                )

                // Crear índice para qrToken
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_alumnos_qrToken ON alumnos(qrToken)"
                )

                // Actualizar registros existentes con UUIDs únicos
                database.execSQL("""
                    UPDATE alumnos 
                    SET qrToken = lower(
                        hex(randomblob(4)) || '-' || 
                        hex(randomblob(2)) || '-' || 
                        '4' || substr(hex(randomblob(2)), 2) || '-' || 
                        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' || 
                        hex(randomblob(6))
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "asistencia_database"
                )
                    .addMigrations(MIGRATION_1_2) // Agregar migración
                    .fallbackToDestructiveMigration() // Solo en desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}