package wagner.jasper.paint.model

import android.graphics.Color
import android.graphics.Paint
import wagner.jasper.paint.ui.CustomCanvasView

object MyPaint {

    val paintStyle = Paint().apply {
        color = Color.BLACK
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Paint.Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Paint.Join.ROUND //default MITER
        strokeCap = Paint.Cap.ROUND  //default: BUTT
        strokeWidth = CustomCanvasView.STROKE_WIDTH
    }
}