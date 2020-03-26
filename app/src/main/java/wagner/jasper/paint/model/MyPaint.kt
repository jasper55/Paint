package wagner.jasper.paint.model

import android.graphics.Paint

data class MyPaint(
    val isEraseOn: Boolean = false,
    val backgroundColor: Int,
    val drawColor: Int,
    val alphaSet: Int,
    val strokeWidthSet: Float
): Paint() {

    val paint = Paint().apply {
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Join.ROUND //default MITER
        strokeCap = Cap.ROUND  //default: BUTT

        // adjustable settings
        alpha = alphaSet
        color = if (isEraseOn) backgroundColor else drawColor
        strokeWidth = strokeWidthSet
    }
}