package com.example.quicknotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edtTitle, edtBody;
    BottomSheetDialog bottomSheetDialog;

    LinearLayout oldNotesLayout;
    BottomSheetBehavior sheetBehavior;
    LinearLayout oldNotesLayoutHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        edtTitle = findViewById(R.id.edt_title);
        edtBody = findViewById(R.id.edt_body);

        // Old Notes Screen
        oldNotesLayout = findViewById(R.id.old_notes_layout);
        sheetBehavior = BottomSheetBehavior.from(oldNotesLayout);
        oldNotesLayoutHeader = findViewById(R.id.old_notes_layout_header);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        oldNotesLayoutHeader.setOnClickListener(view -> handleShowOldNotes());
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0.1) {
                    oldNotesLayoutHeader.setBackgroundResource(R.color.white);
                }
                else {
                    oldNotesLayoutHeader.setBackgroundResource(R.color.grey);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_more) {
            // Menu khác hiện ra -> bottom sheet dialog
            showBottomSheetDialog();
        } else if (id == R.id.menu_edit) {

        } else {

        }

        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);

        LinearLayout backup = bottomSheetDialog.findViewById(R.id.layout_backup);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.layout_delete);
        LinearLayout share = bottomSheetDialog.findViewById(R.id.layout_share);
        LinearLayout attachment = bottomSheetDialog.findViewById(R.id.layout_attachment);

        bottomSheetDialog.show();

        // Bottom sheet dialog action
        backup.setOnClickListener(view -> handleBackUp());
        delete.setOnClickListener(view -> handleDelete());
        share.setOnClickListener(view -> handleShare());
        attachment.setOnClickListener(view -> handleAttachment());
    }

    public void handleBackUp() {
        Toast.makeText(this, "Backup", Toast.LENGTH_LONG).show();
        bottomSheetDialog.dismiss();
    }

    public void handleDelete() {
        Toast.makeText(this, "Delete", Toast.LENGTH_LONG).show();
        bottomSheetDialog.dismiss();
    }

    public void handleShare() {
        Toast.makeText(this, "Share", Toast.LENGTH_LONG).show();
        bottomSheetDialog.dismiss();
    }

    public void handleAttachment() {
        Toast.makeText(this, "Attachment", Toast.LENGTH_LONG).show();
        bottomSheetDialog.dismiss();
    }

    public void handleShowOldNotes() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
}