package cash.p.terminal.modules.walletconnect.request.signmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cash.p.terminal.core.App
import cash.p.terminal.modules.walletconnect.request.signmessage.v1.WC1SignMessageRequestService
import cash.p.terminal.modules.walletconnect.request.signmessage.v2.WC2SignMessageRequestService
import cash.p.terminal.modules.walletconnect.version1.WC1Service
import cash.p.terminal.modules.walletconnect.version1.WC1SignMessageRequest
import cash.p.terminal.modules.walletconnect.version2.WC2SessionManager

object WCSignMessageRequestModule {

    const val TYPED_MESSAGE = "typed_message"

    class Factory(
        private val signMessageRequest: WC1SignMessageRequest,
        private val dAppName: String?,
        private val baseService: WC1Service
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                WCSignMessageRequestViewModel::class.java -> {
                    val service = WC1SignMessageRequestService(
                        signMessageRequest,
                        dAppName,
                        baseService,
                        baseService.evmKitWrapper?.signer!!
                    )
                    WCSignMessageRequestViewModel(service) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

    class FactoryWC2(private val requestData: WC2SessionManager.RequestData) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                WCSignMessageRequestViewModel::class.java -> {
                    val service = WC2SignMessageRequestService(
                        requestData,
                        App.wc2SessionManager
                    )
                    WCSignMessageRequestViewModel(service) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

    interface RequestAction {
        val dAppName: String?
        val message: SignMessage
        val isLegacySignRequest: Boolean
        fun sign()
        fun reject()
    }

}

sealed class SignMessage(val data: String) {
    class Message(data: String, val showLegacySignWarning: Boolean = false) : SignMessage(data)
    class PersonalMessage(data: String) : SignMessage(data)
    class TypedMessage(data: String, val domain: String? = null) : SignMessage(data)
}