package yong.jianwen.heatmap

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

fun generateTripName(baseName: String): String {
    // TODO: to auto suggest trip name when trip is ended by user
    return String.format(baseName, toDateTime(getCurrentDateTime(), "dd MMM yyyy h:mma"))
}

fun generateTrackName(baseName: String, tripMode: TripMode, car: Car? = null): String {
    return String.format(baseName, getTimeOfDay(), tripMode.getLabelName())
}

fun getTimeOfDay(): String {
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
    return when (hourOfDay) {
        in 0..11 -> "Morning"
        12 -> "Noon"
        in 13..17 -> "Afternoon"
        in 18..19 -> "Evening"
        else -> "Night"
    }
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDateTime(): String {
    val calendar = Calendar.getInstance()
    val timezone = calendar.timeZone
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    return formatter.format(calendar.time) + "GMT" + timezone.rawOffset
}

@SuppressLint("SimpleDateFormat")
fun formatTripStartEndTimes(
    start: String,
    end: String,
    outputPattern: String,
    outputPatternSameDay: String
): String {
    val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val startTime: Date? = try {
        inputFormatter.parse(start)!!
    } catch (e: ParseException) {
        null
    }
    val endTime: Date? = try {
        inputFormatter.parse(end)!!
    } catch (e: ParseException) {
        null
    }

    val outputFormatter = SimpleDateFormat(outputPattern)
    return if (startTime != null && endTime != null) {
        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        startCal.time = startTime
        endCal.time = endTime

        outputFormatter.format(startTime) + " - " +
                if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
                    && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
                    && startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH)
                ) {
                    val outputFormatterSameDay = SimpleDateFormat(outputPatternSameDay)
                    outputFormatterSameDay.format(endTime)
                } else {
                    outputFormatter.format(endTime)
                }
    } else if (startTime != null) {
        outputFormatter.format(startTime)
    } else {
        "--"
    }
}

@SuppressLint("SimpleDateFormat")
fun formatTrackStartEndTimes(
    tripStart: String,
    tripEnd: String,
    start: String,
    end: String,
    outputPattern: String,
    dayString: String
): String {
    val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val tripStartTime: Date? = try {
        inputFormatter.parse(tripStart)!!
    } catch (e: ParseException) {
        null
    }
    val tripEndTime: Date? = try {
        inputFormatter.parse(tripEnd)!!
    } catch (e: ParseException) {
        null
    }
    val startTime: Date? = try {
        inputFormatter.parse(start)!!
    } catch (e: ParseException) {
        null
    }
    val endTime: Date? = try {
        inputFormatter.parse(end)!!
    } catch (e: ParseException) {
        null
    }

    val outputFormatter = SimpleDateFormat(outputPattern)
    // start
    // start != tripStart -> Day 1
    // start == tripStart -> today -> ''
    // start == tripStart -> !tripEnd && not today -> Day 1
    // end
    // end date != start date -> Day 2
    // end date == start date -> ''
    // !end
    return if (startTime != null) {
        val startCal = Calendar.getInstance()
        startCal.time = startTime

        if (tripStartTime != null) {
            val tripStartCal = Calendar.getInstance()
            tripStartCal.time = tripStartTime

            val day = isDaysAfter(startCal, tripStartCal)
            if (// track start date != trip start date
                day != 0
                // track start date == trip start date, but trip has not ended, and it is not today
                || (tripEndTime == null && isDaysAfter(startCal, Calendar.getInstance()) < 0)
            ) {
                String.format(dayString, day + 1)
            } else if (tripEndTime != null) {
                // trip has ended, and trip end date != trip start date
                val tripEndCal = Calendar.getInstance()
                tripEndCal.time = tripEndTime
                if (isDaysAfter(tripEndCal, tripStartCal) > 0) {
                    String.format(dayString, 1)
                } else {
                    ""
                }
            } else {
                ""
            }
        } else {
            ""
        } + outputFormatter.format(startTime) + if (endTime != null) {
            val endCal = Calendar.getInstance()
            endCal.time = endTime

            val day = isDaysAfter(endCal, startCal)
            " - " + if (day != 0 && tripStartTime != null) {
                val tripStartCal = Calendar.getInstance()
                tripStartCal.time = tripStartTime
                String.format(dayString, isDaysAfter(endCal, tripStartCal) + 1)
            } else {
                ""
            } + outputFormatter.format(endTime)
        } else {
            ""
        }
    } else {
        "--"
    }

//    val outputFormatter = SimpleDateFormat(outputPattern)
    /*return if (startTime != null && endTime != null) {
        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        startCal.time = startTime
        endCal.time = endTime

        outputFormatter.format(startTime) + " - " +
                if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
                    && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
                    && startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH)
                ) {
                    val outputFormatterSameDay = SimpleDateFormat(outputPatternSameDay)
                    outputFormatterSameDay.format(endTime)
                } else {
                    outputFormatter.format(endTime)
                }
    } else if (startTime != null) {
        outputFormatter.format(startTime)
    } else {
        "--"
    }*/
}

fun isDaysAfter(cal1: Calendar, cal2: Calendar): Int {
    cal1.set(Calendar.HOUR_OF_DAY, 0)
    cal1.set(Calendar.MINUTE, 0)
    cal1.set(Calendar.SECOND, 0)
    cal1.set(Calendar.MILLISECOND, 0)

    cal2.set(Calendar.HOUR_OF_DAY, 0)
    cal2.set(Calendar.MINUTE, 0)
    cal2.set(Calendar.SECOND, 0)
    cal2.set(Calendar.MILLISECOND, 0)

    return ChronoUnit.DAYS.between(cal2.toInstant(), cal1.toInstant()).toInt()
//    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
//            && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
//            && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

@SuppressLint("SimpleDateFormat")
fun toDateTime(str: String, outputPattern: String): String {
    val formatter = SimpleDateFormat(outputPattern)
    return try {
        formatter.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str)!!)
    } catch (e: ParseException) {
        "--"
    }
}

// For showing shadow on the top edge of bottom bar
fun Modifier.advancedShadow(
    color: Color = Color.Black,
    alpha: Float = 1f,
    cornersRadius: Dp = 0.dp,
    shadowBlurRadius: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = color.copy(alpha = 0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowBlurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            cornersRadius.toPx(),
            cornersRadius.toPx(),
            paint
        )
    }
}

fun Modifier.crop(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    fun Dp.toPxInt(): Int = this.toPx().toInt()

    layout(
        placeable.width - (horizontal * 2).toPxInt(),
        placeable.height - (vertical * 2).toPxInt()
    ) {
        placeable.placeRelative(-horizontal.toPx().toInt(), -vertical.toPx().toInt())
    }
}
