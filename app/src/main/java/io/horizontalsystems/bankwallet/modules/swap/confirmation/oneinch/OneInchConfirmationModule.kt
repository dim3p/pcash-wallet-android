package cash.p.terminal.modules.swap.confirmation.oneinch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.p.terminal.core.App
import cash.p.terminal.core.ethereum.CautionViewItemFactory
import cash.p.terminal.core.ethereum.EvmCoinServiceFactory
import cash.p.terminal.modules.evmfee.EvmFeeCellViewModel
import cash.p.terminal.modules.evmfee.IEvmGasPriceService
import cash.p.terminal.modules.evmfee.eip1559.Eip1559GasPriceService
import cash.p.terminal.modules.evmfee.legacy.LegacyGasPriceService
import cash.p.terminal.modules.send.evm.settings.SendEvmNonceService
import cash.p.terminal.modules.send.evm.settings.SendEvmNonceViewModel
import cash.p.terminal.modules.send.evm.settings.SendEvmSettingsService
import cash.p.terminal.modules.sendevmtransaction.SendEvmTransactionViewModel
import cash.p.terminal.modules.swap.SwapViewItemHelper
import cash.p.terminal.modules.swap.SwapMainModule.OneInchSwapParameters
import cash.p.terminal.modules.swap.oneinch.OneInchKitHelper
import io.horizontalsystems.ethereumkit.core.LegacyGasPriceProvider
import io.horizontalsystems.ethereumkit.core.eip1559.Eip1559GasPriceProvider
import io.horizontalsystems.marketkit.models.BlockchainType

object OneInchConfirmationModule {

    class Factory(val blockchainType: BlockchainType, private val oneInchSwapParameters: OneInchSwapParameters) : ViewModelProvider.Factory {

        private val evmKitWrapper by lazy { App.evmBlockchainManager.getEvmKitManager(blockchainType).evmKitWrapper!! }
        private val oneInchKitHelper by lazy { OneInchKitHelper(evmKitWrapper.evmKit) }
        private val token by lazy { App.evmBlockchainManager.getBaseToken(blockchainType)!! }
        private val gasPriceService: IEvmGasPriceService by lazy {
            val evmKit = evmKitWrapper.evmKit
            if (evmKit.chain.isEIP1559Supported) {
                val gasPriceProvider = Eip1559GasPriceProvider(evmKit)
                Eip1559GasPriceService(gasPriceProvider, evmKit)
            } else {
                val gasPriceProvider = LegacyGasPriceProvider(evmKit)
                LegacyGasPriceService(gasPriceProvider)
            }
        }
        private val feeService by lazy {
            OneInchFeeService(oneInchKitHelper, evmKitWrapper.evmKit, gasPriceService, oneInchSwapParameters)
        }
        private val coinServiceFactory by lazy {
            EvmCoinServiceFactory(
                token,
                App.marketKit,
                App.currencyManager,
                App.coinManager
            )
        }
        private val nonceService by lazy { SendEvmNonceService(evmKitWrapper.evmKit) }
        private val settingsService by lazy { SendEvmSettingsService(feeService, nonceService) }
        private val sendService by lazy {
            OneInchSendEvmTransactionService(
                evmKitWrapper,
                feeService,
                settingsService,
                SwapViewItemHelper(App.numberFormatter)
            )
        }
        private val cautionViewItemFactory by lazy { CautionViewItemFactory(coinServiceFactory.baseCoinService) }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SendEvmTransactionViewModel::class.java -> {
                    SendEvmTransactionViewModel(
                        sendService,
                        coinServiceFactory,
                        cautionViewItemFactory,
                        blockchainType = blockchainType,
                        contactsRepo = App.contactsRepository
                    ) as T
                }

                EvmFeeCellViewModel::class.java -> {
                    EvmFeeCellViewModel(feeService, gasPriceService, coinServiceFactory.baseCoinService) as T
                }

                SendEvmNonceViewModel::class.java -> {
                    SendEvmNonceViewModel(nonceService) as T
                }

                else -> throw IllegalArgumentException()
            }
        }
    }

}
