package yong.jianwen.heatmap

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import yong.jianwen.heatmap.data.TripMode
import yong.jianwen.heatmap.data.entity.Car
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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
fun formatDisplayStartEndTimes(
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
