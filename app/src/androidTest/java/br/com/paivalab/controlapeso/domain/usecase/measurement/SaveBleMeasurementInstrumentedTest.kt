package br.com.paivalab.controlapeso.domain.usecase.measurement

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.paivalab.controlapeso.bluetooth.BleWeightReading
import br.com.paivalab.controlapeso.bluetooth.StableWeightEvent
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.data.local.ControlaPesoDatabase
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.repository.GoalRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.MeasurementRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.ScaleDeviceRepositoryImpl
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveBleMeasurementInstrumentedTest {
    private lateinit var database: ControlaPesoDatabase
    private lateinit var preferences: AppPreferencesRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ControlaPesoDatabase::class.java
        ).allowMainThreadQueries().build()
        preferences = AppPreferencesRepository(context)
    }

    @After
    fun tearDown() = runBlocking {
        preferences.clear()
        database.close()
    }

    @Test
    fun probableDuplicateDoesNotPersistDeviceBeforeConfirmation() = runBlocking {
        database.profileDao().insert(profile())
        database.measurementDao().insert(existingMeasurement())

        val measurementRepository = MeasurementRepositoryImpl(database.measurementDao())
        val deviceRepository = ScaleDeviceRepositoryImpl(database.scaleDeviceDao())
        val result = SaveBleMeasurement(
            measurementRepository = measurementRepository,
            deviceRepository = deviceRepository,
            goalRepository = GoalRepositoryImpl(database.goalDao()),
            preferencesRepository = preferences,
            clock = AppClock { NOW },
            idGenerator = IdGenerator { "generated-id" },
            zoneId = ZoneId.of("UTC")
        ).invoke(
            event = stableEvent(),
            profileId = PROFILE_ID,
            confirmedUnit = WeightUnit.KILOGRAM,
            note = null,
            source = MeasurementSource.BLE
        )

        assertTrue(result is SaveBleResult.ProbableDuplicate)
        assertEquals(0, deviceRepository.getAll().size)
        assertEquals(1, measurementRepository.getAll().size)
        assertEquals(
            RAW_PAYLOAD,
            (result as SaveBleResult.ProbableDuplicate).proposed.rawPayloadHex
        )
    }

    private fun profile() = ProfileEntity(
        id = PROFILE_ID,
        name = "Pessoa",
        avatarKey = null,
        heightCm = null,
        birthDate = null,
        preferredWeightUnit = WeightUnit.KILOGRAM.name,
        healthConnectEnabled = false,
        isActive = true,
        createdAt = NOW,
        updatedAt = NOW
    )

    private fun existingMeasurement() = WeightMeasurementEntity(
        id = "existing-measurement",
        profileId = PROFILE_ID,
        weightKg = 103.82,
        measuredAt = MEASURED_AT,
        zoneOffsetSeconds = 0,
        source = MeasurementSource.BLE.name,
        isStable = true,
        deviceId = null,
        deviceName = "Balança",
        deviceAddress = DEVICE_ADDRESS,
        note = null,
        rawPayloadHex = "00 11",
        impedanceOne = null,
        impedanceTwo = null,
        bodyFatPercent = null,
        muscleMassKg = null,
        bodyWaterPercent = null,
        boneMassKg = null,
        visceralFatLevel = null,
        metabolicAge = null,
        createdAt = MEASURED_AT,
        updatedAt = MEASURED_AT
    )

    private fun stableEvent() = StableWeightEvent(
        reading = BleWeightReading(
            deviceAddress = DEVICE_ADDRESS,
            deviceName = "Balança",
            advertisedValue = 103.82,
            rawWeight = 10382,
            property = BleWeightReading.PROPERTY_MEASUREMENT,
            sequenceNumber = 1,
            rssi = -50,
            observedAtEpochMillis = MEASURED_AT.plusSeconds(30).toEpochMilli(),
            rawPayloadHex = RAW_PAYLOAD
        ),
        sampleCount = 8,
        windowDurationMillis = 2_000,
        valueRange = 0.0
    )

    private companion object {
        const val PROFILE_ID = "profile-1"
        const val DEVICE_ADDRESS = "AA:BB:CC:DD:EE:FF"
        const val RAW_PAYLOAD = "C0 68 28 8C"
        val MEASURED_AT: Instant = Instant.parse("2026-07-27T12:30:00Z")
        val NOW: Instant = Instant.parse("2026-07-27T12:31:00Z")
    }
}
