package com.mobdev20.nhom09.quicknote

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdev20.nhom09.quicknote.databinding.ActivityMainBinding
import com.mobdev20.nhom09.quicknote.datasources.ChooseAttachment
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.ui.theme.MainAppTheme
import com.mobdev20.nhom09.quicknote.viewmodels.EditorViewModel
import com.mobdev20.nhom09.quicknote.views.BottomSheetDrawer
import com.mobdev20.nhom09.quicknote.views.CustomTopAppBar
import com.mobdev20.nhom09.quicknote.views.KindOfBottomSheet
import com.mobdev20.nhom09.quicknote.views.NoteTitleTextField
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var darkMode = false

    @Inject
    lateinit var editorViewModel: EditorViewModel

    @Inject
    lateinit var storageDatasource: StorageDatasource

    lateinit var getContent : ActivityResultLauncher<Unit>

    private lateinit var auth: FirebaseAuth

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

        auth = Firebase.auth

        // khởi tạo các giá trị UI
        val noteContent = binding.noteBody
        val offset = mutableStateOf(false)
        val isKeyboardActive = mutableStateOf(false)
        val kindOfBottomSheet = mutableStateOf(KindOfBottomSheet.OldNotes)
        val expanded = mutableStateOf(false)
        val isScrolling = mutableStateOf(false)
        getContent = activityResultRegistry.register("attachment", ChooseAttachment(this.applicationContext, storageDatasource)) {
            if (it != null) {
                editorViewModel.addAttachment(it as File)
            }
        }

        // Nạp Compose TopBar vào
        binding.topAppBar.apply {
            setContent {
                MainAppTheme {
                    darkMode = isSystemInDarkTheme()
                    CustomTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        offset = offset,
                        onClickFormat = { // Mở bảng format
                            if (kindOfBottomSheet.value == KindOfBottomSheet.FormatBar) {
                                kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                                val params =
                                    binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
                                params.bottomMargin = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    20f,
                                    resources.displayMetrics
                                ).toInt()
                                binding.noteContainer.layoutParams = params
                            } else {
                                kindOfBottomSheet.value = KindOfBottomSheet.FormatBar
                                val params =
                                    binding.noteContainer.layoutParams as ViewGroup.MarginLayoutParams
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
                        onClickUndo = {
                            editorViewModel.reverseHistory()
                        },
                        onClickRedo = { editorViewModel.replayHistory(editorViewModel.redoHistory.removeLast()) },
                        redoEnable = editorViewModel.redoEnabled,
                        onClickAccount = {
                            startActivity(Intent(context, AccountActivity::class.java))
                        }
                    )
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
                        expanded = expanded,
                        noteList = editorViewModel.noteList,
                        onDeleteNote = {
                            editorViewModel.deleteNote()
                        },
                        onExpandNote = {
                            editorViewModel.loadNoteList()
                        },
                        onClickNote = {
                            editorViewModel.selectNoteToLoad(it)
                            expanded.value = false
                            noteContent.requestFocus()
                        },
                        onClickAttachment = {
                            getContent.launch(Unit)
                        },
                        attachmentList = editorViewModel.currentAttachment,
                        onDeleteAttachment = {
                            editorViewModel.deleteAttachment(it)
                        },
                        onClickBackup = {
                            editorViewModel.backupNote()
                        },
                        onClickSync = {
                            editorViewModel.restoreNote()
                        }
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
                        }, noteList = editorViewModel.noteList.toList(),
                        createNote = {
                            editorViewModel.saveNoteAfterDelay()
                            noteContent.requestFocus()
                        },
                        onSelectNote = {
                            editorViewModel.selectNoteToLoad(it)
                            noteContent.requestFocus()
                        },
                        clearState = {
                            editorViewModel.clearState()
                        },
                        isClearAvailable = editorViewModel.noteState.collectAsState().value.id.isNotEmpty()
                    ) {
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
            if (isKeyboardActive.value && kindOfBottomSheet.value == KindOfBottomSheet.OldNotes) {
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
                if (editorViewModel.noteState.value.id.isEmpty()) return
                editorViewModel.editBody(stringAfterProcessed, noteContent.selectionEnd)
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
                    val cursor =
                        if (noteContent.selectionEnd > edit.length) edit.length else noteContent.selectionEnd
                    noteContent.setText(edit)
                    noteContent.setSelection(cursor)
                } else if (edit.isEmpty() && it.id.isEmpty()) {
                    editorViewModel.load.value = false
                    noteContent.setText("")
                }
            }
        }

        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        editorViewModel.updateUser(auth = auth)
        editorViewModel.loadNoteList()
    }

    override fun onDestroy() {
        getContent.unregister()
        super.onDestroy()
    }
}