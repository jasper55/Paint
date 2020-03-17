package wagner.jasper.paint.ui

import android.app.Application
import android.graphics.Path
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val _pathList = MutableLiveData<ArrayList<Path>>()
    val pathList: LiveData<ArrayList<Path>>
        get() = _pathList

    private val _currentPath = MutableLiveData<Path>()
    val currentPath: LiveData<Path>
        get() = _currentPath

    private var motionTouchEventX = 0F
    private var motionTouchEventY = 0F

    // to cache latest x and y values
    private var currentX = 0F
    private var currentY = 0F


    private val _path = MutableLiveData<Path>()
    val path: LiveData<Path>
        get() = _path

    init {
        _path.value = Path()
        _currentPath.value = Path()
        _pathList.value = ArrayList()
    }



    // after the user has stopped moving and touches the screen again
    fun touchStart() {
        _currentPath.value!!.reset()
        _currentPath.value!!.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        Log.i("SharedViewModel","touchStart()")

    }
    fun touchMove() {
        _currentPath.value!!.quadTo(
            currentX,
            currentY,
            (motionTouchEventX + currentX) / 2,
            (motionTouchEventY + currentY) / 2
        )
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        Log.i("SharedViewModel","touchMove()")
    }

    fun touchUp() {
        _pathList.value!!.add(currentPath.value!!)
        _path.value?.let {
            it.addPath(_currentPath.value!!)
        }
        _currentPath.value!!.reset()
        Log.i("SharedViewModel","touchUp()")
    }





    fun updateXY(x: Float, y: Float) {
        motionTouchEventX = x
        motionTouchEventY = y
    }

    fun isTouchEventWithinTolerance(touchTolerance: Int): Boolean {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        return dx >= touchTolerance || dy >= touchTolerance
    }


    fun deleteLastPath() {
        val index = _pathList.value!!.size
        _pathList.value!!.removeAt(index)
    }

    fun deleteSelectedPath(path: Path) {
        _pathList.value!!.remove(path)
    }

    fun cleanPath() {
        cleanPath()
    }
}