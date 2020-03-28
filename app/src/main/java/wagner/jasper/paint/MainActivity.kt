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
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import android.widget.SeekBar
import android.os.Build
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import wagner.jasper.paint.ui.CircleView
import wagner.jasper.paint.util.BounceInterpolator


class MainActivity : AppCompatActivity() {

    private var isFABOpen = false
    private lateinit var fab_clear: FloatingActionButton
    private lateinit var fab_save: FloatingActionButton
    private lateinit var fab_share: FloatingActionButton
    private lateinit var fab_menu: FloatingActionButton

    private lateinit var bounceAnimation: Animation

    private lateinit var fab_container_clear: LinearLayout
    private lateinit var fab_container_save: LinearLayout
    private lateinit var fab_container_share: LinearLayout
    private lateinit var fabOverlay: View

    private lateinit var canvas: CustomCanvasView
    private lateinit var linearLayoutAlpha: LinearLayout
    private lateinit var linearLayoutStroke: LinearLayout
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
        initCircleView()
        initBottomNavigation()
        instantiateFABMenu()
        initColorPicker()
        initStrokeSeekbars()
        initApplyButton()
        initBounceAnimator()
    }

    private fun initBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        val menuItem = bottomNavigation.menu.getItem(4)
        val eraseItem = bottomNavigation.menu.getItem(0)
        menuItem.actionView = circleView
        val navigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_erase -> {
                        sharedViewModel.toggleErase()
                        if (sharedViewModel.currentPaint.value!!.isEraseOn) {
                            tintMenuIcon(eraseItem, R.color.darkGrey)
                        } else {
                            tintMenuIcon(eraseItem, R.color.grey)
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
                fab_menu.startAnimation(bounceAnimation)
                fab_clear.startAnimation(bounceAnimation)
                fab_save.startAnimation(bounceAnimation)
                fab_share.startAnimation(bounceAnimation)

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
            sharedViewModel.clear()
            canvas.invalidate()
            closeFABMenu()
        }
        fab_save = findViewById(R.id.fab_save)
        fab_save.setOnClickListener {
            closeFABMenu()

        }

        fab_share = findViewById(R.id.fab_share)
        fab_share.setOnClickListener {
            shareOnInstagram()
            closeFABMenu()
        }
    }

    private fun initBounceAnimator(): Animation {
        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val interpolator = BounceInterpolator(0.2, 20.0)
        bounceAnimation.interpolator = interpolator
        return bounceAnimation
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
        fab_menu.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
        fab_menu.animate().rotationBy(270F)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
        fab_container_share.animate()
            .translationY(-resources.getDimension(R.dimen.standard_180))
        fab_container_save.animate()
            .translationY(-resources.getDimension(R.dimen.standard_125))
        fab_container_clear.animate()
            .translationY(-resources.getDimension(R.dimen.standard_70))
    }

    @SuppressLint("RestrictedApi")
    private fun closeFABMenu() {
        //        removeBlurOnBackground();
        isFABOpen = false
        fabOverlay.visibility = CoordinatorLayout.GONE
        fab_container_clear.animate().translationY(0F)
        fab_container_save.animate().translationY(0F)
        fab_container_share.animate().translationY(0F)
        fab_menu.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.grey))
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
                .setTitle("Choose a color")
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
        linearLayoutAlpha = findViewById(R.id.color_alpha_seek_bar_container)
        linearLayoutStroke = findViewById(R.id.stroke_width_seek_bar_container)
        colorAlphaSeekbar = findViewById(R.id.color_alpha_seek_bar)
        colorAlphaSeekbar.progress = 100
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
        linearLayoutAlpha.visibility = View.VISIBLE
        linearLayoutStroke.visibility = View.VISIBLE
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
        linearLayoutStroke.visibility = View.GONE
        linearLayoutAlpha.visibility = View.GONE
        colorAlphaSeekbar.visibility = View.GONE
//        circleView.visibility = View.GONE
        applyButton.visibility = View.GONE
        canvas.visibility = View.VISIBLE
    }


    private fun tintMenuIcon(item: MenuItem, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            item.icon.setTintList(ColorStateList.valueOf(resources.getColor(color)))
            item.icon.setTint(resources.getColor(color))
        }
    }


    private fun shareOnInstagram() {
        val bitmap = canvas.getBitmap()
        val uri = sharedViewModel.getImageUri(this, bitmap)
        val sharingIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }


}

