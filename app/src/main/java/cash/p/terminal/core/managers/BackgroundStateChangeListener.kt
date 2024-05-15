package cash.p.terminal.core.managers

import android.app.Activity
import cash.p.terminal.core.stats.StatsManager
import cash.p.terminal.modules.keystore.KeyStoreActivity
import cash.p.terminal.modules.lockscreen.LockScreenActivity
import io.horizontalsystems.core.BackgroundManager
import io.horizontalsystems.core.IKeyStoreManager
import io.horizontalsystems.core.IPinComponent
import io.horizontalsystems.core.ISystemInfoManager
import io.horizontalsystems.core.security.KeyStoreValidationResult

class BackgroundStateChangeListener(
    private val systemInfoManager: ISystemInfoManager,
    private val keyStoreManager: IKeyStoreManager,
    private val pinComponent: IPinComponent,
    private val statsManager: StatsManager
) : BackgroundManager.Listener {

    override fun willEnterForeground(activity: Activity) {
        if (systemInfoManager.isSystemLockOff) {
            KeyStoreActivity.startForNoSystemLock(activity)
            return
        }

        when (keyStoreManager.validateKeyStore()) {
            KeyStoreValidationResult.UserNotAuthenticated -> {
                KeyStoreActivity.startForUserAuthentication(activity)
                return
            }
            KeyStoreValidationResult.KeyIsInvalid -> {
                KeyStoreActivity.startForInvalidKey(activity)
                return
            }
            KeyStoreValidationResult.KeyIsValid -> { /* Do nothing */}
        }

        pinComponent.willEnterForeground(activity)

        if (pinComponent.shouldShowPin(activity)) {
            LockScreenActivity.start(activity)
        }

        // TODO: we'll not use anonymous stats from upstream code
        // statsManager.sendStats()
    }

    override fun didEnterBackground() {
        pinComponent.didEnterBackground()
    }

    override fun onAllActivitiesDestroyed() {
        pinComponent.lock()
    }

}
