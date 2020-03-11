package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlin.math.min


//Use this class only if you want to slide up the FAB whenever the seekbar appears.
class MoveUpwardBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View?>(context, attrs) {
//    constructor() : super() {}
//    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is SnackbarLayout
    }


    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val translationY =
            min(0F, ViewCompat.getTranslationY(dependency) - dependency.height)
        ViewCompat.setTranslationY(child, translationY)
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        ViewCompat.animate(child).translationY(0f).start()
    }


}