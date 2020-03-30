package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.graphics.Bitmap
import android.util.DisplayMetrics
import wagner.jasper.paint.MainActivity


class CustomView @JvmOverloads constructor(
    activity: MainActivity,
    context: Context,
    attrs: AttributeSet
) : View(context, attrs)
{

    private var extraBitmap: Bitmap

    init {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    fun updateBitmap(newBitmap: Bitmap) {
        extraBitmap = newBitmap
    }
}
