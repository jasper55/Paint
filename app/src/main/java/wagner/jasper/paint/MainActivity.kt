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
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import android.widget.SeekBar
import android.os.Build
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import wagner.jasper.paint.ui.CircleView
import wagner.jasper.paint.util.BlurBuilder
import wagner.jasper.paint.util.BounceInterpolator

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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
    private lateinit var blurredImageOverlay: ImageView

    private lateinit var canvas: CustomCanvasView
    private lateinit var linearLayoutAlpha: LinearLayout
    private lateinit var linearLayoutStroke: LinearLayout
    private lateinit var colorPicker: AlertDialog
    private lateinit var circleView: CircleView
    private lateinit var undoIcon: ImageView
    private lateinit var redoIcon: ImageView
    private lateinit var eraseIcon: ImageView
    private lateinit var colorPaletteIcon: ImageView
    private lateinit var strokeWidthSeekbar: SeekBar
    private lateinit var colorAlphaSeekbar: SeekBar
    private lateinit var applyButton: Button
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottom_slider: LinearLayout
    private lateinit var bottom_tools_bar: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        setContentView(R.layout.activity_main)

        canvas = custom_canvas_view
        initBottomToolBar()
        instantiateFABMenu()
        initColorPicker()
        initStrokeSeekbars()
        initApplyButton()
        initBounceAnimator()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        observePathChanges()
        return super.onCreateView(name, context, attrs)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) hideSystemUI()
    }

    private fun initBottomToolBar() {

        bottom_slider = findViewById(R.id.bottom_slider)
        bottom_tools_bar = findViewById(R.id.bottom_tools_bar)
        bottom_tools_bar.visibility = View.GONE
        sheetBehavior = BottomSheetBehavior.from(bottom_slider)

        // click event for show-dismiss bottom sheet
        expandable_icon.setOnClickListener {

            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottom_tools_bar.visibility = View.VISIBLE
                        collapsable_icon_down.visibility = View.VISIBLE
                        expandable_icon.visibility = View.GONE
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottom_tools_bar.visibility = View.GONE
                        collapsable_icon_down.visibility = View.GONE
                        expandable_icon.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


        eraseIcon = findViewById(R.id.navigation_erase)
        colorPaletteIcon = findViewById(R.id.navigation_colors)
        undoIcon = findViewById(R.id.navigation_undo)
        redoIcon = findViewById(R.id.navigation_redo)
        circleView = findViewById(R.id.circle_view)

        eraseIcon.setOnClickListener {
            sharedViewModel.toggleErase()
        }

        colorPaletteIcon.setOnClickListener {
            showDrawColorPicker()
        }

        undoIcon.setOnClickListener {
            sharedViewModel.undo()
            canvas.invalidate()
        }

        redoIcon.setOnClickListener {
            sharedViewModel.redo()
            canvas.invalidate()
        }
    }

    private fun observePathChanges() {
        sharedViewModel.path.observe(this, Observer {
            Log.i("SharedViewModel", "observePathChanges()")
            tintLight(undoIcon)
            tintLight(redoIcon)
            sharedViewModel.apply {
                if (path.value!!.keys.isNotEmpty()) {
                    tintDark(undoIcon)
                }
                if (undoPathList.value!!.keys.isNotEmpty()) {
                    tintDark(redoIcon)
                }
            }
        })
        sharedViewModel.drawColor.observe(this, Observer {
            circleView.setColor(it)
            strokeWidthSeekbar.progressDrawable.setColorFilter(it, PorterDuff.Mode.SRC_IN)
            strokeWidthSeekbar.thumb.setColorFilter(it, PorterDuff.Mode.SRC_IN)

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

        sharedViewModel.isEraseOn.observe(this, Observer {
            Log.i("SharedViewModel", "isEraseOn")

            if (it) {
                tintDark(eraseIcon)
                tintLight(colorPaletteIcon)
            } else {
                tintLight(eraseIcon)
                tintDark(colorPaletteIcon)
            }
        })
    }

    private fun instantiateFABMenu() {
        fab_container_clear = findViewById(R.id.fab_container_clear)
        fab_container_save = findViewById(R.id.fab_container_save)
        fab_container_share = findViewById(R.id.fab_container_share)
        blurredImageOverlay = findViewById(R.id.fabOverlay)
        fab_menu = findViewById(R.id.fab_menu)
        fab_menu.setOnClickListener {
            fab_menu.startAnimation(bounceAnimation)
            if (!isFABOpen) {
                fab_clear.startAnimation(bounceAnimation)
                fab_save.startAnimation(bounceAnimation)
                fab_share.startAnimation(bounceAnimation)
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }
        blurredImageOverlay.setOnClickListener {
            closeFABMenu()
        }

        fab_clear = findViewById(R.id.fab_clear)
        fab_clear.setOnClickListener {
            sharedViewModel.clear()
            canvas.initCanvas()
            canvas.invalidate()
            closeFABMenu()
        }
        fab_save = findViewById(R.id.fab_save)
        fab_save.setOnClickListener {
            closeFABMenu()
        }

        fab_share = findViewById(R.id.fab_share)
        fab_share.setOnClickListener {
            shareImage()
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

    private fun showFABMenu() {
        isFABOpen = true
        showBlurredOverlay()
        fab_container_clear.visibility = CoordinatorLayout.VISIBLE
        fab_container_save.visibility = CoordinatorLayout.VISIBLE
        fab_container_share.visibility = CoordinatorLayout.VISIBLE
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
        isFABOpen = false
        blurredImageOverlay.visibility = ImageView.GONE
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
                        showStrokeWidthSeekbar()
                        showApplyButton()
                    }
                })
                .setNegativeButton("cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        applyPaintChanges()
                        return
                    }
                })
                .build()
    }

    private fun initStrokeSeekbars() {
        linearLayoutAlpha = findViewById(R.id.color_alpha_seek_bar_container)
        linearLayoutStroke = findViewById(R.id.stroke_width_seek_bar_container)
        colorAlphaSeekbar = findViewById(R.id.color_alpha_seek_bar)
        colorAlphaSeekbar.progress = 100
        strokeWidthSeekbar = findViewById(R.id.stroke_width_seek_bar)

        colorAlphaSeekbar.progressDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        colorAlphaSeekbar.thumb.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
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
        applyButton.visibility = View.GONE
        canvas.visibility = View.VISIBLE
    }

    private fun tintDark(icon: ImageView) {
        DrawableCompat.setTint(
            icon.drawable,
            ContextCompat.getColor(applicationContext, R.color.darkGrey)
        )
    }

    private fun tintLight(icon: ImageView) {
        DrawableCompat.setTint(
            icon.drawable,
            ContextCompat.getColor(applicationContext, R.color.grey)
        )
    }

    private fun showBlurredOverlay() {
        val oldCanvas = canvas.getCanvas()
        val view = CustomCanvasView(this, canvas.attributeSet)
        val imageData = Bitmap.createBitmap(canvas.getBitmap())
        val blurredBitmap = BlurBuilder.blur(this, imageData)
        val blurredDrawable = BitmapDrawable(blurredBitmap)
        blurredImageOverlay.setImageDrawable(blurredDrawable)
        blurredImageOverlay.invalidate()
        blurredImageOverlay.visibility = ImageView.VISIBLE
        view.draw(oldCanvas)
    }

    private fun shareImage(): Boolean {
        val imageData = Bitmap.createBitmap(canvas.getBitmap())
        val uri = sharedViewModel.getImageUri(this, imageData)
        val sharingIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
//        val filename = "Paint_drawing"
//        val sdIconStorageDir = File(
//            Environment.getExternalStorageDirectory()
//                .absolutePath + "/Paint/"
//        )
//        // create storage directories, if they don't exist
//        if (!sdIconStorageDir.exists()) {
//            sdIconStorageDir.mkdirs()
//        }
//        try {
//            val filePath = sdIconStorageDir.toString() + File.separator + filename
//            val fileOutputStream = FileOutputStream(filePath)
//            val bos = BufferedOutputStream(fileOutputStream)
//
//
//            Toast.makeText(this, "Image Saved at----$filePath", Toast.LENGTH_LONG).show()
//            // choose another format if PNG doesn't suit you
//            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos)
//            bos.flush()
//            bos.close()
//
//        } catch (e: FileNotFoundException) {
//            Log.w("TAG", "Error saving image file: " + e.message)
//            return false;
//        } catch (e: IOException) {
//            Log.w("TAG", "Error saving image file: " + e.message)
//            return false
//        }
        return true
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


}

