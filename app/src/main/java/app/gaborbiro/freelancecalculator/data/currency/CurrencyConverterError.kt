package app.gaborbiro.freelancecalculator.data.currency

class CurrencyConverterError(val code: Int, message: String) : RuntimeException(message)