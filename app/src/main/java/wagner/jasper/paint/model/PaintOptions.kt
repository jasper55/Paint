package wagner.jasper.paint.model

import android.graphics.Color

data class PaintOptions(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 12f,
    var alpha: Int = 255,
    var isErasOn: Boolean = false
)
