package wagner.jasper.paint.ui

import android.app.Application
import android.graphics.Path
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private var eraseToggle = false
    private val isEraseOn = MutableLiveData<Boolean>()
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

    private val _undoPathList = MutableLiveData<ArrayList<Path>>()
    val undoPathList: LiveData<ArrayList<Path>>
        get() = _undoPathList

    init {
        isEraseOn.value = false
        _path.value = Path()
        _currentPath.value = Path()
        _pathList.value = ArrayList()
        _undoPathList.value = ArrayList()
    }


    // after the user has stopped moving and touches the screen again
    fun touchStart() {
        _undoPathList.value = ArrayList()
        _currentPath.value!!.reset()
        _currentPath.value!!.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        Log.i("SharedViewModel", "touchStart()")
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
        Log.i("SharedViewModel", "touchMove()")
    }

    fun touchUp() {
        _pathList.value!!.add(currentPath.value!!)
        _path.value?.addPath(_currentPath.value!!)
        _currentPath.value = Path()
        Log.i("SharedViewModel", "touchUp()")
    }

    fun undoDrawLastPath() {
        if (_pathList.value!!.isNotEmpty()) {
            val lastPath = _pathList.value!!.last()
            _undoPathList.value!!.add(lastPath)
            _pathList.value!!.remove(lastPath)
            _path.value!!.reset()

            for (path in _pathList.value!!) {
                _path.value!!.addPath(path)
            }
            _currentPath.value!!.reset()
        } else {
            return
        }
    }

    fun redo() {
        if (_undoPathList.value!!.isNotEmpty()) {

            val pathToRestore = _undoPathList.value!!.last()
            _pathList.value!!.add(pathToRestore)
            _undoPathList.value!!.remove(pathToRestore)
            _path.value!!.reset()

            for (path in _pathList.value!!) {
                _path.value!!.addPath(path)
            }
            _currentPath.value!!.reset()

        } else {
            return
        }
    }

    fun clear() {
        _path.value = Path()
        _currentPath.value = Path()
        _pathList.value = ArrayList()
        _undoPathList.value = ArrayList()
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

    fun toggleErase(canvasView: CustomCanvasView) {
        eraseToggle = !eraseToggle
        isEraseOn.value = eraseToggle
        canvasView.toggleErase(isEraseOn.value!!)
    }

    fun deleteSelectedPath(path: Path) {
        _pathList.value!!.remove(path)
    }

    fun cleanPath() {
        cleanPath()
    }
}