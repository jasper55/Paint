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
import android.graphics.drawable.Drawable
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import android.widget.SeekBar
import android.os.Build
import android.view.MenuItem
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import wagner.jasper.paint.ui.CircleView


class MainActivity : AppCompatActivity() {

    private var isFABOpen = false
    private lateinit var fab_clear: FloatingActionButton
    private lateinit var fab_save: FloatingActionButton
    private lateinit var fab_share: FloatingActionButton
    private lateinit var fab_menu: FloatingActionButton

    private lateinit var fab_container_clear: LinearLayout
    private lateinit var fab_container_save: LinearLayout
    private lateinit var fab_container_share: LinearLayout
    private lateinit var fabOverlay: View

    private lateinit var canvas: CustomCanvasView
    private lateinit var toolbar: ActionBar
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
        initBottomNavigation()
        instantiateFABMenu()
        initColorPicker()
        initStrokeSeekbars()
        initCircleView()
        initApplyButton()
    }

    private fun initBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        val navigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_erase -> {

                        sharedViewModel.toggleErase()
                        if (sharedViewModel.currentPaint.value!!.isEraseOn) {
//                            item.setIcon(setActivtedTint(this, R.drawable.ic_erase_black))
                            tintMenuIcon(this,item,R.color.darkGrey)
                        } else {
                            item.icon = setInactiveTint(this, R.drawable.ic_erase_black)
                        }
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_brush -> {
                        showDrawColorPicker()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_undo -> {
                        sharedViewModel.undo()
                        canvas.invalidate()
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_redo -> {
                        sharedViewModel.redo()
                        canvas.invalidate()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_circle -> {
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
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
        fab_container_save = findViewById(R.id.fab_container_save)
        fab_container_share = findViewById(R.id.fab_container_share)
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
            closeFABMenu()
        }
        fab_save = findViewById(R.id.fab_save)
        fab_save.setOnClickListener {
            closeFABMenu()

        }

        fab_share = findViewById(R.id.fab_share)
        fab_share.setOnClickListener {
            closeFABMenu()
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
        fab_container_save.visibility = CoordinatorLayout.VISIBLE
        fab_container_share.visibility = CoordinatorLayout.VISIBLE
        fabOverlay.visibility = CoordinatorLayout.VISIBLE
        fab_menu.animate().rotationBy(270F)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
//        fab_container_share.animate()
//            .translationY(-resources.getDimension(R.dimen.standard_230))
        fab_container_share.animate()
            .translationY(-resources.getDimension(R.dimen.standard_175))
        fab_container_save.animate()
            .translationY(-resources.getDimension(R.dimen.standard_120))
        fab_container_clear.animate()
            .translationY(-resources.getDimension(R.dimen.standard_65))
    }

    @SuppressLint("RestrictedApi")
    private fun closeFABMenu() {
        //        removeBlurOnBackground();
        isFABOpen = false
        fabOverlay.visibility = CoordinatorLayout.GONE
        fab_container_clear.animate().translationY(0F)
        fab_container_save.animate().translationY(0F)
        fab_container_share.animate().translationY(0F)
        fab_menu.animate().rotationBy(-270F)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    if (!isFABOpen) {
                        fab_container_clear.visibility = CoordinatorLayout.GONE
                        fab_container_save.visibility = CoordinatorLayout.GONE
                        fab_container_share.visibility = CoordinatorLayout.GONE
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

    private fun showApplyButton() {
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

    private fun setActivtedTint(context: Context, iconSource: Int): Drawable {
        val unwrappedDrawable: Drawable = AppCompatResources.getDrawable(context, iconSource)!!
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, resources.getColor(R.color.darkGrey))
        return wrappedDrawable
    }

    private fun setInactiveTint(context: Context, iconSource: Int): Drawable {
        val unwrappedDrawable: Drawable = AppCompatResources.getDrawable(context, iconSource)!!
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, resources.getColor(R.color.grey))
        return wrappedDrawable
    }

    fun tintMenuIcon(context: Context, item: MenuItem, @ColorRes color: Int) {
        val normalDrawable = item.getIcon()
        val wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));

        item.setIcon(wrapDrawable);
    }
}
