package com.example.oniria;

import android.content.Intent;
// Eliminar SharedPreferences
// import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Añadir Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserSetupActivity extends AppCompatActivity {

    private static final String TAG = "UserSetupActivity"; // Añadir TAG

    private EditText etSueldo, etEgresosMensuales, etAhorrosDisponibles;
    private EditText etMeta1, etPresupuestoMeta1, etMeta2, etPresupuestoMeta2, etMeta3, etPresupuestoMeta3;
    private Button btnGuardar;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        // Verificar si el setup ya se completó
        DatabaseHelper.UserProfileData userProfile = dbHelper.getUserProfile();
        if (userProfile != null && userProfile.isProfileSetupComplete) {
            Log.d(TAG, "El perfil de usuario ya está configurado. Redirigiendo a DashboardActivity.");
            Intent intent = new Intent(UserSetupActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); 
            return; 
        }

        setContentView(R.layout.activity_user_setup);
        Log.d(TAG, "El perfil de usuario no está configurado o es la primera vez. Mostrando pantalla de configuración.");

        initViews();
        setupListeners();
    }

    private void initViews() {
        etSueldo = findViewById(R.id.et_sueldo);
        etEgresosMensuales = findViewById(R.id.et_egresos_mensuales);
        etAhorrosDisponibles = findViewById(R.id.et_ahorros_disponibles);

        etMeta1 = findViewById(R.id.et_meta1);
        etPresupuestoMeta1 = findViewById(R.id.et_presupuesto_meta1);
        etMeta2 = findViewById(R.id.et_meta2);
        etPresupuestoMeta2 = findViewById(R.id.et_presupuesto_meta2);
        etMeta3 = findViewById(R.id.et_meta3);
        etPresupuestoMeta3 = findViewById(R.id.et_presupuesto_meta3);

        btnGuardar = findViewById(R.id.btn_guardar);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarDatos()) {
                guardarDatos();
            }
        });
    }

    private boolean validarDatos() {
        // Validación de perfil de usuario
        if (TextUtils.isEmpty(etSueldo.getText().toString().trim())) {
            etSueldo.setError("Ingresa tu sueldo mensual");
            Toast.makeText(this, "Por favor ingresa tu sueldo mensual", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(etSueldo.getText().toString());
        } catch (NumberFormatException e) {
            etSueldo.setError("Sueldo inválido");
            Toast.makeText(this, "Sueldo mensual debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        etSueldo.setError(null);

        if (TextUtils.isEmpty(etEgresosMensuales.getText().toString().trim())) {
            etEgresosMensuales.setError("Ingresa tus egresos mensuales");
            Toast.makeText(this, "Por favor ingresa tus egresos mensuales promedio", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(etEgresosMensuales.getText().toString());
        } catch (NumberFormatException e) {
            etEgresosMensuales.setError("Egresos inválidos");
            Toast.makeText(this, "Egresos mensuales deben ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        etEgresosMensuales.setError(null);

        if (TextUtils.isEmpty(etAhorrosDisponibles.getText().toString().trim())) {
            etAhorrosDisponibles.setError("Ingresa tus ahorros disponibles");
            Toast.makeText(this, "Por favor ingresa tus ahorros disponibles", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(etAhorrosDisponibles.getText().toString());
        } catch (NumberFormatException e) {
            etAhorrosDisponibles.setError("Ahorros inválidos");
            Toast.makeText(this, "Ahorros disponibles deben ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        etAhorrosDisponibles.setError(null);

        // Validación de Metas
        if (!validarMeta(etMeta1, etPresupuestoMeta1, "Meta 1")) return false;
        if (!validarMeta(etMeta2, etPresupuestoMeta2, "Meta 2")) return false;
        if (!validarMeta(etMeta3, etPresupuestoMeta3, "Meta 3")) return false;

        return true;
    }

    private boolean validarMeta(EditText etNombreMeta, EditText etPresupuestoMeta, String nombreCampoMeta) {
        String nombre = etNombreMeta.getText().toString().trim();
        String presupuestoStr = etPresupuestoMeta.getText().toString().trim();

        if (nombre.isEmpty() && presupuestoStr.isEmpty()) {
            etNombreMeta.setError(null);
            etPresupuestoMeta.setError(null);
            return true;
        }

        if (!nombre.isEmpty() && presupuestoStr.isEmpty()) {
            etPresupuestoMeta.setError("Ingresa presupuesto para " + nombreCampoMeta);
            Toast.makeText(this, "Por favor ingresa el presupuesto para " + nombreCampoMeta, Toast.LENGTH_SHORT).show();
            return false;
        }
        etPresupuestoMeta.setError(null);

        if (nombre.isEmpty() && !presupuestoStr.isEmpty()) {
            etNombreMeta.setError("Ingresa nombre para " + nombreCampoMeta);
            Toast.makeText(this, "Por favor ingresa el nombre para " + nombreCampoMeta, Toast.LENGTH_SHORT).show();
            return false;
        }
        etNombreMeta.setError(null);

        if (!nombre.isEmpty() && !presupuestoStr.isEmpty()) {
            try {
                float presupuesto = Float.parseFloat(presupuestoStr);
                if (presupuesto <= 0) {
                    etPresupuestoMeta.setError("Presupuesto debe ser positivo");
                    Toast.makeText(this, "El presupuesto para " + nombreCampoMeta + " debe ser un número positivo", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                etPresupuestoMeta.setError("Presupuesto inválido");
                Toast.makeText(this, "El presupuesto para " + nombreCampoMeta + " debe ser un número válido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        etPresupuestoMeta.setError(null);
        return true;
    }


    private void guardarDatos() {
        float sueldo = Float.parseFloat(etSueldo.getText().toString().trim());
        float egresos = Float.parseFloat(etEgresosMensuales.getText().toString().trim());
        float ahorros = Float.parseFloat(etAhorrosDisponibles.getText().toString().trim());

        boolean profileSaved = dbHelper.upsertUserProfile(sueldo, egresos, ahorros, true);

        if (profileSaved) {
            Log.d(TAG, "Perfil de usuario guardado/actualizado. Setup marcado como completo.");
            dbHelper.clearAllFinancialGoals();
            Log.d(TAG, "Metas antiguas eliminadas.");

            saveGoalToDb(etMeta1, etPresupuestoMeta1, "Meta 1");
            saveGoalToDb(etMeta2, etPresupuestoMeta2, "Meta 2");
            saveGoalToDb(etMeta3, etPresupuestoMeta3, "Meta 3");

            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserSetupActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar los datos del perfil.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveGoalToDb(EditText etNombreMeta, EditText etPresupuestoMeta, String metaDebugName) {
        String nombre = etNombreMeta.getText().toString().trim();
        String presupuestoStr = etPresupuestoMeta.getText().toString().trim();

        if (!nombre.isEmpty() && !presupuestoStr.isEmpty()) {
            try {
                float presupuesto = Float.parseFloat(presupuestoStr);
                if (presupuesto > 0) { 
                    boolean goalSaved = dbHelper.addFinancialGoal(nombre, presupuesto);
                    if (goalSaved) {
                        Log.d(TAG, metaDebugName + " guardada: " + nombre + " - " + presupuesto);
                    } else {
                        Log.e(TAG, "Error al guardar " + metaDebugName + ": " + nombre);
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error de formato de número para " + metaDebugName + ": " + nombre + " con presupuesto " + presupuestoStr, e);
            }
        }
    }
}
