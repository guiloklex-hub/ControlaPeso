package br.com.paivalab.controlapeso.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.data.backup.BackupRestoreResult
import br.com.paivalab.controlapeso.data.backup.JsonBackupManager
import br.com.paivalab.controlapeso.data.backup.RestoreMode
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity
import br.com.paivalab.controlapeso.data.local.entity.GoalEntity
import br.com.paivalab.controlapeso.data.local.entity.ScaleDeviceEntity
import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.repository.GoalRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.MeasurementRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.ProfileRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.ScaleDeviceRepositoryImpl
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlaPesoDatabaseTest {
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ControlaPesoDatabase::class.java
    )

    private lateinit var database: ControlaPesoDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ControlaPesoDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun deletingProfileUnassignsMeasurement_andRawPayloadRoundTrips() = runBlocking {
        val profile = profile("p1", active = true)
        database.profileDao().insert(profile)
        database.measurementDao().insert(measurement(profile.id))

        val stored = database.measurementDao().findById("m1")
        assertEquals("C0 68 28 8C", stored?.rawPayloadHex)
        assertNull(stored?.bodyFatPercent)

        database.profileDao().delete(profile)
        val unassigned = requireNotNull(database.measurementDao().findById("m1"))
        assertNull(unassigned.profileId)
        assertEquals("C0 68 28 8C", unassigned.rawPayloadHex)
    }

    @Test
    fun repositorySwitchesActiveProfileAtomically() = runBlocking {
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        val repository = ProfileRepositoryImpl(database.profileDao(), AppClock { instant })
        database.profileDao().insert(profile("p1", active = true))
        database.profileDao().insert(profile("p2", active = false))

        repository.setActive("p2")

        assertEquals("p2", repository.observeActive().first()?.id)
        assertEquals(1, repository.observeAll().first().count { it.isActive })
    }

    @Test
    fun repositoryInsertAlsoKeepsOnlyOneActiveProfile() = runBlocking {
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        val repository = ProfileRepositoryImpl(database.profileDao(), AppClock { instant })

        repository.insert(profile("p1", active = true).toDomain())
        repository.insert(profile("p2", active = true).toDomain())

        val profiles = repository.observeAll().first()
        assertEquals(1, profiles.count { it.isActive })
        assertEquals("p2", profiles.single { it.isActive }.id)
    }

    @Test
    fun duplicateQueryRespectsWeightTimeAndDevice() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        database.measurementDao().insert(measurement("p1"))
        val center = Instant.parse("2026-07-27T12:30:30Z")

        val duplicate = database.measurementDao().findProbableDuplicate(
            profileId = "p1",
            weightKg = 103.84,
            toleranceKg = 0.05,
            centerTime = center,
            windowStart = center.minusSeconds(120),
            windowEnd = center.plusSeconds(120),
            deviceAddress = "AA:BB:CC:DD:EE:FF"
        )
        val otherDevice = database.measurementDao().findProbableDuplicate(
            profileId = "p1",
            weightKg = 103.84,
            toleranceKg = 0.05,
            centerTime = center,
            windowStart = center.minusSeconds(120),
            windowEnd = center.plusSeconds(120),
            deviceAddress = "11:22:33:44:55:66"
        )

        assertNotNull(duplicate)
        assertNull(otherDevice)
    }

    @Test
    fun goalDaoKeepsOneActiveGoalPerProfileTransactionally() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        database.goalDao().insertWithActiveConstraint(goal("g1", "p1"))
        database.goalDao().insertWithActiveConstraint(goal("g2", "p1"))

        val goals = database.goalDao().getAll()

        assertEquals(1, goals.count { it.isActive })
        assertEquals("g2", goals.single { it.isActive }.id)
        assertEquals("PAUSED", goals.single { it.id == "g1" }.status)
    }

    @Test
    fun databaseSchemaStartsAtVersionTwo() {
        assertEquals(2, database.openHelper.readableDatabase.version)
    }

    @Test
    fun forgettingScaleNullsForeignKeyButPreservesHistoricalLabel() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        val device = ScaleDeviceEntity(
            id = "d1",
            displayName = "Yoda1",
            bluetoothAddress = "AA:BB:CC:DD:EE:FF",
            protocolName = "OKOK",
            lastConnectedAt = null,
            lastSeenAt = instant,
            isPreferred = true,
            createdAt = instant,
            updatedAt = instant
        )
        database.scaleDeviceDao().insert(device)
        database.measurementDao().insert(
            measurement("p1").copy(deviceId = device.id)
        )

        database.scaleDeviceDao().delete(device)

        val stored = requireNotNull(database.measurementDao().findById("m1"))
        assertNull(stored.deviceId)
        assertEquals("Yoda1", stored.deviceName)
        assertEquals("AA:BB:CC:DD:EE:FF", stored.deviceAddress)
    }

    @Test
    fun measurementRepositorySupportsInsertUpdateObserveAndDelete() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        val repository = MeasurementRepositoryImpl(database.measurementDao())
        val original = measurement("p1").toDomain()

        repository.insert(original)
        assertEquals("m1", repository.observeForProfile("p1").first().single().id)

        repository.update(original.copy(note = "revisada"))
        assertEquals("revisada", repository.findById("m1")?.note)

        assertEquals(true, repository.deleteById("m1"))
        assertNull(repository.findById("m1"))
    }

    @Test
    fun measurementRepositoryAssignsUnassignedMeasurements() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        val repository = MeasurementRepositoryImpl(database.measurementDao())
        val instant = Instant.parse("2026-07-27T12:30:00Z")
        repository.insert(measurement(null).toDomain())

        assertEquals("m1", repository.observeUnassigned().first().single().id)
        assertEquals(
            1,
            repository.assignToProfile(
                measurementIds = listOf("m1"),
                profileId = "p1",
                updatedAt = instant
            )
        )
        assertEquals("p1", repository.findById("m1")?.profileId)
        assertEquals(0, repository.observeUnassigned().first().size)
    }

    @Test
    fun invalidRestoreDoesNotMutateExistingLocalRows() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        database.measurementDao().insert(measurement("p1"))
        val instant = Instant.parse("2026-07-27T12:00:00Z")
        val manager = JsonBackupManager(
            database = database,
            profileRepository = ProfileRepositoryImpl(database.profileDao(), AppClock { instant }),
            measurementRepository = MeasurementRepositoryImpl(database.measurementDao()),
            goalRepository = GoalRepositoryImpl(database.goalDao()),
            deviceRepository = ScaleDeviceRepositoryImpl(database.scaleDeviceDao()),
            preferencesRepository = AppPreferencesRepository(
                InstrumentationRegistry.getInstrumentation().targetContext
            ),
            clock = AppClock { instant }
        )

        val result = manager.restore("{\"schemaVersion\":1", RestoreMode.REPLACE)

        assertTrue(result is BackupRestoreResult.Invalid)
        assertEquals("p1", database.measurementDao().findById("m1")?.profileId)
        assertEquals("C0 68 28 8C", database.measurementDao().findById("m1")?.rawPayloadHex)
    }

    @Test
    fun migrationToVersionTwoPreservesExistingRowsAndRawPayload() {
        val databaseName = "controla-peso-migration-${System.nanoTime()}"
        migrationTestHelper.createDatabase(databaseName, 1).apply {
            execSQL(
                "INSERT INTO profiles " +
                    "(id, name, avatarKey, heightCm, birthDate, preferredWeightUnit, " +
                    "healthConnectEnabled, isActive, createdAt, updatedAt) VALUES " +
                    "('p1', 'Pessoa', NULL, NULL, NULL, 'KILOGRAM', 0, 1, 0, 0)"
            )
            execSQL(
                "INSERT INTO weight_measurements " +
                    "(id, profileId, weightKg, measuredAt, zoneOffsetSeconds, source, " +
                    "isStable, deviceId, deviceName, deviceAddress, note, rawPayloadHex, " +
                    "impedanceOne, impedanceTwo, bodyFatPercent, muscleMassKg, " +
                    "bodyWaterPercent, boneMassKg, visceralFatLevel, metabolicAge, " +
                    "createdAt, updatedAt) VALUES " +
                    "('m1', 'p1', 75.0, 0, -10800, 'BLE', 1, NULL, 'Yoda1', " +
                    "'AA:BB:CC:DD:EE:FF', NULL, 'C0 68 28 8C', NULL, NULL, NULL, NULL, " +
                    "NULL, NULL, NULL, NULL, 0, 0)"
            )
            close()
        }

        val migrated = migrationTestHelper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            MIGRATION_1_2
        )
        migrated.query(
            "SELECT profileId, rawPayloadHex FROM weight_measurements WHERE id = 'm1'"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("p1", cursor.getString(0))
            assertEquals("C0 68 28 8C", cursor.getString(1))
        }
        migrated.execSQL("PRAGMA foreign_keys = ON")
        migrated.execSQL("DELETE FROM profiles WHERE id = 'p1'")
        migrated.query(
            "SELECT profileId, rawPayloadHex FROM weight_measurements WHERE id = 'm1'"
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(true, cursor.isNull(0))
            assertEquals("C0 68 28 8C", cursor.getString(1))
        }
        migrated.close()
    }

    @Test
    fun goalRepositoryAndScaleRepositoryKeepDomainRules() = runBlocking {
        database.profileDao().insert(profile("p1", active = true))
        val goalRepository = GoalRepositoryImpl(database.goalDao())
        val scaleRepository = ScaleDeviceRepositoryImpl(database.scaleDeviceDao())
        val firstGoal = goal("g1", "p1").toDomain()
        val secondGoal = goal("g2", "p1").toDomain()

        goalRepository.insert(firstGoal)
        goalRepository.insert(secondGoal)
        assertEquals("g2", goalRepository.observeActive("p1").first()?.id)

        val instant = Instant.parse("2026-07-27T12:00:00Z")
        val firstScale = ScaleDeviceEntity(
            id = "d1",
            displayName = "Balança 1",
            bluetoothAddress = "AA:BB:CC:DD:EE:01",
            protocolName = "OKOK",
            lastConnectedAt = null,
            lastSeenAt = instant,
            isPreferred = false,
            createdAt = instant,
            updatedAt = instant
        ).toDomain()
        val secondScale = firstScale.copy(
            id = "d2",
            displayName = "Balança 2",
            bluetoothAddress = "AA:BB:CC:DD:EE:02"
        )

        scaleRepository.insert(firstScale)
        scaleRepository.insert(secondScale)
        scaleRepository.setPreferred(secondScale)

        assertEquals(
            "d2",
            scaleRepository.observeAll().first().single { it.isPreferred }.id
        )
        assertEquals(
            "d1",
            scaleRepository.findByAddress("AA:BB:CC:DD:EE:01")?.id
        )
    }

    private fun profile(id: String, active: Boolean) = ProfileEntity(
        id = id,
        name = id,
        avatarKey = null,
        heightCm = null,
        birthDate = null,
        preferredWeightUnit = "KILOGRAM",
        healthConnectEnabled = false,
        isActive = active,
        createdAt = Instant.parse("2026-07-27T12:00:00Z"),
        updatedAt = Instant.parse("2026-07-27T12:00:00Z")
    )

    private fun measurement(profileId: String?) = WeightMeasurementEntity(
        id = "m1",
        profileId = profileId,
        weightKg = 103.8,
        measuredAt = Instant.parse("2026-07-27T12:30:00Z"),
        zoneOffsetSeconds = -10_800,
        source = "BLE",
        isStable = true,
        deviceId = null,
        deviceName = "Yoda1",
        deviceAddress = "AA:BB:CC:DD:EE:FF",
        note = null,
        rawPayloadHex = "C0 68 28 8C",
        impedanceOne = null,
        impedanceTwo = null,
        bodyFatPercent = null,
        muscleMassKg = null,
        bodyWaterPercent = null,
        boneMassKg = null,
        visceralFatLevel = null,
        metabolicAge = null,
        createdAt = Instant.parse("2026-07-27T12:30:00Z"),
        updatedAt = Instant.parse("2026-07-27T12:30:00Z")
    )

    private fun goal(id: String, profileId: String) = GoalEntity(
        id = id,
        profileId = profileId,
        startWeightKg = 80.0,
        targetWeightKg = 75.0,
        startDate = java.time.LocalDate.of(2026, 7, 27),
        targetDate = null,
        status = "ACTIVE",
        isActive = true,
        createdAt = Instant.parse("2026-07-27T12:00:00Z"),
        updatedAt = Instant.parse("2026-07-27T12:00:00Z")
    )
}
