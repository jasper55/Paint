package wagner.jasper.paint.model

import android.graphics.Color
import android.graphics.Paint
import wagner.jasper.paint.ui.CustomCanvasView

data class MyPaint(var isEraseOn: Boolean = false, val backgroundColor: Int = Color.WHITE, val drawColor: Int = Color.BLACK): Paint() {


    val paint = Paint().apply {
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Paint.Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Paint.Join.ROUND //default MITER
        strokeCap = Paint.Cap.ROUND  //default: BUTT


        // adjustable settings
        alpha = 255
        if (isEraseOn) {
            color = backgroundColor
        } else {
            color = drawColor
        }
        strokeWidth = CustomCanvasView.STROKE_WIDTH
    }
}