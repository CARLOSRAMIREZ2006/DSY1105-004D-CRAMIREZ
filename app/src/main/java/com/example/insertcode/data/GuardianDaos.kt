package com.example.insertcode.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Query("SELECT * FROM eventos ORDER BY fecha DESC")
    fun getEventosFlow(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    fun getPorId(id: Long): Flow<EventoEntity?>

    @Query("SELECT COUNT(*) FROM eventos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evento: EventoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(eventos: List<EventoEntity>)

    @Query("UPDATE eventos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)
}

@Dao
interface IncidenciaDao {

    @Query("SELECT * FROM incidencias ORDER BY fecha DESC")
    fun getIncidenciasFlow(): Flow<List<IncidenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(incidencia: IncidenciaEntity): Long
}
