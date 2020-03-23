package wagner.jasper.paint.model

import android.graphics.Paint
import wagner.jasper.paint.ui.CustomCanvasView

data class MyPaint(var isEraseOn: Boolean = false, val backgroundColor: Int, val drawColor: Int): Paint() {


    val paint = Paint().apply {
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Join.ROUND //default MITER
        strokeCap = Cap.ROUND  //default: BUTT

        // adjustable settings
        alpha = 255
        color = if (isEraseOn) backgroundColor else drawColor
        strokeWidth = CustomCanvasView.STROKE_WIDTH
    }
}