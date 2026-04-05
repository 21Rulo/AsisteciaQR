package com.sssl.asisteciaqr.data.dao

import androidx.room.*
import com.sssl.asisteciaqr.data.entity.Alumno
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumnos(alumnos: List<Alumno>)

    @Update
    suspend fun updateAlumno(alumno: Alumno)

    @Delete
    suspend fun deleteAlumno(alumno: Alumno)

    @Query("SELECT * FROM alumnos WHERE grupoId = :grupoId ORDER BY nombre ASC")
    fun getAlumnosByGrupo(grupoId: Long): Flow<List<Alumno>>

    @Query("SELECT * FROM alumnos WHERE matricula = :matricula")
    suspend fun getAlumnoByMatricula(matricula: String): Alumno?

    @Query("SELECT * FROM alumnos WHERE matricula = :matricula")
    fun getAlumnoByMatriculaFlow(matricula: String): Flow<Alumno?>

    @Query("SELECT COUNT(*) FROM alumnos WHERE grupoId = :grupoId")
    fun getAlumnosCountByGrupo(grupoId: Long): Flow<Int>

    @Query("DELETE FROM alumnos WHERE grupoId = :grupoId")
    suspend fun deleteAlumnosByGrupo(grupoId: Long)
}