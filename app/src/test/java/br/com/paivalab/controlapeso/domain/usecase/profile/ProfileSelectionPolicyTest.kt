package br.com.paivalab.controlapeso.domain.usecase.profile

import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileSelectionPolicyTest {
    @Test
    fun `uses exactly one active profile`() {
        assertEquals(
            "b",
            ProfileSelectionPolicy.defaultProfileId(
                listOf(profile("a"), profile("b", active = true))
            )
        )
    }

    @Test
    fun `does not guess when several profiles have no active owner`() {
        assertNull(
            ProfileSelectionPolicy.defaultProfileId(
                listOf(profile("a"), profile("b"))
            )
        )
    }

    @Test
    fun `single profile is safe default`() {
        assertEquals("a", ProfileSelectionPolicy.defaultProfileId(listOf(profile("a"))))
    }

    private fun profile(id: String, active: Boolean = false) = Profile(
        id = id,
        name = id,
        avatarKey = null,
        heightCm = null,
        birthDate = null,
        preferredWeightUnit = WeightUnit.KILOGRAM,
        healthConnectEnabled = false,
        isActive = active,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )
}
