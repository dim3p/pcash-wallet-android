package cash.p.terminal.modules.swap.oneinch

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.p.terminal.core.App
import cash.p.terminal.entities.Address
import cash.p.terminal.modules.swap.SwapMainModule
import cash.p.terminal.modules.swap.SwapViewItemHelper
import cash.p.terminal.modules.swap.allowance.SwapAllowanceService
import cash.p.terminal.modules.swap.allowance.SwapAllowanceViewModel
import cash.p.terminal.modules.swap.allowance.SwapPendingAllowanceService
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.marketkit.models.Token
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

object OneInchModule {

    class AllowanceViewModelFactory(
            private val service: OneInchSwapService
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SwapAllowanceViewModel::class.java -> {
                    SwapAllowanceViewModel(service, service.allowanceService, service.pendingAllowanceService, SwapViewItemHelper(App.numberFormatter)) as T
                }
                else -> throw IllegalArgumentException()
            }
        }

    }

    class Factory(dex: SwapMainModule.Dex) : ViewModelProvider.Factory {
        private val evmKit: EthereumKit by lazy { App.evmBlockchainManager.getEvmKitManager(dex.blockchainType).evmKitWrapper?.evmKit!! }
        private val oneIncKitHelper by lazy { OneInchKitHelper(evmKit) }
        private val allowanceService by lazy { SwapAllowanceService(oneIncKitHelper.smartContractAddress, App.adapterManager, evmKit) }
        private val pendingAllowanceService by lazy { SwapPendingAllowanceService(App.adapterManager, allowanceService) }
        private val service by lazy {
            OneInchSwapService(
                    dex,
                    tradeService,
                    allowanceService,
                    pendingAllowanceService,
                    App.adapterManager
            )
        }
        private val tradeService by lazy {
            OneInchTradeService(evmKit, oneIncKitHelper)
        }
        private val formatter by lazy {
            SwapViewItemHelper(App.numberFormatter)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                OneInchSwapViewModel::class.java -> {
                    OneInchSwapViewModel(service, tradeService, pendingAllowanceService, formatter) as T
                }
                SwapAllowanceViewModel::class.java -> {
                    SwapAllowanceViewModel(service, allowanceService, pendingAllowanceService, formatter) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}

@Parcelize
data class OneInchSwapParameters(
    val tokenFrom: Token,
    val tokenTo: Token,
    val amountFrom: BigDecimal,
    val amountTo: BigDecimal,
    val slippage: BigDecimal,
    val recipient: Address? = null
) : Parcelable