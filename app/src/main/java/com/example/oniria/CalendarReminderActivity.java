package com.example.oniria;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarReminderActivity extends AppCompatActivity {

    private EditText etTipoPago, etMonto, etDescripcion;
    private TextView tvFechaSeleccionada, tvHoraSeleccionada;
    private Spinner spinnerPeriodicidad;
    private CheckBox cbEsPeriodico;
    private Button btnSeleccionarFecha, btnSeleccionarHora, btnCrearRecordatorio;
    private ProgressBar progressBar; 

    private Calendar fechaSeleccionadaCal;
    private String[] opcionesPeriodicidad = {"Una vez", "Diario", "Semanal", "Mensual", "Anual"};

    public static final String NOTIFICATION_CHANNEL_ID = "oniria_reminder_channel";
    public static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    // No se necesita REQUEST_CODE_SCHEDULE_EXACT_ALARM porque la configuración se abre con startActivity

    private DatabaseHelper dbHelper;
    private static final String TAG = "CalendarReminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_reminder);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        createNotificationChannel();

        fechaSeleccionadaCal = Calendar.getInstance();

        // Solicitar permiso POST_NOTIFICATIONS si es necesario (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    private void initViews() {
        etTipoPago = findViewById(R.id.et_tipo_pago);
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        tvFechaSeleccionada = findViewById(R.id.tv_fecha_seleccionada);
        tvHoraSeleccionada = findViewById(R.id.tv_hora_seleccionada);
        spinnerPeriodicidad = findViewById(R.id.spinner_periodicidad);
        cbEsPeriodico = findViewById(R.id.cb_es_periodico);
        btnSeleccionarFecha = findViewById(R.id.btn_seleccionar_fecha);
        btnSeleccionarHora = findViewById(R.id.btn_seleccionar_hora);
        btnCrearRecordatorio = findViewById(R.id.btn_crear_recordatorio);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesPeriodicidad);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodicidad.setAdapter(adapter);
        spinnerPeriodicidad.setVisibility(View.GONE); 
        spinnerPeriodicidad.setSelection(0); 
    }

    private void setupListeners() {
        btnSeleccionarFecha.setOnClickListener(v -> mostrarDatePicker());
        btnSeleccionarHora.setOnClickListener(v -> mostrarTimePicker());
        btnCrearRecordatorio.setOnClickListener(v -> programarRecordatorioConDB());

        cbEsPeriodico.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spinnerPeriodicidad.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                spinnerPeriodicidad.setSelection(0); 
            }
        });
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    fechaSeleccionadaCal.set(Calendar.YEAR, year);
                    fechaSeleccionadaCal.set(Calendar.MONTH, month);
                    fechaSeleccionadaCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    actualizarTvFecha();
                },
                fechaSeleccionadaCal.get(Calendar.YEAR),
                fechaSeleccionadaCal.get(Calendar.MONTH),
                fechaSeleccionadaCal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void actualizarTvFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvFechaSeleccionada.setText(sdf.format(fechaSeleccionadaCal.getTime()));
    }

    private void mostrarTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    fechaSeleccionadaCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    fechaSeleccionadaCal.set(Calendar.MINUTE, minute);
                    fechaSeleccionadaCal.set(Calendar.SECOND, 0);
                    fechaSeleccionadaCal.set(Calendar.MILLISECOND, 0);
                    actualizarTvHora();
                },
                fechaSeleccionadaCal.get(Calendar.HOUR_OF_DAY),
                fechaSeleccionadaCal.get(Calendar.MINUTE),
                true 
        );
        timePickerDialog.show();
    }

    private void actualizarTvHora() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvHoraSeleccionada.setText(sdf.format(fechaSeleccionadaCal.getTime()));
    }

    private void programarRecordatorioConDB() {
        if (!validarDatos()) {
            return;
        }

        // Verificar permiso para ALARMAS EXACTAS (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("Permiso Necesario")
                    .setMessage("Para programar recordatorios precisos, Oniria necesita un permiso especial. Por favor, actívelo en la configuración del sistema y luego intente crear el recordatorio de nuevo.")
                    .setPositiveButton("Abrir Configuración", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        // Opcional: intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        Toast.makeText(this, "No se pueden programar alarmas exactas sin el permiso.", Toast.LENGTH_LONG).show();
                    })
                    .show();
                return; // Detener si el permiso no está concedido
            }
        }

        String tipoPago = etTipoPago.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        String descripcion = TextUtils.isEmpty(etDescripcion.getText().toString().trim()) ? "Sin descripción" : etDescripcion.getText().toString().trim();
        boolean esPeriodico = cbEsPeriodico.isChecked();
        String periodicidadSeleccionada = opcionesPeriodicidad[spinnerPeriodicidad.getSelectedItemPosition()];
        long timeInMillis = fechaSeleccionadaCal.getTimeInMillis();
        long originalTimeInMillis = timeInMillis;

        if (!esPeriodico) {
            periodicidadSeleccionada = "Una vez";
        }

        String tituloNotificacion = "Recordatorio de Pago: " + tipoPago;
        String contenidoNotificacion = "Monto: $" + montoStr + ". " + descripcion;

        long reminderId = dbHelper.addReminder(tituloNotificacion, contenidoNotificacion, timeInMillis, esPeriodico, periodicidadSeleccionada, originalTimeInMillis);

        if (reminderId == -1) {
            Toast.makeText(this, "Error al guardar el recordatorio en la base de datos.", Toast.LENGTH_LONG).show();
            return;
        }

        scheduleNotification(timeInMillis, tituloNotificacion, contenidoNotificacion, (int) reminderId, esPeriodico, periodicidadSeleccionada, originalTimeInMillis);

        Toast.makeText(this, "Recordatorio guardado y notificación programada.", Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean validarDatos() {
        if (TextUtils.isEmpty(etTipoPago.getText().toString().trim())) {
            etTipoPago.setError("Ingresa el tipo de pago");
            Toast.makeText(this, "Ingresa el tipo de pago", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etMonto.getText().toString().trim())) {
            etMonto.setError("Ingresa el monto");
            Toast.makeText(this, "Ingresa el monto", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Float.parseFloat(etMonto.getText().toString().trim());
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            Toast.makeText(this, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (tvFechaSeleccionada.getText().toString().equalsIgnoreCase("Seleccionar fecha") || TextUtils.isEmpty(tvFechaSeleccionada.getText())) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvHoraSeleccionada.getText().toString().equalsIgnoreCase("Seleccionar hora") || TextUtils.isEmpty(tvHoraSeleccionada.getText())) {
            Toast.makeText(this, "Selecciona una hora", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        Calendar ahora = Calendar.getInstance();
        if (fechaSeleccionadaCal.getTimeInMillis() <= ahora.getTimeInMillis()) {
            Toast.makeText(this, "La fecha y hora del recordatorio deben ser futuras.", Toast.LENGTH_SHORT).show();
            return false;
        }
        etTipoPago.setError(null);
        etMonto.setError(null);
        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "OniriaReminderChannel";
            String description = "Channel for Oniria Reminder Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH; 
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleNotification(long timeInMillis, String title, String content, int alarmRequestCode, boolean esPeriodico, String periodicidad, long originalTimeMillis) {
        Intent notificationIntent = new Intent(this, ReminderNotificationReceiver.class);
        notificationIntent.putExtra(ReminderNotificationReceiver.NOTIFICATION_TITLE, title);
        notificationIntent.putExtra(ReminderNotificationReceiver.NOTIFICATION_CONTENT, content);
        notificationIntent.putExtra(ReminderNotificationReceiver.EXTRA_REMINDER_ID, alarmRequestCode); 
        notificationIntent.putExtra(ReminderNotificationReceiver.EXTRA_IS_PERIODIC, esPeriodico);
        notificationIntent.putExtra(ReminderNotificationReceiver.EXTRA_PERIODICITY, periodicidad);
        notificationIntent.putExtra(ReminderNotificationReceiver.EXTRA_ORIGINAL_TIME_MILLIS, originalTimeMillis);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmRequestCode, 
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                    } catch (SecurityException se){
                        Log.e(TAG, "SecurityException al programar alarma exacta.", se);
                        Toast.makeText(this, "Error de seguridad al programar la alarma.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Este caso no debería ocurrir si la verificación en programarRecordatorioConDB() funciona
                    Log.w(TAG, "scheduleNotification: Intento de programar alarma exacta sin permiso (API S+).");
                    Toast.makeText(this, "No se pudo programar la alarma. Permiso de alarma exacta no concedido.", Toast.LENGTH_LONG).show();
                }
            } else {
                 // Para APIs < S, no se necesita canScheduleExactAlarms()
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } catch (SecurityException se){ // Aunque menos común aquí, es buena práctica
                     Log.e(TAG, "SecurityException al programar alarma exacta (API < S).", se);
                     Toast.makeText(this, "Error de seguridad al programar la alarma.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, "No se pudo acceder al servicio de alarmas.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificación concedido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de notificación denegado. No se podrán mostrar recordatorios.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
