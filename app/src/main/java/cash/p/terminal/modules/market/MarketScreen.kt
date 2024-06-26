package cash.p.terminal.modules.market

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cash.p.terminal.R
import cash.p.terminal.core.slideFromRight
import cash.p.terminal.core.stats.StatEvent
import cash.p.terminal.core.stats.StatPage
import cash.p.terminal.core.stats.stat
import cash.p.terminal.core.stats.statTab
import cash.p.terminal.modules.market.favorites.MarketFavoritesScreen
import cash.p.terminal.modules.market.overview.MarketOverviewScreen
import cash.p.terminal.modules.market.posts.MarketPostsScreen
import cash.p.terminal.ui.compose.ComposeAppTheme
import cash.p.terminal.ui.compose.TranslatableString
import cash.p.terminal.ui.compose.components.AppBar
import cash.p.terminal.ui.compose.components.MenuItem
import cash.p.terminal.ui.compose.components.TabItem
import cash.p.terminal.ui.compose.components.Tabs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketScreen(navController: NavController) {
    val marketViewModel = viewModel<MarketViewModel>(factory = MarketModule.Factory())
    val tabs = marketViewModel.tabs
    val selectedTab = marketViewModel.selectedTab

    val pagerState = rememberPagerState(initialPage = selectedTab.ordinal) { tabs.size }

    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = stringResource(R.string.Market_Title),
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Market_Search),
                    icon = R.drawable.icon_search,
                    onClick = {
                        navController.slideFromRight(R.id.marketSearchFragment)

                        stat(page = StatPage.Markets, event = StatEvent.Open(StatPage.MarketSearch))
                    }
                ),
                MenuItem(
                    title = TranslatableString.ResString(R.string.Market_Filters),
                    icon = R.drawable.ic_manage_2_24,
                    onClick = {
                        navController.slideFromRight(R.id.marketAdvancedSearchFragment)

                        stat(page = StatPage.Markets, event = StatEvent.Open(StatPage.AdvancedSearch))
                    }
                ),
            )
        )

        LaunchedEffect(key1 = selectedTab, block = {
            pagerState.scrollToPage(selectedTab.ordinal)
        })
        val tabItems = tabs.map {
            TabItem(stringResource(id = it.titleResId), it == selectedTab, it)
        }
        Tabs(tabItems, onClick = {
            marketViewModel.onSelect(it)

            stat(page = StatPage.Markets, event = StatEvent.SwitchTab(it.statTab))
        })

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when (tabs[page]) {
                MarketModule.Tab.Overview -> MarketOverviewScreen(navController)
                MarketModule.Tab.Posts -> MarketPostsScreen()
                MarketModule.Tab.Watchlist -> MarketFavoritesScreen(navController)
            }
        }
    }
}
