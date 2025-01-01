package yong.jianwen.heatmap

import android.app.Application
import yong.jianwen.heatmap.data.AppContainer
import yong.jianwen.heatmap.data.AppDataContainer

class HeatMapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
