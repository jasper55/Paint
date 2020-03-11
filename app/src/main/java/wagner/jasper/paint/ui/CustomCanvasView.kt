package wagner.jasper.paint.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import wagner.jasper.paint.R
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector

class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.paintColor, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.background, null)

    private val paintStyle = Paint().apply {
        color = drawColor
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Paint.Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Paint.Join.ROUND //default MITER
        strokeCap = Paint.Cap.ROUND  //default: BUTT
        strokeWidth = STROKE_WIDTH
    }

//    init {
//
//    }

    private fun subscribe() {
//        sharedViewModel.pathList.observe(activity, Observer {
//        })
    }





    // the drawing will be interpolated and not drawn for every pixel
    // the sensibiliy of the distance between two points is set here
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop


    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        //onSizeChanged creates always a new bitmap when size is changed, this would cause a memory leak,
        //  because the old bitmaps would still left around
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        subscribe()
        sharedViewModel.path.value?.let {
            extraCanvas.drawPath(it, paintStyle)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        sharedViewModel.updateXY(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> sharedViewModel.touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> sharedViewModel.touchUp()
        }
        return true
    }


    // after the user has stopped moving and touches the screen again


    private fun touchMove() {
        if (sharedViewModel.isTouchEventWithinTolerance(touchTolerance)) {
            sharedViewModel.touchMove()
            extraCanvas.drawPath(sharedViewModel.path.value!!, paintStyle)
        }
        // forces to redraw the on the screen with the updated path
        invalidate()
    }


    companion object {
        const val STROKE_WIDTH = 12f


    }


}