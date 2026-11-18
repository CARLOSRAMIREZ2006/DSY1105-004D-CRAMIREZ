package com.example.insertcode.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class, EventoEntity::class, IncidenciaEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun eventoDao(): EventoDao
    abstract fun incidenciaDao(): IncidenciaDao

    companion object {
        // @Volatile: los cambios son visibles de inmediato para todos los hilos
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MIGRATION 1 -> 2: agrega las tablas "eventos" e "incidencias"
        // (la tabla "usuarios" y sus datos se conservan intactos)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `eventos` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`titulo` TEXT NOT NULL, " +
                        "`ubicacion` TEXT NOT NULL, " +
                        "`detalle` TEXT NOT NULL, " +
                        "`categoria` TEXT NOT NULL, " +
                        "`severidad` TEXT NOT NULL, " +
                        "`estado` TEXT NOT NULL, " +
                        "`camara` TEXT NOT NULL, " +
                        "`confianzaIa` INTEGER NOT NULL, " +
                        "`duracionSeg` INTEGER NOT NULL, " +
                        "`fecha` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `incidencias` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`tipo` TEXT NOT NULL, " +
                        "`descripcion` TEXT NOT NULL, " +
                        "`fotoPath` TEXT, " +
                        "`eventoId` INTEGER, " +
                        "`fecha` INTEGER NOT NULL)"
                )
            }
        }

        // Singleton con doble verificación (thread-safe)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
