package app.gaborbiro.freelancecalculator.persistence

interface Mapper<T, S> {
    fun toStoreType(value: T?): S?
    fun fromStoreType(value: S?): T?
}