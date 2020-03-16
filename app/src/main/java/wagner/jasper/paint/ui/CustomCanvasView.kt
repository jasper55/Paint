package wagner.jasper.paint.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.*
import wagner.jasper.paint.R
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector


class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs), LifecycleObserver,
    ViewModelAccessor by ViewModelInjector(context) {

    lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.paintColor, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.background, null)

    val paintStyle = Paint().apply {
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

    init {
        (getContext() as LifecycleOwner).lifecycle.addObserver(this)
        initCanvas()
//        subscribe()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        subscribe()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(){
        lifecycle
    }

    // TODO subscription not working yet
    private fun subscribe() {
        Log.i("CCV", "${sharedViewModel.path.value}")
        Log.i("CCV", activity.toString())
//        sharedViewModel.path.observeForever {
//            it?.let {
//                extraCanvas.drawPath(it, paintStyle)
//                invalidate()
//            }
//        }
        sharedViewModel.path.observe((context as LifecycleOwner).lifecycle., Observer {
            it?.let {
                extraCanvas.drawPath(it, paintStyle)
                invalidate()
            }
        })
    }


    // the drawing will be interpolated and not drawn for every pixel
    // the sensibility of the distance between two points is set here
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop


    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
//        if (::extraBitmap.isInitialized) extraBitmap.recycle()
//        //onSizeChanged creates always a new bitmap when size is changed, this would cause a memory leak,
//        //  because the old bitmaps would still left around
//        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        extraCanvas = Canvas(extraBitmap)
//        extraCanvas.drawColor(backgroundColor)
//
//        subscribe()
        //restorePath()
    }

    // TODO restoring path after orientation changes not working yet
    private fun restorePath() {
        sharedViewModel.path.value?.let {
            extraCanvas.drawPath(it, paintStyle)
            invalidate()
        }
    }

    private fun initCanvas() {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
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





    private fun touchMove() {
        if (sharedViewModel.isTouchEventWithinTolerance(touchTolerance)) {
            sharedViewModel.touchMove()
            // TODO replace it with observe
//            extraCanvas.drawPath(sharedViewModel.path.value!!, paintStyle)
        }
        // forces to redraw the on the screen with the updated path
        invalidate()
    }




    companion object {
        const val STROKE_WIDTH = 12f


    }


}