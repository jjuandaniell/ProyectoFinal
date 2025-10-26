package com.example.oniria;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;
import java.util.Locale;

public class ManageGoalsActivity extends AppCompatActivity {

    private LinearLayout layoutMetas;
    private EditText etNuevaMetaNombre, etNuevaMetaPresupuesto; // Nombres usados en la lógica interna
    private Button btnAgregarMeta;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_goals);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadExistingGoals();
        setupListeners();
    }

    private void initViews() {
        layoutMetas = findViewById(R.id.layout_metas);
        // Asignar a las variables internas los EditText del layout XML
        // Usaremos los IDs originales que proporcionaste: et_nueva_meta y et_presupuesto_nueva_meta
        etNuevaMetaNombre = findViewById(R.id.et_nueva_meta); 
        etNuevaMetaPresupuesto = findViewById(R.id.et_presupuesto_nueva_meta);
        btnAgregarMeta = findViewById(R.id.btn_agregar_meta);
    }

    private void loadExistingGoals() {
        layoutMetas.removeAllViews();
        List<DatabaseHelper.FinancialGoalData> goals = dbHelper.getAllFinancialGoals();

        if (goals.isEmpty()) {
            TextView tvNoGoals = new TextView(this);
            tvNoGoals.setText("No hay metas definidas todavía. ¡Añade una!");
            tvNoGoals.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,32,0,0);
            tvNoGoals.setLayoutParams(params);
            layoutMetas.addView(tvNoGoals);
        } else {
            for (DatabaseHelper.FinancialGoalData goal : goals) {
                addGoalCard(goal);
            }
        }
    }

    private void addGoalCard(DatabaseHelper.FinancialGoalData goal) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Usar valores directos para márgenes si R.dimen no está configurado aún
        cardParams.setMargins(16, 16, 16, 16); 
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16); // Valor directo para radio
        cardView.setCardElevation(8); // Valor directo para elevación

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLinearLayout.setPadding(32, 32, 32, 32); 
        mainLinearLayout.setWeightSum(10f);

        LinearLayout textLinearLayout = new LinearLayout(this);
        textLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 7f 
        );
        textLinearLayout.setLayoutParams(textContainerParams);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(goal.name);
        tvNombre.setTextSize(18f); 
        tvNombre.setTextColor(Color.BLACK);

        TextView tvPresupuesto = new TextView(this);
        tvPresupuesto.setText(String.format(Locale.getDefault(), "Presupuesto: $%.2f", goal.budget));
        tvPresupuesto.setTextSize(14f);
        tvPresupuesto.setTextColor(Color.DKGRAY);
        LinearLayout.LayoutParams tvPresupuestoParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPresupuestoParams.setMargins(0,8,0,0);
        tvPresupuesto.setLayoutParams(tvPresupuestoParams);

        textLinearLayout.addView(tvNombre);
        textLinearLayout.addView(tvPresupuesto);

        LinearLayout buttonContainerLayout = new LinearLayout(this);
        buttonContainerLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f
        );
        buttonContainerLayout.setLayoutParams(buttonContainerParams);
        buttonContainerLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        Button btnEditar = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        LinearLayout.LayoutParams btnEditParams = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnEditar.setLayoutParams(btnEditParams);
        btnEditar.setText("Editar");
        btnEditar.setOnClickListener(v -> showEditGoalDialog(goal));
        
        Button btnEliminar = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        LinearLayout.LayoutParams btnDelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnDelParams.leftMargin = 8;
        btnEliminar.setLayoutParams(btnDelParams);
        btnEliminar.setText("Eliminar");
        btnEliminar.setOnClickListener(v -> showDeleteConfirmDialog(goal));
        
        buttonContainerLayout.addView(btnEditar);
        buttonContainerLayout.addView(btnEliminar);

        mainLinearLayout.addView(textLinearLayout);
        mainLinearLayout.addView(buttonContainerLayout);
        cardView.addView(mainLinearLayout);
        layoutMetas.addView(cardView);
    }
    
    private void showEditGoalDialog(final DatabaseHelper.FinancialGoalData goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Meta");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50,24,50,24);

        final EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre de la meta");
        inputNombre.setText(goal.name);
        layout.addView(inputNombre);

        final EditText inputPresupuesto = new EditText(this);
        inputPresupuesto.setHint("Presupuesto ($)");
        inputPresupuesto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputPresupuesto.setText(String.valueOf(goal.budget));
        LinearLayout.LayoutParams paramsPresupuesto = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsPresupuesto.topMargin = 16;
        inputPresupuesto.setLayoutParams(paramsPresupuesto);
        layout.addView(inputPresupuesto);

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            String nuevoPresupuestoStr = inputPresupuesto.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(ManageGoalsActivity.this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nuevoPresupuestoStr.isEmpty()) {
                Toast.makeText(ManageGoalsActivity.this, "El presupuesto no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                float nuevoPresupuesto = Float.parseFloat(nuevoPresupuestoStr);
                if (nuevoPresupuesto <= 0) {
                    Toast.makeText(ManageGoalsActivity.this, "El presupuesto debe ser positivo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dbHelper.updateFinancialGoal(goal.id, nuevoNombre, nuevoPresupuesto)) {
                    Toast.makeText(ManageGoalsActivity.this, "Meta actualizada.", Toast.LENGTH_SHORT).show();
                    loadExistingGoals(); // Recargar
                } else {
                    Toast.makeText(ManageGoalsActivity.this, "Error al actualizar la meta.", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(ManageGoalsActivity.this, "Presupuesto inválido.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteConfirmDialog(DatabaseHelper.FinancialGoalData goal) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Meta")
                .setMessage("¿Estás seguro de que quieres eliminar la meta \"" + goal.name + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (dbHelper.deleteFinancialGoal(goal.id)) {
                        Toast.makeText(ManageGoalsActivity.this, "Meta eliminada", Toast.LENGTH_SHORT).show();
                        loadExistingGoals(); // Recargar la lista
                    } else {
                        Toast.makeText(ManageGoalsActivity.this, "Error al eliminar la meta", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupListeners() {
        btnAgregarMeta.setOnClickListener(v -> {
            if (validarDatosNuevaMeta()) {
                agregarNuevaMeta();
            }
        });
    }

    private boolean validarDatosNuevaMeta() {
        String nombre = etNuevaMetaNombre.getText().toString().trim();
        String presupuestoStr = etNuevaMetaPresupuesto.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNuevaMetaNombre.setError("El nombre no puede estar vacío.");
            // Toast.makeText(this, "Por favor ingresa el nombre de la meta", Toast.LENGTH_SHORT).show(); // Ya se muestra con setError
            return false;
        }
        if (presupuestoStr.isEmpty()) {
            etNuevaMetaPresupuesto.setError("El presupuesto no puede estar vacío.");
            return false;
        }
        try {
            float presupuesto = Float.parseFloat(presupuestoStr);
            if (presupuesto <= 0) {
                etNuevaMetaPresupuesto.setError("El presupuesto debe ser positivo.");
                return false;
            }
        } catch (NumberFormatException e) {
            etNuevaMetaPresupuesto.setError("Presupuesto inválido.");
            return false;
        }
        etNuevaMetaNombre.setError(null); // Limpiar errores si pasa la validación
        etNuevaMetaPresupuesto.setError(null);
        return true;
    }

    private void agregarNuevaMeta() {
        String nombreMeta = etNuevaMetaNombre.getText().toString().trim();
        float presupuestoMeta = Float.parseFloat(etNuevaMetaPresupuesto.getText().toString().trim());

        if (dbHelper.addFinancialGoal(nombreMeta, presupuestoMeta)) {
            Toast.makeText(this, "Meta agregada correctamente", Toast.LENGTH_SHORT).show();
            etNuevaMetaNombre.setText("");
            etNuevaMetaPresupuesto.setText("");
            etNuevaMetaNombre.setError(null); // Limpiar error después de agregar
            etNuevaMetaPresupuesto.setError(null);
            loadExistingGoals(); // Recargar la lista
        } else {
            Toast.makeText(this, "Error al agregar la meta", Toast.LENGTH_SHORT).show();
        }
    }
}
