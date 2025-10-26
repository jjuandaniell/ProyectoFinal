package com.example.oniria;

// Removido: import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditSalaryActivity extends AppCompatActivity {

    private static final String TAG = "EditSalaryActivity";
    private TextView tvSueldoActual;
    private EditText etNuevoSueldo;
    private Button btnGuardar, btnCancelar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_salary);

        dbHelper = new DatabaseHelper(this); // Inicializar DatabaseHelper

        initViews();
        loadCurrentSalary();
        setupListeners();
    }

    private void initViews() {
        tvSueldoActual = findViewById(R.id.tv_sueldo_actual);
        etNuevoSueldo = findViewById(R.id.et_nuevo_sueldo);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }

    private void loadCurrentSalary() {
        DatabaseHelper.UserProfileData userProfile = dbHelper.getUserProfile();
        if (userProfile != null) {
            tvSueldoActual.setText(String.format(java.util.Locale.getDefault(), "Sueldo actual: $%.2f", userProfile.sueldoMensual));
            Log.d(TAG, "Sueldo actual cargado: " + userProfile.sueldoMensual);
        } else {
            tvSueldoActual.setText("Sueldo actual: $0.00");
            Log.e(TAG, "Error al cargar el perfil de usuario para obtener el sueldo actual.");
        }
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarDatos()) {
                guardarNuevoSueldo();
                // finish() se llama dentro de guardarNuevoSueldo si tiene éxito
            }
        });

        btnCancelar.setOnClickListener(v -> finish());
    }

    private boolean validarDatos() {
        String nuevoSueldoStr = etNuevoSueldo.getText().toString().trim();
        if (TextUtils.isEmpty(nuevoSueldoStr)) {
            etNuevoSueldo.setError("Por favor ingresa el nuevo sueldo");
            Toast.makeText(this, "Por favor ingresa el nuevo sueldo", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(nuevoSueldoStr);
        } catch (NumberFormatException e) {
            etNuevoSueldo.setError("Monto inválido");
            Toast.makeText(this, "El nuevo sueldo debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        etNuevoSueldo.setError(null);
        return true;
    }

    private void guardarNuevoSueldo() {
        float nuevoSueldo = Float.parseFloat(etNuevoSueldo.getText().toString().trim());

        // Obtener los valores actuales de egresos y ahorros para no sobrescribirlos
        DatabaseHelper.UserProfileData currentUserProfile = dbHelper.getUserProfile();
        float egresosActuales = 0;
        float ahorrosActuales = 0;
        boolean isProfileSetupComplete = false; // Por defecto

        if (currentUserProfile != null) {
            egresosActuales = currentUserProfile.egresosMensuales;
            ahorrosActuales = currentUserProfile.ahorrosDisponibles;
            isProfileSetupComplete = currentUserProfile.isProfileSetupComplete; // Mantener el estado actual de configuración
        } else {
            Log.w(TAG, "No se pudo obtener el perfil de usuario actual para egresos/ahorros. Se usarán valores por defecto.");
            // Considerar si es un error fatal o si se puede proceder con valores por defecto
            // Si es la primera vez que se configura, profile_setup_complete podría cambiar.
            // Aquí asumimos que si está editando el sueldo, el perfil ya fue configurado al menos una vez.
            // Si no hay perfil, upsertUserProfile debería crear uno con el ID por defecto.
        }

        // El flag de profile_setup_complete debería ser true si ya existe un perfil o se está editando.
        // Si currentUserProfile fue null (raro si está editando sueldo), al menos el sueldo se establecerá.
        boolean profileWasAlreadySetup = (currentUserProfile != null && currentUserProfile.isProfileSetupComplete);

        if (dbHelper.upsertUserProfile(nuevoSueldo, egresosActuales, ahorrosActuales, profileWasAlreadySetup || !TextUtils.isEmpty(etNuevoSueldo.getText().toString()))) {
            Toast.makeText(this, "Sueldo actualizado correctamente", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Sueldo actualizado en la BD a: " + nuevoSueldo);
            finish(); // Regresar a DashboardActivity solo si se guardó correctamente
        } else {
            Toast.makeText(this, "Error al actualizar el sueldo en la base de datos", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error al llamar a upsertUserProfile para actualizar el sueldo.");
        }
    }
}
