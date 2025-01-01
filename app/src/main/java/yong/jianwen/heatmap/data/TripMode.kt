package yong.jianwen.heatmap.data

enum class TripMode : Selectable {
    DRIVING {
        override val id: Int = 0
        override fun getDisplayName(): String = "Driving"
        override fun getLabelName(): String = "drive"
    },
    SEATED {
        override val id: Int = 1
        override fun getDisplayName(): String = "Seated"
        override fun getLabelName(): String = "trip"
    }
}
