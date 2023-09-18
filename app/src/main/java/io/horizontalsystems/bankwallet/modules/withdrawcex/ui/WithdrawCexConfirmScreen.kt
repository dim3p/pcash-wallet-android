package cash.p.terminal.modules.withdrawcex.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import cash.p.terminal.R
import cash.p.terminal.modules.evmfee.ButtonsGroupWithShade
import cash.p.terminal.modules.send.ConfirmAmountCell
import cash.p.terminal.modules.withdrawcex.WithdrawCexViewModel
import cash.p.terminal.ui.compose.ComposeAppTheme
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.*
import kotlinx.coroutines.launch

@Composable
fun WithdrawCexConfirmScreen(
    mainViewModel: WithdrawCexViewModel,
    openVerification: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClose: () -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    ComposeAppTheme {
        val confirmationData = mainViewModel.getConfirmationData()

        val assetName = confirmationData.assetName
        val coinAmount = confirmationData.coinAmount
        val currencyAmount = confirmationData.currencyAmount
        val coinIconUrl = confirmationData.coinIconUrl
        val address = confirmationData.address
        val blockchainType = confirmationData.blockchainType
        val networkName = confirmationData.networkName

        Scaffold(
            backgroundColor = ComposeAppTheme.colors.tyler,
            topBar = {
                AppBar(
                    title = TranslatableString.ResString(R.string.Send_Confirmation_Title),
                    navigationIcon = {
                        HsBackButton(onClick = onNavigateBack)
                    },
                    menuItems = listOf(
                        MenuItem(
                            title = TranslatableString.ResString(R.string.Button_Close),
                            icon = R.drawable.ic_close,
                            onClick = onClose
                        )
                    )
                )
            }
        ) {
            Column(modifier = Modifier.padding(it)) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                ) {
                    VSpacer(12.dp)
                    val topSectionItems = buildList<@Composable () -> Unit> {
                        add {
                            SectionTitleCell(
                                stringResource(R.string.Send_Confirmation_YouSend),
                                assetName,
                                R.drawable.ic_arrow_up_right_12
                            )
                        }
                        add {
                            ConfirmAmountCell(currencyAmount, coinAmount, coinIconUrl)
                        }
                        add {
                            TransactionInfoAddressCell(
                                title = stringResource(R.string.Send_Confirmation_To),
                                value = address.hex,
                                showAdd = false,
                                blockchainType = blockchainType,
                                navController = navController
                            )
                        }
//                        contact?.let {
//                            add {
//                                TransactionInfoContactCell(name = contact.name)
//                            }
//                        }
                    }

                    CellUniversalLawrenceSection(topSectionItems)

                    VSpacer(16.dp)

                    networkName?.let { networkName ->
                        CellUniversalLawrenceSection(
                            listOf {
                                TransactionInfoCell(
                                    stringResource(R.string.CexWithdraw_Network),
                                    networkName
                                )
                            }
                        )
                    }
                    VSpacer(16.dp)
                }

                var confirmEnabled by remember { mutableStateOf(true) }

                ButtonsGroupWithShade {
                    ButtonPrimaryYellow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                        title = stringResource(R.string.CexWithdraw_Withdraw),
                        onClick = {
                            coroutineScope.launch {
                                confirmEnabled = false
                                val withdrawId = mainViewModel.confirm()
                                if (withdrawId != null) {
                                    openVerification.invoke(withdrawId)
                                }
                                confirmEnabled = true
                            }
                        },
                        enabled = confirmEnabled
                    )
                }
            }
        }
    }
}