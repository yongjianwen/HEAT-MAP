package yong.jianwen.heatmap

import android.content.Context

enum class CurrentPage(
    private val resourceId: Int
) {
    HOME(R.string.app_name),
    TRIP_DETAIL(R.string.trip_detail_screen_title),
    MAP(R.string.map_screen_title);

    fun getDisplayName(context: Context): String {
        return context.getString(resourceId)
    }
}
