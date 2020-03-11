package wagner.jasper.paint.util

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import wagner.jasper.paint.MainActivity
import wagner.jasper.paint.ui.SharedViewModel

interface ViewModelAccessor {
    var sharedViewModel: SharedViewModel
    val activity: MainActivity
}

class ViewModelInjector(val context: Context) : ViewModelAccessor {
    override val activity: MainActivity by lazy {
        try {
            context as MainActivity
        } catch (exception: ClassCastException) {
            throw ClassCastException("Please ensure that the provided Context is a valid FragmentActivity")
        }
    }
    override var sharedViewModel = ViewModelProviders.of(activity).get(SharedViewModel::class.java)
}