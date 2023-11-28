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
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
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
        setTheme(
            if (DynamicColors.isDynamicColorAvailable()) R.style.Theme_QuickNote_Dynamic
            else if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) R.style.Theme_QuickNote_Dark
            else R.style.Theme_QuickNote
        )

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        val offset = mutableStateOf(false)
        val isKeyboardActive = mutableStateOf(false)
        val kindOfBottomSheet = mutableStateOf(KindOfBottomSheet.OldNotes)
        val expanded = mutableStateOf(false)
        val isScrolling = mutableStateOf(false)

        binding.topAppBar.apply {
            setContent {
                MainAppTheme {
                    darkMode = isSystemInDarkTheme()
                    CustomTopAppBar(modifier = Modifier.fillMaxWidth(),
                        offset = offset,
                        onClickFormat = { // TODO: This is how Format text work, move it to ViewModel
//                            val start = noteBody.selectionStart
//                            val end = noteBody.selectionEnd
//                            if (start != end) {
//                                var exist = false
//                                noteBody.text.getSpans(start, end, StyleSpan::class.java)?.forEach {
//                                    val style = it.style
//                                    if (style == Typeface.BOLD) {
//                                        noteBody.text.removeSpan(it)
//                                        exist = true
//                                    }
//                                }
//
//                                if (!exist) {
//                                    noteBody.text.setSpan(
//                                        StyleSpan(Typeface.BOLD),
//                                        start,
//                                        end,
//                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                                    )
//                                }
//                            }
                            if (kindOfBottomSheet.value == KindOfBottomSheet.FormatBar)
                                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                            else
                                kindOfBottomSheet.value = KindOfBottomSheet.FormatBar
                        },
                        onClickMore = {
                            (this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                                ?.hideSoftInputFromWindow(view.windowToken, 0)
                            if (kindOfBottomSheet.value == KindOfBottomSheet.MoreOpts)
                                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                            else
                                kindOfBottomSheet.value = KindOfBottomSheet.MoreOpts
                        })
                }
            }
        }

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
            if (isKeyboardActive.value && kindOfBottomSheet.value != KindOfBottomSheet.FormatBar) kindOfBottomSheet.value =
                KindOfBottomSheet.OldNotes
        }

        binding.noteContainer.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            run {
                offset.value = scrollY > TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10f,
                    resources.displayMetrics
                )
                if (scrollY < oldScrollY - 10 && isScrolling.value) {
                    (this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        ?.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }

        val noteContent = binding.noteBody

        noteContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
//                TODO("Not yet implemented"), Implement text processor
                editorViewModel.editBody(s.toString())
                editorViewModel.saveNoteAfterDelay()
            }
        })

        lifecycleScope.launch {
            editorViewModel.noteState.collect {
                if (!editorViewModel.load.value) {
                    return@collect
                }
                var edit = it.content
                if (edit != null && edit!!.isNotEmpty()) {
                    editorViewModel.load.value = false
                    noteContent.setText(edit)
                } else if (edit.toString().isEmpty() && it.id.isEmpty()) {
                    editorViewModel.load.value = false
                    noteContent.setText("")
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

        this.onBackPressedDispatcher.addCallback {
            if (expanded.value == true) expanded.value = false
            else finish()
        }

        setContentView(view)
    }
}