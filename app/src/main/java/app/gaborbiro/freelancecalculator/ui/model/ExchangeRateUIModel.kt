package app.gaborbiro.freelancecalculator.ui.model

data class ExchangeRateUIModel(
    val rate: Double?,
    val since: String,
    val error: Boolean,
)