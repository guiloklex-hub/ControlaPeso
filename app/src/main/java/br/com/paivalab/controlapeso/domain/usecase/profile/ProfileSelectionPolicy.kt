package br.com.paivalab.controlapeso.domain.usecase.profile

import br.com.paivalab.controlapeso.domain.model.Profile

object ProfileSelectionPolicy {
    fun defaultProfileId(profiles: List<Profile>): String? =
        profiles.singleOrNull(Profile::isActive)?.id
            ?: profiles.singleOrNull()?.id
}
