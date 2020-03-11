package wagner.jasper.paint.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import wagner.jasper.paint.R


class FabMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoordinatorLayout(context, attrs) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.fab_menu, this, true)
    }


}