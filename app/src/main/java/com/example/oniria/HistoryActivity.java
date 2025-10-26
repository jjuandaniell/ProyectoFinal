package com.example.oniria;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<DatabaseHelper.TransactionData> transactionList;
    private DatabaseHelper dbHelper;
    private TextView textViewEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Historial");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_view_history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Asumiendo que CoordinatorLayout maneja el padding para AppBarLayout,
            // solo necesitamos aplicar padding al contenido del RecyclerView si es necesario.
            // Por ahora, el layout behavior lo maneja bien.
            return insets;
        });

        recyclerViewHistory = findViewById(R.id.recycler_view_history);
        textViewEmptyHistory = findViewById(R.id.text_view_empty_history);
        dbHelper = new DatabaseHelper(this);

        transactionList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, transactionList);

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);

        loadTransactionHistory();
    }

    private void loadTransactionHistory() {
        List<DatabaseHelper.TransactionData> currentMonthTransactions = dbHelper.getCurrentMonthTransactions();
        if (currentMonthTransactions != null && !currentMonthTransactions.isEmpty()) {
            transactionList.clear();
            transactionList.addAll(currentMonthTransactions);
            historyAdapter.notifyDataSetChanged(); // O usar historyAdapter.updateData(currentMonthTransactions);
            recyclerViewHistory.setVisibility(View.VISIBLE);
            textViewEmptyHistory.setVisibility(View.GONE);
        } else {
            recyclerViewHistory.setVisibility(View.GONE);
            textViewEmptyHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar el clic en el botón de atrás de la Toolbar
        if (item.getItemId() == android.R.id.home) {
            finish(); // Cierra la actividad actual y regresa a la anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar el historial en onResume por si hay cambios mientras la actividad estaba pausada
        // aunque para un simple historial del mes actual, onCreate podría ser suficiente.
        // Si se pueden añadir transacciones y volver inmediatamente, es bueno recargar.
        loadTransactionHistory(); 
    }
}
