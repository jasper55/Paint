package wagner.jasper.paint.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import wagner.jasper.paint.R
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector
import kotlin.math.abs

class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {


    init {
        subscribe()
    }

    private fun subscribe() {
        sharedViewModel.property.observe(activity, Observer {
        })
    }


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

    // the path the user is drawing
    private var path = Path()
    var pathList = ArrayList<Path>()

    private var motionTouchEventX = 0F
    private var motionTouchEventY = 0F

    // to cache latest x and y values
    private var currentX = 0F
    private var currentY = 0F

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
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }


    // after the user has stopped moving and touches the screen again
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(
                currentX,
                currentY,
                (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            extraCanvas.drawPath(path, paintStyle)
        }
        // forces to redraw the on the screen with the updated path
        invalidate()
    }

    private fun touchUp() {
        savePathToList(path)
        path.reset()
        invalidate()
    }

    private fun savePathToList(path: Path) {
        pathList.add(path)
        invalidate()
    }

    private fun deleteLastPath() {
        val index = pathList.size
        pathList.removeAt(index)
        invalidate()
    }

    private fun deleteSelectedPath(path: Path) {
        pathList.remove(path)
    }

    private fun cleanPath() {
        cleanPath()
    }


    companion object {
        const val STROKE_WIDTH = 12f


    }


}