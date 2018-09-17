package com.github.sumimakito.rhythmview.datasource

/**
 * Base class for data sources.
 *
 * You may create a customized data source by extending this class.
 *
 * `resolution` defines the number of items in this data source.
 */
abstract class BaseDataSource<T>(val resolution: Int) {
    var data: Array<T>? = null
}