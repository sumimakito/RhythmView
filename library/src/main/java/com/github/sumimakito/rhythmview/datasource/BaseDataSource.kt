package com.github.sumimakito.rhythmview.datasource

abstract class BaseDataSource<T>(val size: Int) {
    var data: Array<T>? = null
}