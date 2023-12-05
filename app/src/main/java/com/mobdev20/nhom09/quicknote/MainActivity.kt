package com.mobdev20.nhom09.quicknote

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdev20.nhom09.quicknote.databinding.ActivityMainBinding
import com.mobdev20.nhom09.quicknote.datasources.ChooseAttachment
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.helpers.TextProcessor
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

    lateinit var getContent: ActivityResultLauncher<Unit>

    private lateinit var auth: FirebaseAuth

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
            Toast.makeText(this, "You won't receive any notifications", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Check permissions
        askNotificationPermission()

        // Preload
        val intent = this.intent
        if (!intent?.getStringExtra("ID").isNullOrEmpty())
            editorViewModel.selectNoteToLoad(intent?.getStringExtra("ID")!!)

        // khởi tạo các giá trị UI
        val noteContent = binding.noteBody
        val offset = mutableStateOf(false)
        val isKeyboardActive = mutableStateOf(false)
        val kindOfBottomSheet = mutableStateOf(KindOfBottomSheet.OldNotes)
        val expanded = mutableStateOf(false)
        val isScrolling = mutableStateOf(false)
        getContent = activityResultRegistry.register(
            "attachment",
            ChooseAttachment(this.applicationContext, storageDatasource)
        ) {
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
                        },
                        onClickBold = {
                            val noteContent = binding.noteBody
                            val string = TextProcessor.setFormat(noteContent, Typeface.BOLD)
                            val spannable = TextProcessor.convertFormat(string)
//                            noteContent.setText(spannable)
//                            noteContent.setSelection(noteContent.text.length)
                            TextProcessor.renderFormat(noteContent, Typeface.BOLD)
                        },
                        onClickItalic = {
                            val noteContent = binding.noteBody
                            val string = TextProcessor.setFormat(noteContent, Typeface.ITALIC)
                            val spannable = TextProcessor.convertFormat(string)
//                            noteContent.setText(spannable)
//                            noteContent.setSelection(noteContent.text.length)
                            TextProcessor.renderFormat(noteContent, Typeface.ITALIC)
                        },
                        onClickUnderline = {
                            val noteContent = binding.noteBody
                            val string = TextProcessor.setFormat(noteContent, 3)
                            val spannable = TextProcessor.convertFormat(string)
//                            noteContent.setText(spannable)
//                            noteContent.setSelection(noteContent.text.length)
                            TextProcessor.renderFormat(noteContent, 3)
                        },
                        onClickOpen = {
                            val file = File(NoteJson.getLast(it.filepath))

                            // Get URI and MIME type of file
                            Log.d("FILE_TAG", application.packageName + ".provider" + file)

                            // Get URI and MIME type of file
                            val uri: Uri? = try {
                                FileProvider.getUriForFile(
                                    context,
                                    applicationContext.packageName + ".fileprovider",
                                    file
                                )
                            } catch (e: IllegalArgumentException) {
                                Log.e(
                                    "File Selector",
                                    "The selected file can't be shared: $file"
                                )
                                null
                            }
                            val mime = contentResolver.getType(uri!!)
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.setDataAndType(uri, mime)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(intent)
                        },
                        isNotiOn = editorViewModel.noteState.collectAsState().value.notificationId.isNotEmpty(),
                        onSetDate = { time ->
                            editorViewModel.instant.value = time
                        },
                        onSetTime = { hourOfDay, minute ->
                            editorViewModel.hourOfDay.value = hourOfDay
                            editorViewModel.minute.value = minute
                        },
                        onSetRemove = {
                            editorViewModel.deleteNotification()
                        },
                        onSetAdd = {
                            editorViewModel.addNotification()
                        },
                        time = editorViewModel.combineInstant()
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
                            editorViewModel.saveNoteNow()
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
                if (edit.isNotEmpty() && noteContent.text.toString() != edit.toString()) {
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
        editorViewModel.clearState()
        getContent.unregister()
        super.onDestroy()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) && shouldShowRequestPermissionRationale(android.Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                requestPermissionLauncher.launch(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }
    }
}