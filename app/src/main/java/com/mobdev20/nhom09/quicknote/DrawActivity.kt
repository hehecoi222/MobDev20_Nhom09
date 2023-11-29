package com.mobdev20.nhom09.quicknote

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.mobdev20.nhom09.quicknote.viewmodels.EditorViewModel
import javax.inject.Inject

class DrawActivity : AppCompatActivity() {

    @Inject
    lateinit var editorViewModel: EditorViewModel

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val paint = Paint()
    private var floatStartX = -1f
    private var floatStartY = -1f
    private var floatEndX = -1f
    private var floatEndY = -1f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
    }

    // In tutorial we draw on imageView , in this outline, I don't know where do we draw so i comment on it
//    Please change it according to your preference
    private fun DrawPaintSketchImage() {
//        bitmap = paper
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(
//                imageView.getWidth(),
//                imageView.getHeight(),
                0,0,
                Bitmap.Config.ARGB_8888
            )
            //  canvas = brush
            canvas = Canvas(bitmap!!)
            paint.color = Color.RED
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
        }
        canvas!!.drawLine(floatStartX, floatStartY, floatEndX, floatEndY, paint)

//      update the ImageView and show the drawn content on the screen. : Should be x.setimagebitmap(bitmap)
//        imageView.setImageBitmap(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//      starting point
        if (event.action == MotionEvent.ACTION_DOWN) {
            floatStartX = event.x
            floatStartY = event.y
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            floatEndX = event.x
            floatEndY = event.y
            DrawPaintSketchImage()
            floatStartX = event.x
            floatStartY = event.y
        }
        if (event.action == MotionEvent.ACTION_UP) {
            floatEndX = event.x
            floatEndY = event.y
            DrawPaintSketchImage()
        }
        return super.onTouchEvent(event)
    }

//    Should have a function onclick on a button done -> call EditorViewModel.saveAsBitmap(bitmap)

}