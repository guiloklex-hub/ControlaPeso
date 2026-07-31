package br.com.paivalab.controlapeso.data.update

import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.flow.first

class AppPreferencesReleaseUpdateMetadataStore(
    private val preferencesRepository: AppPreferencesRepository
) : ReleaseUpdateMetadataStore {
    override suspend fun getEtag(): String? =
        preferencesRepository.preferences.first().releaseUpdateEtag

    override suspend fun getCachedRelease(): String? =
        preferencesRepository.preferences.first().releaseUpdateCachedRelease

    override suspend fun setCachedRelease(eTag: String?, releaseJson: String?) =
        preferencesRepository.setReleaseUpdateCache(eTag, releaseJson)
}
