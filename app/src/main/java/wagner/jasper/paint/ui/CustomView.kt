package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.graphics.Bitmap


class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs)
{

    private lateinit var extraBitmap: Bitmap

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    fun updateBitmap(newBitmap: Bitmap) {
        extraBitmap = newBitmap
    }
}
