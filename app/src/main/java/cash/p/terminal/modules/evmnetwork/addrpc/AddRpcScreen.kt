package cash.p.terminal.modules.evmnetwork.addrpc

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cash.p.terminal.R
import cash.p.terminal.entities.DataState
import cash.p.terminal.modules.evmfee.ButtonsGroupWithShade
import cash.p.terminal.modules.swap.settings.Caution
import cash.p.terminal.ui.compose.ComposeAppTheme
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.AppBar
import cash.p.terminal.ui.compose.components.ButtonPrimaryYellow
import cash.p.terminal.ui.compose.components.FormsInput
import cash.p.terminal.ui.compose.components.FormsInputStateWarning
import cash.p.terminal.ui.compose.components.HeaderText
import cash.p.terminal.ui.compose.components.MenuItem

@Composable
fun AddRpcScreen(
    navController: NavController,
    arguments: Bundle,
    viewModel: AddRpcViewModel = viewModel(factory = AddRpcModule.Factory(arguments))
) {
    if (viewModel.viewState.closeScreen) {
        navController.popBackStack()
        viewModel.onScreenClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.AddEvmSyncSource_AddRPCSource),
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Button_Close),
                    icon = R.drawable.ic_close,
                    onClick = {
                        navController.popBackStack()
                    }
                )
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            HeaderText(stringResource(id = R.string.AddEvmSyncSource_RpcUrl))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                qrScannerEnabled = true,
                onValueChange = viewModel::onEnterRpcUrl,
                hint = "",
                state = getState(viewModel.viewState.urlCaution)
            )
            Spacer(modifier = Modifier.height(24.dp))

            HeaderText(stringResource(id = R.string.AddEvmSyncSource_BasicAuthentication))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                qrScannerEnabled = true,
                onValueChange = viewModel::onEnterBasicAuth,
                hint = ""
            )
            Spacer(Modifier.height(60.dp))
        }

        ButtonsGroupWithShade {
            ButtonPrimaryYellow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.Button_Add),
                onClick = { viewModel.onAddClick() },
            )
        }
    }
}

private fun getState(caution: Caution?) = when (caution?.type) {
    Caution.Type.Error -> DataState.Error(Exception(caution.text))
    Caution.Type.Warning -> DataState.Error(FormsInputStateWarning(caution.text))
    null -> null
}