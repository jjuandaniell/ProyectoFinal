package com.example.oniria;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log; 

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView tvBienvenida, tvSaldoActual, tvProximoIngreso;
    private CardView cardVerGraficos, cardAgregarIngreso, cardAgregarEgreso,
                     cardGestionarMetas, cardEditarSueldo, cardRecordatoriosCalendar,
                     cardVerMisRecordatorios, cardVerHistorial, cardAsistenteIA,
                     cardCapturarRecibo;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvBienvenida = findViewById(R.id.tv_bienvenida);
        tvSaldoActual = findViewById(R.id.tv_saldo_actual);
        tvProximoIngreso = findViewById(R.id.tv_proximo_ingreso); 

        cardVerGraficos = findViewById(R.id.card_ver_graficos);
        cardAgregarIngreso = findViewById(R.id.card_agregar_ingreso);
        cardAgregarEgreso = findViewById(R.id.card_agregar_egreso);
        cardGestionarMetas = findViewById(R.id.card_gestionar_metas);
        cardEditarSueldo = findViewById(R.id.card_editar_sueldo);
        cardRecordatoriosCalendar = findViewById(R.id.card_recordatorios_calendar);
        cardVerMisRecordatorios = findViewById(R.id.card_ver_mis_recordatorios);
        cardVerHistorial = findViewById(R.id.card_ver_historial);
        cardAsistenteIA = findViewById(R.id.card_asistente_ia);
        cardCapturarRecibo = findViewById(R.id.card_capturar_recibo);
    }

    private void loadUserData() {
        Log.d(TAG, "Cargando datos del usuario desde SQLite...");
        DatabaseHelper.UserProfileData userProfile = dbHelper.getUserProfile();

        if (userProfile != null) {
            tvBienvenida.setText("¡Bienvenido de vuelta!"); 
            tvSaldoActual.setText(String.format(java.util.Locale.getDefault(), "Saldo disponible: Q%.2f", userProfile.ahorrosDisponibles));
            tvProximoIngreso.setText(String.format(java.util.Locale.getDefault(), "Sueldo base mensual: Q%.2f", userProfile.sueldoMensual));
            Log.d(TAG, "Datos cargados: Sueldo=" + userProfile.sueldoMensual + ", Egresos=" + userProfile.egresosMensuales + ", Ahorros=" + userProfile.ahorrosDisponibles);
        } else {
            Log.e(TAG, "Error: UserProfileData es null.");
            tvBienvenida.setText("Bienvenido");
            tvSaldoActual.setText("Saldo disponible: Q0.00");
            tvProximoIngreso.setText("Sueldo base mensual: Q0.00");
        }
    }

    private void setupListeners() {
        cardVerGraficos.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChartsActivity.class);
            startActivity(intent);
        });

        cardAgregarIngreso.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddIncomeActivity.class);
            startActivity(intent);
        });

        cardAgregarEgreso.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        cardGestionarMetas.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ManageGoalsActivity.class);
            startActivity(intent);
        });

        cardEditarSueldo.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, EditSalaryActivity.class);
            startActivity(intent);
        });

        cardRecordatoriosCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CalendarReminderActivity.class);
            startActivity(intent);
        });

        if (cardVerMisRecordatorios != null) {
            cardVerMisRecordatorios.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Ver Mis Recordatorios' clickeado.");
                Intent intent = new Intent(DashboardActivity.this, ViewRemindersActivity.class); 
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "La CardView card_ver_mis_recordatorios no se encontró en el layout.");
        }

        if (cardVerHistorial != null) {
            cardVerHistorial.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Historial' clickeado.");
                Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "La CardView card_ver_historial no se encontró en el layout.");
        }

        if (cardAsistenteIA != null) {
            cardAsistenteIA.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Asistente IA' clickeado.");
                Intent intent = new Intent(DashboardActivity.this, AIAssistantActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "La CardView card_asistente_ia no se encontró en el layout.");
        }

        if (cardCapturarRecibo != null) {
            cardCapturarRecibo.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Capturar Recibo' clickeado.");
                Intent intent = new Intent(DashboardActivity.this, ReceiptCaptureActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "La CardView card_capturar_recibo no se encontró en el layout.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Actualizando datos del usuario.");
        loadUserData();
    }
}
