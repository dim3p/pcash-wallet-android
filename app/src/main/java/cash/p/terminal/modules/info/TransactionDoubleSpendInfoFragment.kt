package cash.p.terminal.modules.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import cash.p.terminal.R
import cash.p.terminal.core.BaseComposeFragment
import cash.p.terminal.core.shorten
import cash.p.terminal.modules.info.ui.InfoHeader
import cash.p.terminal.ui.compose.ComposeAppTheme
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.AppBar
import cash.p.terminal.ui.compose.components.ButtonSecondaryDefault
import cash.p.terminal.ui.compose.components.CellSingleLineLawrence
import cash.p.terminal.ui.compose.components.MenuItem
import cash.p.terminal.ui.compose.components.TextImportantWarning
import cash.p.terminal.ui.compose.components.subhead2_grey
import io.horizontalsystems.core.helpers.HudHelper

class TransactionDoubleSpendInfoFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        InfoScreen(
            txHash = requireArguments().getString(TRANSACTION_HASH)!!,
            conflictingTxHash = requireArguments().getString(
                CONFLICTING_TRANSACTION_HASH
            )!!,
            onBackClick = { navController.popBackStack() }
        )
    }

    companion object {
        private const val TRANSACTION_HASH = "transaction_hash"
        private const val CONFLICTING_TRANSACTION_HASH = "conflicting_transaction_hash"

        fun prepareParams(transactionHash: String, conflictingTransactionHash: String) = bundleOf(
            TRANSACTION_HASH to transactionHash,
            CONFLICTING_TRANSACTION_HASH to conflictingTransactionHash,
        )
    }

}

@Composable
private fun InfoScreen(
    txHash: String,
    conflictingTxHash: String,
    onBackClick: () -> Unit
) {

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            AppBar(
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = onBackClick
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                InfoHeader(R.string.Info_DoubleSpend_Title)
                TextImportantWarning(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    text = stringResource(R.string.Info_DoubleSpend_Description),
                )
                ConflictingTransactions(txHash, conflictingTxHash)
                Spacer(Modifier.height(44.dp))
            }
        }
    }
}

@Composable
fun ConflictingTransactions(transactionHash: String, conflictingHash: String) {
    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        TransactionHashCell(R.string.Info_DoubleSpend_ThisTx, transactionHash)
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = ComposeAppTheme.colors.steel10
        )
        TransactionHashCell(R.string.Info_DoubleSpend_ConflictingTx, conflictingHash)
    }
}

@Composable
private fun TransactionHashCell(titleRes: Int, transactionHash: String) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    CellSingleLineLawrence() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            subhead2_grey(
                text = stringResource(titleRes),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            ButtonSecondaryDefault(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = transactionHash.shorten(),
                onClick = {
                    clipboardManager.setText(AnnotatedString(transactionHash))
                    HudHelper.showSuccessMessage(view, R.string.Hud_Text_Copied)
                }
            )
        }
    }
}

@Preview
@Composable
private fun Preview_InfoScreen() {
    ComposeAppTheme {
        InfoScreen(
            "jh2rnj23rnk2b3k42b2k4jb",
            "nb3k4brk34bk34bk34bk3g"
        ) { }
    }
}