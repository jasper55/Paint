package wagner.jasper.paint

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import wagner.jasper.paint.ui.CustomCanvasView
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
    private lateinit var customFABView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        canvas = CustomCanvasView(this, null)
        setContentView(R.layout.activity_main)
        instantiateFABMenu()

        // set canvas to full screen
//        paintCanvas.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
//        paintCanvas.contentDescription = getString(R.string.paintCanvasDescription)
//        setContentView(paintCanvas)
//        fabMenu.instantiateFABMenu(this)
//        window.addContentView(fabMenu,)
//        instantiateCanvas()
    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//        val viewGroup = findViewById<ViewGroup>(R.id.main)
//        canvas = CustomCanvasView(this,attrs)
//        viewGroup.addView(canvas)
//

////        setContentView(viewGroup)
//        return viewGroup

        observePathChanges()

        return super.onCreateView(name, context, attrs)
    }


    override fun onBackPressed() {
        super.onBackPressed()

    }

    private fun observePathChanges() {
        sharedViewModel.path.observe(this, Observer {
            Log.i("SharedViewModel","observePathChanges()" )
            canvas.drawPath(it)
        })
    }


    private fun instantiateCanvas() {
//        canvas = findViewById(R.id.custom_canvas_view)
//        subscribe()
    }


    private fun instantiateFABMenu() {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        customFABView = layoutInflater.inflate(R.layout.fab_menu, null, false)

        customFABView.apply {
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
