package br.com.paivalab.controlapeso.ui.profiles

import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
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
        assertEquals(now, stored.createdAt)
        assertEquals(true, stored.isActive)
        assertNull(viewModel.uiState.value.form)
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
        viewModel.setBirthDate("28-07-2026")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(ProfileFormError.FUTURE_BIRTH_DATE, viewModel.uiState.value.error)
        assertEquals(emptyList<Profile>(), repository.getAll())
    }

    private class FakeProfileRepository : ProfileRepository {
        private val profiles = MutableStateFlow<List<Profile>>(emptyList())

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
}
