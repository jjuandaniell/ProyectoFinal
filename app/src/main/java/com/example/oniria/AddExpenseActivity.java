package com.example.oniria;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private static final String TAG = "AddExpenseActivity";
    private EditText etMonto, etDescripcion;
    private Spinner spinnerCategoryExpense; // <--- Spinner añadido
    private Button btnGuardar, btnCancelar;
    private DatabaseHelper dbHelper;

    // Lista de categorías predefinidas
    private static final String[] GASTO_CATEGORIES = {
            "Alimentación", "Transporte", "Servicios", "Vivienda", "Salud", 
            "Ocio", "Educación", "Ropa y Accesorios", "Regalos y Donaciones", "Otros"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        spinnerCategoryExpense = findViewById(R.id.spinner_category_expense); // <--- Inicializar Spinner
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }

    private void setupCategorySpinner() {
        List<String> categoriesList = Arrays.asList(GASTO_CATEGORIES);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoriesList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryExpense.setAdapter(adapter);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarDatos()) {
                guardarEgresoConDB();
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

        // Validar que se haya seleccionado una categoría (el Spinner no estará vacío por defecto)
        if (spinnerCategoryExpense.getSelectedItem() == null || 
            spinnerCategoryExpense.getSelectedItem().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            // Podrías poner un error en el Spinner o en un TextView asociado si lo tuvieras.
            return false;
        }

        return true;
    }

    private void guardarEgresoConDB() {
        float monto;
        try {
            monto = Float.parseFloat(etMonto.getText().toString().trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error de formato al convertir monto: " + etMonto.getText().toString(), e);
            Toast.makeText(this, "Monto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoryExpense.getSelectedItem().toString(); // <--- Obtener categoría del Spinner
        long fechaActualMillis = System.currentTimeMillis();

        // Llamar a addTransaction con la categoría
        boolean transaccionGuardada = dbHelper.addTransaction(descripcion, monto, "expense", fechaActualMillis, categoria);

        if (transaccionGuardada) {
            Log.d(TAG, "Gasto guardado en tabla transactions (categoría: " + categoria + "). Procediendo a actualizar perfil.");
            DatabaseHelper.UserProfileData currentUserProfile = dbHelper.getUserProfile();

            if (currentUserProfile == null) {
                Log.e(TAG, "Error crítico: currentUserProfile es null después de guardar transacción.");
                Toast.makeText(this, "Error crítico al obtener perfil de usuario.", Toast.LENGTH_LONG).show();
                return;
            }

            // La lógica de actualizar egresosMensuales y ahorrosDisponibles en UserProfileData 
            // podría necesitar ser revisada si quieres que UserProfileData refleje los totales 
            // o si ahora los egresos se calculan dinámicamente desde la tabla de transacciones.
            // Por ahora, mantenemos la lógica existente de actualizar los totales en UserProfile.
            float nuevosEgresosMensuales = currentUserProfile.egresosMensuales + monto;
            float nuevosAhorrosDisponibles = currentUserProfile.ahorrosDisponibles - monto;

            Log.d(TAG, "Actualizando perfil: Sueldo=" + currentUserProfile.sueldoMensual +
                    ", NuevosEgresos=" + nuevosEgresosMensuales +
                    ", NuevosAhorros=" + nuevosAhorrosDisponibles +
                    ", SetupCompleto=" + currentUserProfile.isProfileSetupComplete);

            boolean perfilActualizado = dbHelper.upsertUserProfile(
                    currentUserProfile.sueldoMensual,
                    nuevosEgresosMensuales,
                    nuevosAhorrosDisponibles,
                    currentUserProfile.isProfileSetupComplete
            );

            if (perfilActualizado) {
                Toast.makeText(this, "Gasto agregado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Gasto agregado a transacciones, PERO hubo un error al actualizar el perfil en la BD.");
                Toast.makeText(this, "Gasto agregado, pero hubo un error al actualizar el perfil.", Toast.LENGTH_LONG).show();
                finish(); 
            }
        } else {
            Log.e(TAG, "Error al guardar el gasto en la tabla transactions.");
            Toast.makeText(this, "Error al guardar el gasto.", Toast.LENGTH_LONG).show();
        }
    }
}
