package cash.p.terminal.modules.manageaccount.recoveryphrase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.p.terminal.entities.Account

object RecoveryPhraseModule {
    class Factory(private val account: Account) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecoveryPhraseViewModel(account) as T
        }
    }

    data class WordNumbered(val word: String, val number: Int)

}
