package wagner.jasper.paint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import wagner.jasper.paint.UI.CustomCanvasView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main) will be replaced with custom canvas
        val paintCanvas = CustomCanvasView(this)
    }
}
