package wagner.jasper.paint.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleView(context: Context, attrs: AttributeSet?): View(context, attrs) {
    private var paint = Paint()
    var radius = 80f

    init {
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val cX = width.div(2)
        val cY = height.div(2)

        canvas.drawCircle(cX, cY, radius/2, paint)
    }

    fun setCircleRadius(r: Float){
        radius = r
        invalidate()
    }

    fun setAlpha(newAlpha: Int){
        val alpha = (newAlpha*255)/100
        paint.alpha = alpha
        invalidate()
    }

    fun setColor(color: Int){
        paint.color = color
        invalidate()
    }
}