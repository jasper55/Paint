package wagner.jasper.paint

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import wagner.jasper.paint.ui.CustomCanvasView
import wagner.jasper.paint.ui.FabMenu
import wagner.jasper.paint.ui.SharedViewModel


class MainActivity : AppCompatActivity() {

    private var isFABOpen = false
    private var fab_clear: FloatingActionButton? = null
    private var fab_erase: FloatingActionButton? = null
    private var fab_delete: FloatingActionButton? = null
    private var fab_menu: FloatingActionButton? = null
    private lateinit var canvas: CustomCanvasView
    private lateinit var sharedViewModel: SharedViewModel

    var fab_container_clear: LinearLayout? = null
    var fab_container_erase: LinearLayout? = null
    var fab_container_delete: LinearLayout? = null
    var fabOverlay: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
//        val paintCanvas = CustomCanvasView(this)

        // set canvas to full screen
//        paintCanvas.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
//        paintCanvas.contentDescription = getString(R.string.paintCanvasDescription)
//        setContentView(paintCanvas)
//        fabMenu.instantiateFABMenu(this)
//        window.addContentView(fabMenu,)
        instantiateFABMenu()
        instantiateCanvas()
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }


    private fun instantiateCanvas(){
        canvas = findViewById(R.id.custom_canvas_view)
        subscribe()
    }

    private fun subscribe() {
//        Log.i("CCV", "${sharedViewModel.path.value}")
//        Log.i("CCV", activity.toString())
        sharedViewModel.path.observe(this, Observer {
            it?.let {
                canvas.extraCanvas.drawPath(it, canvas.paintStyle)
                canvas.invalidate()
            }
        })
    }

    private fun instantiateFABMenu() {
        fab_container_clear = findViewById(R.id.fab_container_clear)
        fab_container_erase = findViewById(R.id.fab_container_erase)
        fab_container_delete = findViewById(R.id.fab_container_undo)
        fabOverlay = findViewById(R.id.fabOverlay)
        fab_menu = findViewById(R.id.fab_menu)
        fab_menu!!.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }
        fabOverlay!!.setOnClickListener {
            closeFABMenu()
        }

        fab_clear = findViewById(R.id.fab_clear)
        fab_clear!!.setOnClickListener {
            //                closeFABMenu()
        }
        fab_erase = findViewById(R.id.fab_erase)
        fab_erase!!.setOnClickListener {
            //closeFABMenu()
        }
        fab_delete = findViewById(R.id.fab_undo)
        fab_delete!!.setOnClickListener {
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showFABMenu() {
        isFABOpen = true
        //applyBlurOnBackground();
        fab_container_clear!!.visibility = CoordinatorLayout.VISIBLE
        fab_container_delete!!.visibility = CoordinatorLayout.VISIBLE
        fab_container_erase!!.visibility = CoordinatorLayout.VISIBLE
        fabOverlay!!.visibility = CoordinatorLayout.VISIBLE
        fab_menu!!.animate().rotationBy(270F).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        fab_container_clear!!.animate().translationY(-resources.getDimension(R.dimen.standard_175))
        fab_container_delete!!.animate()
            .translationY(-resources.getDimension(R.dimen.standard_120))
        fab_container_erase!!.animate()
            .translationY(-resources.getDimension(R.dimen.standard_65))
    }

    @SuppressLint("RestrictedApi")
    private fun closeFABMenu() {
        //        removeBlurOnBackground();
        isFABOpen = false
        fabOverlay!!.visibility = CoordinatorLayout.GONE
        fab_container_clear!!.animate().translationY(0F)
        fab_container_delete!!.animate().translationY(0F)
        fab_container_erase!!.animate().translationY(0F)
        fab_menu!!.animate().rotationBy(-270F).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                if (!isFABOpen) {
                    fab_container_clear!!.visibility = CoordinatorLayout.GONE
                    fab_container_delete!!.visibility = CoordinatorLayout.GONE
                    fab_container_erase!!.visibility = CoordinatorLayout.GONE
                }
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

}
