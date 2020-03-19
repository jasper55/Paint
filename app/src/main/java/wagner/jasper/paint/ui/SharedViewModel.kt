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

    private val _undoPath = MutableLiveData<Path>()
    val undoPath: LiveData<Path>
        get() = _undoPath

    init {
        _path.value = Path()
        _undoPath.value = Path()
        _currentPath.value = Path()
        _pathList.value = ArrayList()
    }


    // after the user has stopped moving and touches the screen again
    fun touchStart() {
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
        val lastPath = _pathList.value!!.last()
        _undoPath.value!!.addPath(lastPath)
        _pathList.value!!.remove(lastPath)
        _path.value!!.reset()

        for (path in _pathList.value!!) {
            _path.value!!.addPath(path)
        }
        _currentPath.value!!.reset()

//        Log.i("SharedViewModel", "${_pathList.value!!.size} size after")
//
//        val updatedPath = Path()
//
//         set new currentPath to be able do further undo
//        val pathList = pathList.value!!
//        val size = pathList.size
//
//        Log.i("SharedViewModel", "pathlist iteration")
//        if (size != 0) {
//            _currentPath.value = pathList.get(size - 1)
//            _path.value!!.reset()
//
//            for (i in 0 until size) {
//                Log.i("SharedViewModel", "$i")
//                updatedPath.addPath(pathList[i])
//                Log.i("SharedViewModel", "${_path.value!!}")
//            }
//        } else {
//            _currentPath.value = Path()
//            _path.value = Path()
//        }
//        _path.value = updatedPath
    }


//    fun undo() {
//        if (mPaths.isEmpty() && mLastPaths.isNotEmpty()) {
//            mPaths = mLastPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
//            mLastPaths.clear()
//            invalidate()
//            return
//        }
//        if (mPaths.isEmpty()) {
//            return
//        }
//        val lastPath = mPaths.values.lastOrNull()
//        val lastKey = mPaths.keys.lastOrNull()
//
//        mPaths.remove(lastKey)
//        if (lastPath != null && lastKey != null) {
//            mUndonePaths[lastKey] = lastPath
//        }
//        invalidate()
//    }

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