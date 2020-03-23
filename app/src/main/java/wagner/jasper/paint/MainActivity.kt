package wagner.jasper.paint

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import wagner.jasper.paint.ui.CustomCanvasView
import wagner.jasper.paint.ui.SharedViewModel
import android.content.DialogInterface
import android.graphics.Color
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder




class MainActivity : AppCompatActivity() {

    private var isFABOpen = false
    private lateinit var fab_clear: FloatingActionButton
    private lateinit var fab_erase: FloatingActionButton
    private lateinit var fab_undo: FloatingActionButton
    private lateinit var fab_redo: FloatingActionButton
    private lateinit var fab_menu: FloatingActionButton

    private lateinit var fab_container_clear: LinearLayout
    private lateinit var fab_container_erase: LinearLayout
    private lateinit var fab_container_undo: LinearLayout
    private lateinit var fab_container_redo: LinearLayout
    private lateinit var fabOverlay: View

    private lateinit var canvas: CustomCanvasView
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        setContentView(R.layout.activity_main)

        // link to inflated view in xml
        canvas = custom_canvas_view
        instantiateFABMenu()
    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        observePathChanges()
        return super.onCreateView(name, context, attrs)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        sharedViewModel.undo()
    }

    private fun observePathChanges() {
        sharedViewModel.path.observe(this, Observer {
            Log.i("SharedViewModel", "observePathChanges()")
        })
    }


    private fun instantiateFABMenu() {
        fab_container_clear = findViewById(R.id.fab_container_clear)
        fab_container_erase = findViewById(R.id.fab_container_erase)
        fab_container_undo = findViewById(R.id.fab_container_undo)
        fab_container_redo = findViewById(R.id.fab_container_redo)
        fabOverlay = findViewById(R.id.fabOverlay)
        fab_menu = findViewById(R.id.fab_menu)
        fab_menu.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }
        fabOverlay.setOnClickListener {
            closeFABMenu()
        }

        fab_clear = findViewById(R.id.fab_clear)
        fab_clear.setOnClickListener {
//            sharedViewModel.clear()
            closeFABMenu()
            showDrawColorPicker()
//            canvas.invalidate()
        }
        fab_erase = findViewById(R.id.fab_erase)
        fab_erase.setOnClickListener {
            sharedViewModel.toggleErase()
            closeFABMenu()
        }
        fab_undo = findViewById(R.id.fab_undo)
        fab_undo.setOnClickListener {
            sharedViewModel.undo()
            closeFABMenu()
            canvas.invalidate()
        }

        fab_redo = findViewById(R.id.fab_redo)
        fab_redo.setOnClickListener {
            sharedViewModel.redo()
            closeFABMenu()
            canvas.invalidate()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showFABMenu() {
        isFABOpen = true
        //applyBlurOnBackground();
        fab_container_clear.visibility = CoordinatorLayout.VISIBLE
        fab_container_undo.visibility = CoordinatorLayout.VISIBLE
        fab_container_erase.visibility = CoordinatorLayout.VISIBLE
        fab_container_redo.visibility = CoordinatorLayout.VISIBLE
        fabOverlay.visibility = CoordinatorLayout.VISIBLE
        fab_menu.animate().rotationBy(270F)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
        fab_container_redo.animate()
            .translationY(-resources.getDimension(R.dimen.standard_230))
        fab_container_clear.animate()
            .translationY(-resources.getDimension(R.dimen.standard_175))
        fab_container_undo.animate()
            .translationY(-resources.getDimension(R.dimen.standard_120))
        fab_container_erase.animate()
            .translationY(-resources.getDimension(R.dimen.standard_65))
    }

    @SuppressLint("RestrictedApi")
    private fun closeFABMenu() {
        //        removeBlurOnBackground();
        isFABOpen = false
        fabOverlay.visibility = CoordinatorLayout.GONE
        fab_container_clear.animate().translationY(0F)
        fab_container_undo.animate().translationY(0F)
        fab_container_erase.animate().translationY(0F)
        fab_container_redo.animate().translationY(0F)
        fab_menu.animate().rotationBy(-270F)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    if (!isFABOpen) {
                        fab_container_clear.visibility = CoordinatorLayout.GONE
                        fab_container_undo.visibility = CoordinatorLayout.GONE
                        fab_container_erase.visibility = CoordinatorLayout.GONE
                        fab_container_redo.visibility = CoordinatorLayout.GONE
                    }
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
    }

    private fun showDrawColorPicker(){
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .initialColor(Color.BLACK)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setOnColorSelectedListener(object : OnColorSelectedListener {
                override fun onColorSelected(selectedColor: Int) {
                    sharedViewModel.setDrawColor(selectedColor)
                }
            })
            .setPositiveButton("ok", object : ColorPickerClickListener {
                override fun onClick(dialog: DialogInterface, selectedColor: Int, allColors: Array<Int>) {
//                    sharedViewModel.setBackgroundColor(selectedColor)
                }
            })
            .setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which -> })
            .build()
            .show()
    }

}
