package cash.p.terminal.modules.chart

import cash.p.terminal.entities.Currency
import cash.p.terminal.modules.coin.overview.ui.SelectedItem
import cash.p.terminal.modules.market.Value
import java.math.BigDecimal

object ChartModule {

    fun createViewModel(
        chartService: AbstractChartService,
        chartNumberFormatter: ChartNumberFormatter,
    ): ChartViewModel {
        return ChartViewModel(chartService, chartNumberFormatter)
    }

    interface ChartNumberFormatter {
        fun formatValue(currency: Currency, value: BigDecimal): String
    }

    data class ChartHeaderView(
        val value: String,
        val valueHint: String?,
        val date: String?,
        val diff: Value.Percent?,
        val extraData: ChartHeaderExtraData?
    )

    sealed class ChartHeaderExtraData {
        class Volume(val volume: String) : ChartHeaderExtraData()
        class Dominance(val dominance: String, val diff: Value.Percent?) : ChartHeaderExtraData()
        class Indicators(
            val movingAverages: List<SelectedItem.MA>,
            val rsi: Float?,
            val macd: SelectedItem.Macd?
        ) : ChartHeaderExtraData()
    }

}
