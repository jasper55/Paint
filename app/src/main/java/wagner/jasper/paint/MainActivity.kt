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
import android.graphics.PorterDuff
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import android.widget.SeekBar
import android.os.Build
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import wagner.jasper.paint.ui.CircleView


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
    private lateinit var colorPicker: AlertDialog
    private lateinit var circleView: CircleView
    private lateinit var strokeWidthSeekbar: SeekBar
    private lateinit var colorAlphaSeekbar: SeekBar
    private lateinit var applyButton: Button
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        setContentView(R.layout.activity_main)

        // link to inflated view in xml -- neccessary for undo/redo
        canvas = custom_canvas_view
        instantiateFABMenu()
        initColorPicker()
        initStrokeSeekbars()
        initCircleView()
        initApplyButton()
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
        sharedViewModel.drawColor.observe(this, Observer {
            circleView.setColor(it)
            strokeWidthSeekbar.progressDrawable.setColorFilter(it, PorterDuff.Mode.SRC_IN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                strokeWidthSeekbar.thumb.setColorFilter(it, PorterDuff.Mode.SRC_IN)
            }

            Log.i("SharedViewModel", "observeDrawColor()")
        })
        sharedViewModel.strokeWidth.observe(this, Observer {
            circleView.setCircleRadius(it)
            Log.i("SharedViewModel", "observeStrokeWidth()")
        })
        sharedViewModel.colorAlpha.observe(this, Observer {
            circleView.setAlpha(it)
            Log.i("SharedViewModel", "observeCircleView()")
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

    private fun initApplyButton() {
        applyButton = apply_button
    }

    private fun showCircleView() {
        circleView.visibility = View.VISIBLE
        Log.i("SharedViewModel", "${circleView.radius}, ${circleView}")
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

    private fun initCircleView() {
        circleView = circle_view
        circleView.invalidate()
    }

    private fun showDrawColorPicker() {
        canvas.visibility = View.GONE
        colorPicker.show()
    }

    private fun initColorPicker() {
        colorPicker =
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(sharedViewModel.currentPaint.value?.drawColor ?: Color.BLUE)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .showAlphaSlider(false)
                .density(12)
                .setOnColorSelectedListener(object : OnColorSelectedListener {
                    override fun onColorSelected(selectedColor: Int) {
                        sharedViewModel.setDrawColor(selectedColor)
                    }
                })
                .setPositiveButton("ok", object : ColorPickerClickListener {
                    override fun onClick(
                        dialog: DialogInterface,
                        selectedColor: Int,
                        allColors: Array<Int>
                    ) {
                        showColorAlphaSeekbar()
                        showCircleView()
                        showStrokeWidthSeekbar()
                        showApplyButton()
                    }
                })
                .setNegativeButton("cancel") { dialog, which -> }
                .build()
    }

    private fun initStrokeSeekbars() {
        colorAlphaSeekbar = findViewById(R.id.color_alpha_seek_bar)
        strokeWidthSeekbar = findViewById(R.id.stroke_width_seek_bar)

        colorAlphaSeekbar.progressDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            colorAlphaSeekbar.thumb.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun showStrokeWidthSeekbar() {
        strokeWidthSeekbar.visibility = View.VISIBLE
        strokeWidthSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, width: Int, b: Boolean) {
                sharedViewModel.setStrokeWidth(width.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun showColorAlphaSeekbar() {
        colorAlphaSeekbar.visibility = View.VISIBLE
        colorAlphaSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, width: Int, b: Boolean) {
                sharedViewModel.setAlpha(width)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun showApplyButton(){
        applyButton.visibility = View.VISIBLE
        applyButton.setOnClickListener {
            applyPaintChanges()
        }
    }

    private fun applyPaintChanges() {
        strokeWidthSeekbar.visibility = View.GONE
        colorAlphaSeekbar.visibility = View.GONE
        circleView.visibility = View.GONE
        applyButton.visibility = View.GONE
        canvas.visibility = View.VISIBLE
    }

}
