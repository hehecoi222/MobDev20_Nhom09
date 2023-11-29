package com.mobdev20.nhom09.quicknote

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.mobdev20.nhom09.quicknote.databinding.ActivityMainBinding
import com.mobdev20.nhom09.quicknote.ui.theme.MainAppTheme
import com.mobdev20.nhom09.quicknote.viewmodels.EditorViewModel
import com.mobdev20.nhom09.quicknote.views.BottomSheetDrawer
import com.mobdev20.nhom09.quicknote.views.CustomTopAppBar
import com.mobdev20.nhom09.quicknote.views.KindOfBottomSheet
import com.mobdev20.nhom09.quicknote.views.NoteTitleTextField
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var darkMode = false

    @Inject
    lateinit var editorViewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme ứng dụng
        setTheme(
            if (DynamicColors.isDynamicColorAvailable()) R.style.Theme_QuickNote_Dynamic
            else if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) R.style.Theme_QuickNote_Dark
            else R.style.Theme_QuickNote
        )

        // onCreate ban đầu
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        // khởi tạo các giá trị UI
        val noteContent = binding.noteBody
        val offset = mutableStateOf(false)
        val isKeyboardActive = mutableStateOf(false)
        val kindOfBottomSheet = mutableStateOf(KindOfBottomSheet.OldNotes)
        val expanded = mutableStateOf(false)
        val isScrolling = mutableStateOf(false)

        // Nạp Compose TopBar vào
        binding.topAppBar.apply {
            setContent {
                MainAppTheme {
                    darkMode = isSystemInDarkTheme()
                    CustomTopAppBar(modifier = Modifier.fillMaxWidth(),
                        offset = offset,
                        onClickFormat = { // Mở bảng format
                            if (kindOfBottomSheet.value == KindOfBottomSheet.FormatBar) {
                                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                                val params = binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
                                params.bottomMargin = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    20f,
                                    resources.displayMetrics
                                ).toInt()
                                binding.noteContainer.layoutParams = params
                            }
                            else {
                                kindOfBottomSheet.value = KindOfBottomSheet.FormatBar
                                val params = binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
                                params.bottomMargin = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    64f,
                                    resources.displayMetrics
                                ).toInt()
                                binding.noteContainer.layoutParams = params
                            }
                        },
                        onClickMore = { // Mở bảng thêm
                            (this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                                ?.hideSoftInputFromWindow(view.windowToken, 0)
                            if (kindOfBottomSheet.value == KindOfBottomSheet.MoreOpts)
                                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                            else
                                kindOfBottomSheet.value = KindOfBottomSheet.MoreOpts
                        },
                        onClickAccount = { editorViewModel.reverseHistory() })
                }
            }
        }

        // Nạp Compose bottomSheet
        binding.bottomSheetDrawer.apply {
            setContent {
                MainAppTheme {
                    BottomSheetDrawer(
                        isKeyboardActive = isKeyboardActive,
                        kindOfBottomSheet = kindOfBottomSheet,
                        expanded = expanded
                    )
                }
            }
        }

        // Nạp Compose NoteTitleTextField
        binding.noteTitleCompose.apply {
            setContent {
                MainAppTheme {
                    NoteTitleTextField(
                        value = editorViewModel.noteState.collectAsState().value.title,
                        onValueChange = {
                            editorViewModel.editTitle(it)
                            editorViewModel.saveNoteAfterDelay()
                        }) {
                        binding.noteBody.requestFocus()
                    }
                }
            }
        }

        // Listener cái soft có hiện không
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)
            val heightDiff = (view.rootView.height - (r.bottom - r.top))
            val typed = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                64f,
                resources.displayMetrics
            ).toInt()
            isKeyboardActive.value = heightDiff > typed

            // Logic ẩn hiện bottom và padding content
            if (isKeyboardActive.value && kindOfBottomSheet.value != KindOfBottomSheet.FormatBar) {
                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                val params = binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    resources.displayMetrics
                ).toInt()
                binding.noteContainer.layoutParams = params
            } else if (!isKeyboardActive.value) {
                val params = binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    64f,
                    resources.displayMetrics
                ).toInt()
                binding.noteContainer.layoutParams = params
            }
        }

        // Listener ẩn soft key khi người dùng vuốt lên
        binding.noteContainer.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            run {
                offset.value = scrollY > TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    36f,
                    resources.displayMetrics
                )
                if (scrollY < oldScrollY - 36 && isScrolling.value) {
                    (this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        ?.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        noteContent.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            run {
                Log.d("ACTION", motionEvent.action.toString())
                when (motionEvent.action) {
                    MotionEvent.ACTION_MOVE -> {
                        isScrolling.value = true
                    }

                    MotionEvent.ACTION_DOWN -> {
                        isScrolling.value = true
                    }

                    MotionEvent.ACTION_CANCEL -> {}
                    else -> isScrolling.value = false
                }
                view.performClick()
                view.onTouchEvent(motionEvent)
            }
        }

        // Listener khi text thay đổi
        noteContent.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
//                TODO("Not yet implemented"), Implement text processor in helpers
                val stringAfterProcessed = if (s.isNullOrEmpty()) "" else s.toString()
                if (editorViewModel.currentReverseHistory.value != null) {
                    editorViewModel.currentReverseHistory.value = null
                    return
                }
                editorViewModel.editBody(stringAfterProcessed, noteContent.selectionEnd)
                editorViewModel.saveNoteAfterDelay()
            }
        })

        // Handle thoát ứng dụng
        this.onBackPressedDispatcher.addCallback {
            if (expanded.value == true) expanded.value = false
            else finish()
        }

        // ViewModel launch cập nhật giá trị
        lifecycleScope.launch {
            editorViewModel.noteState.collect {
                if (!editorViewModel.load.value) {
                    return@collect
                }
                val edit = it.content
                if (editorViewModel.currentReverseHistory.value != null) {
                    editorViewModel.load.value = false
                    val cursor =
                        if (noteContent.selectionEnd > edit.length) edit.length else noteContent.selectionEnd
                    noteContent.setText(edit)
                    noteContent.setSelection(cursor)
                    return@collect
                }
                if (edit.isNotEmpty()) {
                    editorViewModel.load.value = false
                    noteContent.setText(edit)
                } else if (edit.isEmpty() && it.id.isEmpty()) {
                    editorViewModel.load.value = false
                    noteContent.setText("")
                }
            }
        }

        setContentView(view)
    }
}