package com.example.oniria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Verificar si es la primera vez que abre la app
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("first_time", true);

        if (!isFirstTime) {
            // Si ya configuró sus datos, ir directo al dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, UserSetupActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
