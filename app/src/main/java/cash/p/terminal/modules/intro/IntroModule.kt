package cash.p.terminal.modules.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.p.terminal.core.App

object IntroModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntroViewModel(App.localStorage) as T
        }
    }

    data class IntroSliderData(
        val title: Int,
        val subtitle: Int,
        val imageLight: Int,
        val imageDark: Int
    )

}
