package io.homeassistant.companion.android.launch

import android.util.Log
import io.homeassistant.companion.android.BuildConfig
import io.homeassistant.companion.android.common.data.authentication.AuthenticationRepository
import io.homeassistant.companion.android.common.data.integration.DeviceRegistration
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class LaunchPresenterImpl @Inject constructor(
    view: LaunchView,
    authenticationUseCase: AuthenticationRepository,
    integrationUseCase: IntegrationRepository
) : LaunchPresenterBase(view, authenticationUseCase, integrationUseCase) {
    override fun resyncRegistration() {
        mainScope.launch {
            try {
                integrationUseCase.updateRegistration(
                    DeviceRegistration(
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    )
                )
                integrationUseCase.setHomeAssistantVersion()
            } catch (e: Exception) {
                Log.e(TAG, "Issue updating Registration", e)
            }
        }
    }
}
