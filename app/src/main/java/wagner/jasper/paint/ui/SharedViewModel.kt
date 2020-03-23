package wagner.jasper.paint.ui

import android.app.Application
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import wagner.jasper.paint.model.MyPaint
import kotlin.math.abs

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private var isEraseOn = MutableLiveData<Boolean>(false)
    private val _pathList = MutableLiveData<LinkedHashMap<Path, MyPaint>>()
    val pathList: LiveData<LinkedHashMap<Path, MyPaint>>
        get() = _pathList

    private val _currentPath = MutableLiveData<Path>()
    val currentPath: LiveData<Path>
        get() = _currentPath

    private val _backgroundColor = MutableLiveData<Int>()
    val backgroundColor: LiveData<Int>
    get() = _backgroundColor

    private val _drawColor = MutableLiveData<Int>()
//    val drawColor: LiveData<Int>
//        get() = _drawColor

    private var motionTouchEventX = 0F
    private var motionTouchEventY = 0F

    // to cache latest x and y values
    private var currentX = 0F
    private var currentY = 0F

    private var chacheHash = LinkedHashMap<Path, MyPaint>()

    private val _path = MutableLiveData<LinkedHashMap<Path, MyPaint>>()
    val path: LiveData<LinkedHashMap<Path, MyPaint>>
        get() = _path

    private val _undoPathList = MutableLiveData<LinkedHashMap<Path, MyPaint>>()
    val undoPathList: LiveData<LinkedHashMap<Path, MyPaint>>
        get() = _undoPathList

    private val _currentPaint = MutableLiveData<MyPaint>()
    val currentPaint: LiveData<MyPaint>
        get() = _currentPaint

    init {
        _backgroundColor.value = Color.WHITE
        _drawColor.value = Color.BLACK
        setCurrentPaint()
        _path.value = LinkedHashMap()
        _currentPath.value = Path()
        _pathList.value = LinkedHashMap()
        _undoPathList.value = LinkedHashMap()
    }


    // after the user has stopped moving and touches the screen again
    fun touchStart() {
        _undoPathList.value = LinkedHashMap()
        _currentPath.value!!.reset()
        setCurrentPaint()
        _currentPath.value!!.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        Log.i("SharedViewModel", "touchStart()")
    }

    private fun setCurrentPaint() {
        val paint = MyPaint(isEraseOn.value!!,_backgroundColor.value!!,_drawColor.value!!)
        _currentPaint.value = paint
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
        _pathList.value!!.set(_currentPath.value!!, _currentPaint.value!!)
        _path.value?.set(_currentPath.value!!, _currentPaint.value!!)
        _currentPath.value = Path()
        Log.i("SharedViewModel", "touchUp()")
    }

    fun undoDrawLastPath() {
        if (_pathList.value!!.isNotEmpty()) {

            val key = _pathList.value!!.keys.last()
            val value = _pathList.value!!.values.last()
            _undoPathList.value!!.put(key, value)
            _pathList.value!!.remove(key)
            _path.value!!.clear()

            _path.value = _pathList.value!!.clone() as LinkedHashMap<Path, MyPaint>
            _currentPath.value!!.reset()
        } else {
            return
        }
    }

    fun redo() {
        if (_undoPathList.value!!.isNotEmpty()) {
            val key = _undoPathList.value!!.keys.last()
            val value = _undoPathList.value!!.values.last()
            _undoPathList.value!!.remove(key)
            _pathList.value!!.put(key, value)
            _path.value!!.clear()

            _path.value = _pathList.value!!.clone() as LinkedHashMap<Path, MyPaint>
            _currentPath.value!!.reset()
        } else {
            return
        }
    }

    fun clear() {
        _path.value = LinkedHashMap()
        _currentPath.value = Path()
        _pathList.value = LinkedHashMap()
        _undoPathList.value = LinkedHashMap()
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

    fun toggleErase() {
        val previousEraseSetting = isEraseOn.value!!
            _currentPaint.value!!.apply {
                isEraseOn = !previousEraseSetting
                color = if(previousEraseSetting == false) _backgroundColor.value!!
                else _drawColor.value!!
            }
        isEraseOn.value = !previousEraseSetting
        }

    fun setDrawColor(newColor: Int) {
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, _currentPaint.value!!.alpha)
        _drawColor.value = alphaColor
    }

    fun setBackgroundColor(newColor: Int) {
        _backgroundColor.value = newColor
    }

    fun setAlpha(newAlpha: Int) {
        val alpha = (newAlpha * 255) / 100
        _currentPaint.value!!.alpha = alpha
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        currentPaint.value!!.strokeWidth = newStrokeWidth
    }

    fun deleteSelectedPath(path: Path) {
//        _pathList.value!!.remove(path)
    }

    fun cleanPath() {
        cleanPath()
    }

//    fun addPath(path: Path, options: PaintOptions) {
//        _path.value!![path] = options
//    }
}