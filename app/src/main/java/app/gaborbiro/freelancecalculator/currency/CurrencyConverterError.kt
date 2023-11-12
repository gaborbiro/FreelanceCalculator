package app.gaborbiro.freelancecalculator.currency

class CurrencyConverterError(val code: Int, message: String) : RuntimeException(message)