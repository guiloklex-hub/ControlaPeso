package br.com.paivalab.controlapeso.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.paivalab.controlapeso.data.local.converter.DatabaseConverters
import br.com.paivalab.controlapeso.data.local.dao.GoalDao
import br.com.paivalab.controlapeso.data.local.dao.MeasurementDao
import br.com.paivalab.controlapeso.data.local.dao.ProfileDao
import br.com.paivalab.controlapeso.data.local.dao.ScaleDeviceDao
import br.com.paivalab.controlapeso.data.local.entity.GoalEntity
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import br.com.paivalab.controlapeso.data.local.entity.ScaleDeviceEntity
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity

@Database(
    entities = [
        ProfileEntity::class,
        WeightMeasurementEntity::class,
        GoalEntity::class,
        ScaleDeviceEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class ControlaPesoDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun goalDao(): GoalDao
    abstract fun scaleDeviceDao(): ScaleDeviceDao

    companion object {
        const val NAME = "controla_peso.db"

        fun create(context: Context): ControlaPesoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                ControlaPesoDatabase::class.java,
                NAME
            ).addMigrations(MIGRATION_1_2).build()
    }
}

/**
 * Keeps existing measurements while allowing a profile to be assigned later.
 * The profile foreign key also changes from CASCADE to SET NULL so deleting a
 * profile cannot silently delete its measurement history.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE `weight_measurements_new` (
                `id` TEXT NOT NULL,
                `profileId` TEXT,
                `weightKg` REAL NOT NULL,
                `measuredAt` INTEGER NOT NULL,
                `zoneOffsetSeconds` INTEGER,
                `source` TEXT NOT NULL,
                `isStable` INTEGER NOT NULL,
                `deviceId` TEXT,
                `deviceName` TEXT,
                `deviceAddress` TEXT,
                `note` TEXT,
                `rawPayloadHex` TEXT,
                `impedanceOne` REAL,
                `impedanceTwo` REAL,
                `bodyFatPercent` REAL,
                `muscleMassKg` REAL,
                `bodyWaterPercent` REAL,
                `boneMassKg` REAL,
                `visceralFatLevel` REAL,
                `metabolicAge` INTEGER,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`deviceId`) REFERENCES `scale_devices`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO `weight_measurements_new` (
                `id`, `profileId`, `weightKg`, `measuredAt`, `zoneOffsetSeconds`,
                `source`, `isStable`, `deviceId`, `deviceName`, `deviceAddress`,
                `note`, `rawPayloadHex`, `impedanceOne`, `impedanceTwo`,
                `bodyFatPercent`, `muscleMassKg`, `bodyWaterPercent`, `boneMassKg`,
                `visceralFatLevel`, `metabolicAge`, `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `profileId`, `weightKg`, `measuredAt`, `zoneOffsetSeconds`,
                `source`, `isStable`, `deviceId`, `deviceName`, `deviceAddress`,
                `note`, `rawPayloadHex`, `impedanceOne`, `impedanceTwo`,
                `bodyFatPercent`, `muscleMassKg`, `bodyWaterPercent`, `boneMassKg`,
                `visceralFatLevel`, `metabolicAge`, `createdAt`, `updatedAt`
            FROM `weight_measurements`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `weight_measurements`")
        database.execSQL(
            "ALTER TABLE `weight_measurements_new` RENAME TO `weight_measurements`"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_weight_measurements_profileId_measuredAt` " +
                "ON `weight_measurements` (`profileId`, `measuredAt`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_weight_measurements_deviceId` " +
                "ON `weight_measurements` (`deviceId`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_weight_measurements_source` " +
                "ON `weight_measurements` (`source`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_weight_measurements_rawPayloadHex` " +
                "ON `weight_measurements` (`rawPayloadHex`)"
        )
    }
}
