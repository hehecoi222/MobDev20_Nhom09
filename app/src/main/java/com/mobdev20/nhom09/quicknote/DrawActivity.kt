package com.mobdev20.nhom09.quicknote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.mobdev20.nhom09.quicknote.databinding.ActivityMainBinding
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.viewmodels.EditorDrawModel
import com.mobdev20.nhom09.quicknote.viewmodels.EditorViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class DrawActivity : AppCompatActivity() {

    @Inject
    lateinit var editorDrawModel: EditorDrawModel


    private lateinit var imageView : ImageView

    private lateinit var button : Button

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val paint = Paint()
    private var floatStartX = -1f
    private var floatStartY = -1f
    private var floatEndX = -1f
    private var floatEndY = -1f

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.button)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root

//        editorDrawModel = EditorDrawModel(view.context)

        button.setOnClickListener{
            bitmap?.let { it1 -> clickSave(it1) }
        }
    }

    // In tutorial we draw on imageView , in this outline, I don't know where do we draw so i comment on it
//    Please change it according to your preference
    private fun DrawPaintSketchImage() {
//        bitmap = paper
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(
                imageView.getWidth(),
                imageView.getHeight(),
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
        imageView.setImageBitmap(bitmap)
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

    fun clickSave(bitmap: Bitmap) {
        editorDrawModel.saveAsBitmap(bitmap)
    }

}