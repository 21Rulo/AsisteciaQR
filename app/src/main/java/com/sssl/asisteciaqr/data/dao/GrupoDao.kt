package com.sssl.asisteciaqr.data.dao

import androidx.room.*
import com.sssl.asisteciaqr.data.entity.Grupo
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrupo(grupo: Grupo): Long

    @Update
    suspend fun updateGrupo(grupo: Grupo)

    @Delete
    suspend fun deleteGrupo(grupo: Grupo)

    @Query("SELECT * FROM grupos ORDER BY fechaCreacion DESC")
    fun getAllGrupos(): Flow<List<Grupo>>

    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    suspend fun getGrupoById(grupoId: Long): Grupo?

    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    fun getGrupoByIdFlow(grupoId: Long): Flow<Grupo?>
}