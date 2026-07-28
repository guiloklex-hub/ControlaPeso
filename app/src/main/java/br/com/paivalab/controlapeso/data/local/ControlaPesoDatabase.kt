package br.com.paivalab.controlapeso.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
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
            ).build()
    }
}
