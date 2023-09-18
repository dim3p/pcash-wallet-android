package cash.p.terminal.modules.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import cash.p.terminal.R
import cash.p.terminal.core.BaseActivity

class LockScreenActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController(R.id.lockScreenNavHost)
                if (navController.currentDestination?.id == navController.graph.startDestinationId) {
                    finishAffinity()
                }
            }
        })
    }

    companion object {
        fun start(context: Activity) {
            val intent = Intent(context, LockScreenActivity::class.java)
            context.startActivity(intent)
        }
    }
}
