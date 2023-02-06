package cash.p.terminal.modules.send.bitcoin.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import cash.p.terminal.R
import cash.p.terminal.entities.TransactionDataSortMode
import cash.p.terminal.modules.btcblockchainsettings.BlockchainSettingCell
import cash.p.terminal.modules.hodler.HSHodlerInput
import cash.p.terminal.modules.send.bitcoin.TransactionInputsSortInfoPage
import cash.p.terminal.modules.send.bitcoin.advanced.SendBtcAdvancedSettingsModule.SortModeViewItem
import cash.p.terminal.ui.compose.ComposeAppTheme
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.*
import io.horizontalsystems.hodler.LockTimeInterval
import io.horizontalsystems.marketkit.models.BlockchainType

@Composable
fun SendBtcAdvancedSettingsScreen(
    navController: NavHostController,
    blockchainType: BlockchainType,
    lockTimeEnabled: Boolean,
    lockTimeIntervals: List<LockTimeInterval?>,
    lockTimeInterval: LockTimeInterval?,
    onEnterLockTimeInterval: (LockTimeInterval?) -> Unit,
) {
    var selectedLockTimeInterval by remember { mutableStateOf(lockTimeInterval) }
    ComposeAppTheme {
        Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
            AppBar(
                title = TranslatableString.ResString(R.string.Send_Advanced),
                navigationIcon = {
                    HsIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "back button",
                            tint = ComposeAppTheme.colors.jacob
                        )
                    }
                },
            )
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val viewModel: SendBtcAdvancedSettingsViewModel =
                    viewModel(factory = SendBtcAdvancedSettingsModule.Factory(blockchainType))

                TransactionDataSortSettings(
                    navController,
                    viewModel.sortModeViewItems
                ) { viewModel.setTransactionMode(it) }

                if (lockTimeEnabled) {
                    Spacer(Modifier.height(32.dp))
                    CellUniversalLawrenceSection(
                        listOf {
                            HSHodlerInput(
                                lockTimeIntervals = lockTimeIntervals,
                                lockTimeInterval = selectedLockTimeInterval,
                                onSelect = {
                                    selectedLockTimeInterval = it
                                    onEnterLockTimeInterval(it)
                                }
                            )
                        }
                    )
                    InfoText(
                        text = stringResource(R.string.Send_Hodler_Description),
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TransactionDataSortSettings(
    navController: NavController,
    sortModeViewItems: List<SortModeViewItem>,
    onSelect: (TransactionDataSortMode) -> Unit
) {
    SettingSection(
        items = sortModeViewItems,
        settingTitleTextRes = R.string.BtcBlockchainSettings_TransactionInputsOutputs,
        settingDescriptionTextRes = R.string.BtcBlockchainSettings_TransactionInputsOutputsSettingsDescription,
        onItemClick = { viewItem -> onSelect.invoke(viewItem.mode) },
        navController = navController
    )
}

@Composable
private fun SettingSection(
    items: List<SortModeViewItem>,
    settingTitleTextRes: Int,
    settingDescriptionTextRes: Int,
    onItemClick: (SortModeViewItem) -> Unit,
    navController: NavController
) {
    HeaderText(
        text = stringResource(settingTitleTextRes),
        onInfoClick = {
            navController.navigate(TransactionInputsSortInfoPage)
        })
    CellUniversalLawrenceSection(items) { item ->
        BlockchainSettingCell(
            title = stringResource(item.mode.title),
            subtitle = stringResource(item.mode.description),
            checked = item.selected
        ) {
            onItemClick(item)
        }
    }
    InfoText(
        text = stringResource(settingDescriptionTextRes),
    )
}