package wagner.jasper.paint.ui

import android.app.Application
import android.graphics.Path
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import wagner.jasper.paint.R
import wagner.jasper.paint.model.MyPaint
import kotlin.math.abs

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private var isEraseOn = MutableLiveData<Boolean>(false)

    private val _pathList: MutableLiveData<LinkedHashMap<Path, MyPaint>> by lazy { MutableLiveData<LinkedHashMap<Path, MyPaint>>() }
    val pathList: LiveData<LinkedHashMap<Path, MyPaint>>
        get() = _pathList

    private val _currentPath = MutableLiveData<Path>()
    val currentPath: LiveData<Path>
        get() = _currentPath

    private val _backgroundColor = MutableLiveData<Int>()
    val backgroundColor: LiveData<Int>
        get() = _backgroundColor

    private val _strokeWidth = MutableLiveData<Float>(12f)
    val strokeWidth: LiveData<Float>
        get() = _strokeWidth

    private val _colorAlpha = MutableLiveData<Int>(255)
    val colorAlpha: LiveData<Int>
        get() = _colorAlpha

    private val _drawColor = MutableLiveData<Int>()
    val drawColor: LiveData<Int>
        get() = _drawColor

    private var motionTouchEventX = 0F
    private var motionTouchEventY = 0F

    // to cache latest x and y values
    private var currentX = 0F
    private var currentY = 0F

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
        _backgroundColor.value =
            ColorUtils.setAlphaComponent(application.resources.getColor(R.color.background), 255)
        _drawColor.value =
            ColorUtils.setAlphaComponent(application.resources.getColor(R.color.drawColor), 255)
        _colorAlpha.value = 255
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

    private fun setCurrentPaint() {
        _currentPaint.value = MyPaint(
            isEraseOn = isEraseOn.value!!,
            backgroundColor = _backgroundColor.value!!,
            drawColor = _drawColor.value!!,
            alphaSet = _colorAlpha.value!!,
            strokeWidthSet = _strokeWidth.value!!
        )
    }

    fun undo() {
        removeAdd(
            listToRemove = pathList.value!!,
            listToAdd = _undoPathList.value!!
        )
    }

    fun redo() {
        removeAdd(
            listToRemove = _undoPathList.value!!,
            listToAdd = pathList.value!!
        )
    }

    private fun removeAdd(
        listToRemove: LinkedHashMap<Path, MyPaint>,
        listToAdd: LinkedHashMap<Path, MyPaint>
    ) {
        if (listToRemove.isNotEmpty()) {
            val key = listToRemove.keys.last()
            val value = listToRemove.values.last()
            listToRemove.remove(key)
            listToAdd.put(key, value)
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
            _currentPaint.value!!.copy(
                isEraseOn = !previousEraseSetting
            )
            color = if (previousEraseSetting == false) _backgroundColor.value!!
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
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, _currentPaint.value!!.alpha)
        _backgroundColor.value = alphaColor
    }

    fun setAlpha(newAlpha: Int) {
        val alpha = (newAlpha * 255) / 100
        _colorAlpha.value = newAlpha
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        _strokeWidth.value = newStrokeWidth
    }

    fun deleteSelectedPath(path: Path) {
//        _pathList.value!!.remove(path)
    }

    fun cleanPath() {
        cleanPath()
    }

}
