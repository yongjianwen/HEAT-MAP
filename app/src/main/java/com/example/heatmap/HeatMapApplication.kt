package com.example.heatmap

import android.app.Application
import com.example.heatmap.data.AppContainer
import com.example.heatmap.data.AppDataContainer

class HeatMapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
