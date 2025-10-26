package com.example.oniria;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class ReminderNotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_CONTENT = "notification_content";

    // Nuevas constantes para los extras (deben coincidir con las usadas en CalendarReminderActivity)
    public static final String EXTRA_REMINDER_ID = "alarm_request_code"; // El ID de la BD, usado como requestCode
    public static final String EXTRA_IS_PERIODIC = "esPeriodico";
    public static final String EXTRA_PERIODICITY = "periodicidad";
    public static final String EXTRA_ORIGINAL_TIME_MILLIS = "timeInMillisOriginal";

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NOTIFICATION_CONTENT);
        long reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1); // Llega como int, pero es long en DB
        boolean isPeriodic = intent.getBooleanExtra(EXTRA_IS_PERIODIC, false);
        String periodicity = intent.getStringExtra(EXTRA_PERIODICITY);
        long originalTimeMillis = intent.getLongExtra(EXTRA_ORIGINAL_TIME_MILLIS, 0);

        if (reminderId == -1) {
            Log.e(TAG, "ID de recordatorio inválido recibido.");
            return;
        }

        // Intent para abrir la app al hacer clic en la notificación
        // CORREGIDO: Cambiado MainActivity.class a DashboardActivity.class
        Intent mainActivityIntent = new Intent(context, DashboardActivity.class); 
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntentActivity = PendingIntent.getActivity(
                context,
                (int) reminderId, // Usar el reminderId como requestCode único
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CalendarReminderActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.onirialogo) 
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntentActivity)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permiso de notificación no concedido al momento de recibir la alarma.");
                return;
            }
        }
        notificationManager.notify((int) reminderId, builder.build()); 
        Log.d(TAG, "Notificación mostrada para el recordatorio ID: " + reminderId);

        if (isPeriodic && periodicity != null && !periodicity.equals("Una vez") && originalTimeMillis > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(originalTimeMillis);

            boolean rescheduled = true;
            switch (periodicity) {
                case "Diario":
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case "Semanal":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "Mensual":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case "Anual":
                    calendar.add(Calendar.YEAR, 1);
                    break;
                default:
                    Log.w(TAG, "Periodicidad desconocida: " + periodicity);
                    rescheduled = false;
                    break;
            }

            long nextTimeInMillis = calendar.getTimeInMillis();

            if (rescheduled && nextTimeInMillis > System.currentTimeMillis()) {
                boolean dbUpdated = dbHelper.updateReminderTime(reminderId, nextTimeInMillis, nextTimeInMillis);

                if (dbUpdated) {
                    Intent rescheduleIntent = new Intent(context, ReminderNotificationReceiver.class);
                    rescheduleIntent.putExtra(NOTIFICATION_TITLE, title);
                    rescheduleIntent.putExtra(NOTIFICATION_CONTENT, content);
                    rescheduleIntent.putExtra(EXTRA_REMINDER_ID, (int) reminderId);
                    rescheduleIntent.putExtra(EXTRA_IS_PERIODIC, true);
                    rescheduleIntent.putExtra(EXTRA_PERIODICITY, periodicity);
                    rescheduleIntent.putExtra(EXTRA_ORIGINAL_TIME_MILLIS, nextTimeInMillis); // El nuevo "original"

                    PendingIntent reschedulePendingIntent = PendingIntent.getBroadcast(
                            context,
                            (int) reminderId, 
                            rescheduleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        try {
                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                Log.w(TAG, "No se puede reprogramar alarma exacta, permiso no concedido o revocado.");
                             } else {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTimeInMillis, reschedulePendingIntent);
                                Log.d(TAG, "Recordatorio periódico ID: " + reminderId + " reprogramado para: " + nextTimeInMillis);
                             }
                        } catch (SecurityException se) {
                            Log.e(TAG, "Error de seguridad al reprogramar alarma exacta.", se);
                        }
                    } else {
                        Log.e(TAG, "AlarmManager fue nulo al intentar reprogramar.");
                    }
                } else {
                    Log.e(TAG, "Error al actualizar la hora del recordatorio en la BD para ID: " + reminderId + ". No se reprogramará.");
                }
            } else if (rescheduled) {
                 Log.w(TAG, "La próxima hora calculada para el recordatorio ID: " + reminderId + " es en el pasado. No se reprogramará.");
                 dbHelper.deleteReminder(reminderId);
                 Log.d(TAG, "Recordatorio periódico ID: " + reminderId + " eliminado porque la próxima hora era pasada.");
            }
        } else {
            Log.d(TAG, "Recordatorio ID: " + reminderId + " no es periódico o datos inválidos. Eliminando de la BD.");
            boolean deleted = dbHelper.deleteReminder(reminderId);
            if (deleted) {
                Log.d(TAG, "Recordatorio ID: " + reminderId + " eliminado exitosamente de la BD.");
            } else {
                Log.e(TAG, "Error al eliminar el recordatorio ID: " + reminderId + " de la BD.");
            }
        }
    }
}
