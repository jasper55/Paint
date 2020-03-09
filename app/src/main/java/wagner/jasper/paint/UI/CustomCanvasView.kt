package wagner.jasper.paint.UI

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.content.res.ResourcesCompat
import wagner.jasper.paint.R

class CustomCanvasView(context: Context): View(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.paintColor,null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.background,null)

    private val paint = Paint().apply {
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

    // the pa the user is drawing
    private var path = Path()

    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        //onSizeChanged creates always a new bitmap when size is changed, this would cause a memory leak,
        //  because the old bitmaps would still left around
        extraBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap,0f, 0f,null)
    }

    companion object {
        const val STROKE_WIDTH = 12f



    }
    
    
}