package cash.p.terminal.modules.coin.majorholders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.p.terminal.R
import cash.p.terminal.core.brandColor
import cash.p.terminal.core.managers.MarketKitWrapper
import cash.p.terminal.entities.ViewState
import cash.p.terminal.modules.coin.CoinViewFactory
import cash.p.terminal.modules.coin.MajorHolderItem
import cash.p.terminal.modules.coin.majorholders.CoinMajorHoldersModule.UiState
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.StackBarSlice
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.TokenHolders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class CoinMajorHoldersViewModel(
    private val coinUid: String,
    private val blockchain: Blockchain,
    private val marketKit: MarketKitWrapper,
    private val factory: CoinViewFactory
) : ViewModel() {

    private var viewState: ViewState = ViewState.Loading
    private var top10Share: String = ""
    private var totalHoldersCount: String = ""
    private var seeAllUrl: String? = null
    private var chartData: List<StackBarSlice> = emptyList()
    private var topHolders: List<MajorHolderItem> = emptyList()
    private var error: TranslatableString? = null

    var uiState by mutableStateOf(UiState(viewState))
        private set

    init {
        fetch()
    }

    fun onErrorClick() {
        viewState = ViewState.Loading
        error = null
        syncState()
        fetch()
    }

    fun errorShown() {
        error = null
        syncState()
    }

    private fun syncState() {
        uiState = UiState(
            viewState = viewState,
            top10Share = top10Share,
            totalHoldersCount  = totalHoldersCount,
            seeAllUrl = seeAllUrl,
            chartData = chartData,
            topHolders = topHolders,
            error = error
        )
    }

    private fun fetch() {
        viewModelScope.launch {
            delay(200)
            try {
                val result = getTokenHolders(coinUid, blockchain.uid)
                val top10ShareNumber = result.topHolders.sumOf { it.percentage }
                seeAllUrl = result.holdersUrl
                top10Share = factory.getTop10Share(top10ShareNumber)
                totalHoldersCount = factory.getHoldersCount(result.count)
                viewState = ViewState.Success
                chartData = getChartData(top10ShareNumber.toFloat(), blockchain)
                topHolders = factory.getCoinMajorHolders(result)
                error = null
            } catch (e: Throwable) {
                viewState = ViewState.Error(e)
                error = errorText(e)
            }
            syncState()
        }
    }

    private suspend fun getTokenHolders(coinUid: String, blockchainUid: String): TokenHolders = withContext(Dispatchers.IO) {
        marketKit.tokenHoldersSingle(coinUid, blockchainUid).await()
    }

    private fun getChartData(top10ShareFloat: Float, blockchain: Blockchain): List<StackBarSlice> {
        val remaining = 100f - top10ShareFloat
        return listOf(
            StackBarSlice(value = top10ShareFloat, color = blockchain.type.brandColor ?: Color(0xFFFFA800)),
            StackBarSlice(value = remaining, color = Color(0x806B7196)),
        )
    }

    private fun errorText(error: Throwable): TranslatableString {
        return when (error) {
            is UnknownHostException -> TranslatableString.ResString(R.string.Hud_Text_NoInternet)
            else -> TranslatableString.PlainString(error.message ?: error.javaClass.simpleName)
        }
    }
}
