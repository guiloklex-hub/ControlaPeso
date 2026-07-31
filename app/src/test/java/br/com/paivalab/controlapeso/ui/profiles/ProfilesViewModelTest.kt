package br.com.paivalab.controlapeso.ui.profiles

import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.data.profile.ProfilePhotoStore
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-07-27T12:00:00Z")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun firstCreatedProfileBecomesActiveAndUsesDeterministicClock() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "profile-1" }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startCreate()
        viewModel.setName("Pessoa")
        viewModel.setHeight("172,5")
        viewModel.setUnit(WeightUnit.POUND)
        viewModel.save()
        advanceUntilIdle()

        val stored = repository.getAll().single()
        assertEquals("profile-1", stored.id)
        assertEquals(172.5, stored.heightCm ?: 0.0, 0.0)
        assertEquals(WeightUnit.POUND, stored.preferredWeightUnit)
        assertEquals(false, stored.healthConnectEnabled)
        assertEquals(now, stored.createdAt)
        assertEquals(true, stored.isActive)
        assertNull(viewModel.uiState.value.form)
    }

    @Test
    fun newProfileKeepsHealthConnectDisabledUntilExplicitOptIn() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "profile-1" }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startCreate()
        viewModel.setName("Pessoa")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(false, repository.getAll().single().healthConnectEnabled)
    }

    @Test
    fun editingProfileStartsWithItsPreferredUnit() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val profile = Profile(
            id = "profile-1",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.POUND,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        repository.insert(profile)
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "unused" }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startEdit(profile)
        advanceUntilIdle()

        assertEquals(WeightUnit.POUND, viewModel.uiState.value.form?.unit)
    }

    @Test
    fun invalidFutureBirthDateDoesNotWriteRepository() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "profile-1" }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startCreate()
        viewModel.setName("Pessoa")
        viewModel.setBirthDate("28/07/2026")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(ProfileFormError.FUTURE_BIRTH_DATE, viewModel.uiState.value.error)
        assertEquals(emptyList<Profile>(), repository.getAll())
    }

    @Test
    fun selectedPhotoIsStoredPrivatelyAfterProfileReceivesAnId() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val photoStore = FakeProfilePhotoStore()
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "profile-1" },
            photoStore = photoStore
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startCreate()
        viewModel.setName("Pessoa")
        viewModel.setPhotoUri("content://photos/pessoa")
        viewModel.save()
        advanceUntilIdle()

        assertEquals("profile-1", photoStore.savedProfileId)
        assertEquals("content://photos/pessoa", photoStore.savedSource)
        assertEquals(
            "/private/profile-1.photo",
            viewModel.uiState.value.photoPaths["profile-1"]
        )
    }

    @Test
    fun failedProfileEditRestoresThePreviousPrivatePhoto() = runTest(dispatcher) {
        val repository = FakeProfileRepository()
        val existing = Profile(
            id = "profile-1",
            name = "Pessoa",
            avatarKey = "ocean",
            heightCm = null,
            birthDate = null,
            preferredWeightUnit = WeightUnit.KILOGRAM,
            healthConnectEnabled = false,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        repository.insert(existing)
        repository.failUpdates = true
        val photoStore = FakeProfilePhotoStore().apply {
            seed("profile-1", "/private/old.photo", "content://photos/old")
        }
        val viewModel = ProfilesViewModel(
            repository = repository,
            clock = AppClock { now },
            idGenerator = IdGenerator { "unused" },
            photoStore = photoStore
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }

        viewModel.startEdit(existing)
        viewModel.setPhotoUri("content://photos/new")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(ProfileFormError.SAVE_FAILED, viewModel.uiState.value.error)
        assertEquals("/private/old.photo", photoStore.pathFor("profile-1"))
        assertEquals("content://photos/old", photoStore.sourceFor("profile-1"))
        assertEquals(existing, repository.findById("profile-1"))
    }

    private class FakeProfileRepository : ProfileRepository {
        private val profiles = MutableStateFlow<List<Profile>>(emptyList())
        var failUpdates: Boolean = false

        override fun observeAll(): Flow<List<Profile>> = profiles

        override fun observeActive(): Flow<Profile?> =
            profiles.map { values -> values.firstOrNull(Profile::isActive) }

        override suspend fun findById(id: String): Profile? =
            profiles.value.firstOrNull { it.id == id }

        override suspend fun getAll(): List<Profile> = profiles.value

        override suspend fun insert(profile: Profile) {
            profiles.value += profile
        }

        override suspend fun update(profile: Profile) {
            check(!failUpdates) { "Falha de teste ao atualizar perfil." }
            profiles.value = profiles.value.map {
                if (it.id == profile.id) profile else it
            }
        }

        override suspend fun delete(profile: Profile) {
            profiles.value = profiles.value.filterNot { it.id == profile.id }
        }

        override suspend fun setActive(id: String): Boolean {
            if (profiles.value.none { it.id == id }) return false
            profiles.value = profiles.value.map { it.copy(isActive = it.id == id) }
            return true
        }
    }

    private class FakeProfilePhotoStore : ProfilePhotoStore {
        private val paths = mutableMapOf<String, String>()
        private val sources = mutableMapOf<String, String>()
        var savedProfileId: String? = null
        var savedSource: String? = null

        override fun pathFor(profileId: String): String? = paths[profileId]

        override fun hasPhoto(profileId: String): Boolean = profileId in paths

        override suspend fun save(profileId: String, sourceUri: String): String {
            savedProfileId = profileId
            savedSource = sourceUri
            sources[profileId] = sourceUri
            return "/private/$profileId.photo".also { paths[profileId] = it }
        }

        override suspend fun <T> withPhotoChange(
            profileId: String,
            sourceUri: String?,
            removePhoto: Boolean,
            block: suspend () -> T
        ): T {
            val previousPath = paths[profileId]
            val previousSource = sources[profileId]
            try {
                if (removePhoto) delete(profileId)
                sourceUri?.let { save(profileId, it) }
                return block()
            } catch (failure: Throwable) {
                if (previousPath == null) {
                    delete(profileId)
                } else {
                    paths[profileId] = previousPath
                    previousSource?.let { sources[profileId] = it }
                }
                throw failure
            }
        }

        override fun delete(profileId: String) {
            paths.remove(profileId)
            sources.remove(profileId)
        }

        override fun deleteAll() {
            paths.clear()
            sources.clear()
        }

        fun seed(profileId: String, path: String, source: String) {
            paths[profileId] = path
            sources[profileId] = source
        }

        fun sourceFor(profileId: String): String? = sources[profileId]
    }
}
