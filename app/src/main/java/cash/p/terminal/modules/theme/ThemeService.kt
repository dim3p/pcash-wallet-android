package cash.p.terminal.modules.theme

import androidx.appcompat.app.AppCompatDelegate
import cash.p.terminal.core.ILocalStorage
import cash.p.terminal.ui.compose.Select
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThemeService(private val localStorage: ILocalStorage) {
    private val themes by lazy { ThemeType.entries }

    val selectedTheme: ThemeType
        get() = localStorage.currentTheme

    private val _optionsFlow = MutableStateFlow(
        Select(selectedTheme, themes)
    )
    val optionsFlow = _optionsFlow.asStateFlow()

    fun setThemeType(themeType: ThemeType) {
        localStorage.currentTheme = themeType

        _optionsFlow.update {
            Select(themeType, themes)
        }

        val nightMode = when (themeType) {
            ThemeType.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeType.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeType.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
