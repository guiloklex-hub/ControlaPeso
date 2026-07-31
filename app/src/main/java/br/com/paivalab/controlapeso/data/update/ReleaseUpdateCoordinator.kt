package br.com.paivalab.controlapeso.data.update

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Starts one non-blocking release check for each application process. */
class ReleaseUpdateCoordinator(
    private val repository: ReleaseUpdateRepository,
    private val installedVersionName: String
) {
    private val hasStarted = AtomicBoolean(false)
    private val mutableResult = MutableStateFlow<ReleaseCheckResult?>(null)
    val result: StateFlow<ReleaseCheckResult?> = mutableResult

    fun check(scope: CoroutineScope) {
        if (!hasStarted.compareAndSet(false, true)) return
        scope.launch {
            mutableResult.value = try {
                repository.checkForUpdate(installedVersionName)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                ReleaseCheckResult.Failure(failure)
            }
        }
    }
}
