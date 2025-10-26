package com.example.oniria;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewRemindersActivity extends AppCompatActivity {

    private RecyclerView rvRemindersList;
    private TextView tvNoReminders;
    private DatabaseHelper dbHelper;
    private ReminderAdapter reminderAdapter; // Lo crearemos después

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        rvRemindersList = findViewById(R.id.rv_reminders_list);
        tvNoReminders = findViewById(R.id.tv_no_reminders);
        dbHelper = new DatabaseHelper(this);

        setupRecyclerView();
        loadReminders();
    }

    private void setupRecyclerView() {
        rvRemindersList.setLayoutManager(new LinearLayoutManager(this));
        // El adapter se creará y asignará en loadReminders o después
    }

    private void loadReminders() {
        List<DatabaseHelper.ReminderData> reminders = dbHelper.getAllReminders();

        if (reminders == null || reminders.isEmpty()) {
            tvNoReminders.setVisibility(View.VISIBLE);
            rvRemindersList.setVisibility(View.GONE);
        } else {
            tvNoReminders.setVisibility(View.GONE);
            rvRemindersList.setVisibility(View.VISIBLE);
            
            if (reminderAdapter == null) {
                reminderAdapter = new ReminderAdapter(this, reminders);
                rvRemindersList.setAdapter(reminderAdapter);
            } else {
                // Si el adapter ya existe, actualiza su lista de datos
                // Esto sería útil si implementamos la opción de eliminar, por ejemplo
                reminderAdapter.updateData(reminders); 
            }
        }
    }
    
    // Si quieres que la lista se actualice si regresas a esta activity después de hacer cambios
    // (por ejemplo, después de eliminar un recordatorio desde otra pantalla o si un recordatorio se dispara y se elimina)
    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }
}
