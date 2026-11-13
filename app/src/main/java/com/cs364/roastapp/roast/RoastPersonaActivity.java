package com.cs364.roastapp.roast;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.ConfirmDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class RoastPersonaActivity extends AppCompatActivity {

    private RoastLineProvider lineProvider;
    private CustomRoastDao customRoastDao;
    private CustomRoastAdapter adapter;

    private RadioGroup radioGroupPersona;
    private TextView textSampleLine;
    private TextInputLayout inputLayoutCustom;
    private TextInputEditText editCustomRoast;
    private TextView textEmptyCustom;
    private RecyclerView recyclerCustom;

    private boolean isBindingUi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roast_activity_persona);

        lineProvider = RoastLineProvider.getInstance(this);
        customRoastDao = AppServices.getDatabase(this).customRoastDao();

        radioGroupPersona = findViewById(R.id.radio_group_persona);
        textSampleLine = findViewById(R.id.text_persona_sample_line);
        inputLayoutCustom = findViewById(R.id.input_layout_custom_roast);
        editCustomRoast = findViewById(R.id.edit_custom_roast);
        textEmptyCustom = findViewById(R.id.text_custom_roast_empty);
        recyclerCustom = findViewById(R.id.recycler_custom_roasts);

        Button btnPreview = findViewById(R.id.btn_preview_roast);
        Button btnAdd = findViewById(R.id.btn_add_custom_roast);
        Button btnBack = findViewById(R.id.btn_roast_back);

        adapter = new CustomRoastAdapter(this::confirmDelete);
        recyclerCustom.setLayoutManager(new LinearLayoutManager(this));
        recyclerCustom.setAdapter(adapter);

        bindSelectedPersona();
        refreshSampleLine();
        refreshCustomList();

        radioGroupPersona.setOnCheckedChangeListener((group, checkedId) -> {
            if (isBindingUi) {
                return;
            }
            String persona = RoastLineProvider.PERSONA_STRICT;
            if (checkedId == R.id.radio_persona_sarcastic) {
                persona = RoastLineProvider.PERSONA_SARCASTIC;
            } else if (checkedId == R.id.radio_persona_mom) {
                persona = RoastLineProvider.PERSONA_MOM;
            }
            lineProvider.setSelectedPersona(persona);
            refreshSampleLine();
        });

        btnPreview.setOnClickListener(v -> refreshSampleLine());
        btnAdd.setOnClickListener(v -> addCustomRoast());
        btnBack.setOnClickListener(v -> finish());
    }

    private void bindSelectedPersona() {
        isBindingUi = true;
        String current = lineProvider.getSelectedPersona();
        if (RoastLineProvider.PERSONA_SARCASTIC.equals(current)) {
            radioGroupPersona.check(R.id.radio_persona_sarcastic);
        } else if (RoastLineProvider.PERSONA_MOM.equals(current)) {
            radioGroupPersona.check(R.id.radio_persona_mom);
        } else {
            radioGroupPersona.check(R.id.radio_persona_strict);
        }
        isBindingUi = false;
    }

    private void refreshSampleLine() {
        textSampleLine.setText(lineProvider.getNextRoastLine(this));
    }

    private void refreshCustomList() {
        List<CustomRoastEntity> items = customRoastDao.getAll();
        adapter.submitList(items);
        boolean isEmpty = items.isEmpty();
        textEmptyCustom.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerCustom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void addCustomRoast() {
        String raw = editCustomRoast.getText() != null
                ? editCustomRoast.getText().toString().trim()
                : "";
        if (raw.isEmpty()) {
            inputLayoutCustom.setError(getString(R.string.roast_custom_error_empty));
            return;
        }
        inputLayoutCustom.setError(null);
        customRoastDao.insert(new CustomRoastEntity(raw, System.currentTimeMillis()));
        editCustomRoast.setText("");
        refreshCustomList();
        refreshSampleLine();
        Toast.makeText(this, R.string.roast_custom_added, Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete(CustomRoastEntity item) {
        ConfirmDialog.show(
                this,
                R.string.roast_custom_delete_title,
                R.string.roast_custom_delete_message,
                () -> {
                    customRoastDao.delete(item);
                    refreshCustomList();
                    refreshSampleLine();
                }
        );
    }
}
