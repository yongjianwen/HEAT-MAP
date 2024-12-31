package com.example.heatmap.data

interface Selectable {
    val id: Int

    fun getDisplayName(): String
    fun getLabelName(): String
}
