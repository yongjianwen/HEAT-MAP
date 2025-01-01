package yong.jianwen.heatmap.data

interface Selectable {
    val id: Int

    fun getDisplayName(): String
    fun getLabelName(): String
}
