package com.example.oniria;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class AddIncomeActivity extends AppCompatActivity {

    private static final String TAG = "AddIncomeActivity";
    private EditText etMonto, etDescripcion;
    private Spinner spinnerCategoryIncome;
    private Button btnGuardar, btnCancelar;
    private DatabaseHelper dbHelper;

    // Lista de categorías de ingresos
    private static final String[] INGRESO_CATEGORIES = {
            "Sueldo", "Ventas", "Regalo", "Inversiones", "Freelance", "Otros"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        spinnerCategoryIncome = findViewById(R.id.spinner_category_income);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }

    private void setupCategorySpinner() {
        List<String> categoriesList = Arrays.asList(INGRESO_CATEGORIES);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoriesList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryIncome.setAdapter(adapter);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarDatos()) {
                guardarIngresoConDB();
            }
        });

        btnCancelar.setOnClickListener(v -> finish());
    }

    private boolean validarDatos() {
        if (TextUtils.isEmpty(etMonto.getText().toString().trim())) {
            etMonto.setError("Ingresa el monto");
            Toast.makeText(this, "Por favor ingresa el monto", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(etMonto.getText().toString().trim());
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            Toast.makeText(this, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        etMonto.setError(null);

        if (TextUtils.isEmpty(etDescripcion.getText().toString().trim())) {
            etDescripcion.setError("Ingresa una descripción");
            Toast.makeText(this, "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show();
            return false;
        }
        etDescripcion.setError(null);

        if (spinnerCategoryIncome.getSelectedItem() == null || 
            spinnerCategoryIncome.getSelectedItem().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void guardarIngresoConDB() {
        float monto;
        try {
            monto = Float.parseFloat(etMonto.getText().toString().trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error de formato al convertir monto: " + etMonto.getText().toString(), e);
            Toast.makeText(this, "Monto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoryIncome.getSelectedItem().toString();
        long fechaActualMillis = System.currentTimeMillis();

        boolean transaccionGuardada = dbHelper.addTransaction(descripcion, monto, "income", fechaActualMillis, categoria);

        if (transaccionGuardada) {
            Log.d(TAG, "Ingreso guardado en tabla transactions. Procediendo a actualizar perfil.");
            DatabaseHelper.UserProfileData currentUserProfile = dbHelper.getUserProfile();

            if (currentUserProfile == null) {
                Log.e(TAG, "Error crítico: currentUserProfile es null después de guardar transacción.");
                Toast.makeText(this, "Error crítico al obtener perfil de usuario.", Toast.LENGTH_LONG).show();
                return;
            }

            float nuevosAhorrosDisponibles = currentUserProfile.ahorrosDisponibles + monto;

            Log.d(TAG, "Actualizando perfil: Sueldo=" + currentUserProfile.sueldoMensual +
                    ", Egresos=" + currentUserProfile.egresosMensuales +
                    ", NuevosAhorros=" + nuevosAhorrosDisponibles +
                    ", SetupCompleto=" + currentUserProfile.isProfileSetupComplete);

            boolean perfilActualizado = dbHelper.upsertUserProfile(
                    currentUserProfile.sueldoMensual,
                    currentUserProfile.egresosMensuales,
                    nuevosAhorrosDisponibles,
                    currentUserProfile.isProfileSetupComplete
            );

            if (perfilActualizado) {
                Toast.makeText(this, "Ingreso agregado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Ingreso agregado a transacciones, PERO hubo un error al actualizar el perfil en la BD.");
                Toast.makeText(this, "Ingreso agregado, pero hubo un error al actualizar el perfil.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Log.e(TAG, "Error al guardar el ingreso en la tabla transactions.");
            Toast.makeText(this, "Error al guardar el ingreso.", Toast.LENGTH_LONG).show();
        }
    }
}
