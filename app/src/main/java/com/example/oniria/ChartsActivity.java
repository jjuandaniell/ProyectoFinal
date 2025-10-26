package com.example.oniria;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChartsActivity extends AppCompatActivity {

    private static final String TAG = "ChartsActivity";
    private DatabaseHelper dbHelper;

    private PieChart gastosDelMesChartVw;
    private PieChart ingresosDelMesChartVw;
    private BarChart ingresosEgresosDelMesChartVw;
    private AnyChartView progresoMetasChartVw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        Log.d(TAG, "onCreate: ChartsActivity iniciada.");

        dbHelper = new DatabaseHelper(this);

        gastosDelMesChartVw = findViewById(R.id.chart_gastos_mes);
        ingresosDelMesChartVw = findViewById(R.id.chart_ingresos_mes);
        ingresosEgresosDelMesChartVw = findViewById(R.id.chart_ingresos_egresos_mes);
        progresoMetasChartVw = findViewById(R.id.chart_progreso_metas);
    }

    private void setupGastosDelMesChart() {
        Log.d(TAG, "setupGastosDelMesChart: Configurando gráfico de GASTOS (MPAndroidChart)...");
        gastosDelMesChartVw.setUsePercentValues(true);
        gastosDelMesChartVw.getDescription().setEnabled(false);
        gastosDelMesChartVw.setExtraOffsets(5, 10, 5, 5);
        gastosDelMesChartVw.setDragDecelerationFrictionCoef(0.95f);
        gastosDelMesChartVw.setDrawHoleEnabled(true);
        gastosDelMesChartVw.setHoleColor(Color.TRANSPARENT);
        gastosDelMesChartVw.setTransparentCircleRadius(61f);
        gastosDelMesChartVw.getLegend().setEnabled(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        List<DataEntry> expensesData = dbHelper.getMonthlyExpensesByCategory(year, month);

        if (expensesData.isEmpty()) {
            yValues.add(new PieEntry(1f, "Sin gastos"));
        } else {
            for (DataEntry entry : expensesData) {
                yValues.add(new PieEntry(Float.parseFloat(String.valueOf(entry.getValue("value"))), (String) entry.getValue("x")));
            }
        }

        PieDataSet dataSet = new PieDataSet(yValues, "Gastos del Mes");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.md_theme_onPrimary));

        gastosDelMesChartVw.setData(data);
        gastosDelMesChartVw.animateY(1000);
        gastosDelMesChartVw.invalidate();
    }

    private void setupIngresosPorCategoriaChart() {
        Log.d(TAG, "setupIngresosPorCategoriaChart: Configurando gráfico de INGRESOS (MPAndroidChart)...");
        ingresosDelMesChartVw.setUsePercentValues(true);
        ingresosDelMesChartVw.getDescription().setEnabled(false);
        ingresosDelMesChartVw.setExtraOffsets(5, 10, 5, 5);
        ingresosDelMesChartVw.setDragDecelerationFrictionCoef(0.95f);
        ingresosDelMesChartVw.setDrawHoleEnabled(true);
        ingresosDelMesChartVw.setHoleColor(Color.TRANSPARENT);
        ingresosDelMesChartVw.setTransparentCircleRadius(61f);
        ingresosDelMesChartVw.getLegend().setEnabled(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        List<DataEntry> incomeData = dbHelper.getMonthlyIncomeByCategory(year, month);

        if (incomeData.isEmpty()) {
            yValues.add(new PieEntry(1f, "Sin ingresos"));
        } else {
            for (DataEntry entry : incomeData) {
                yValues.add(new PieEntry(Float.parseFloat(String.valueOf(entry.getValue("value"))), (String) entry.getValue("x")));
            }
        }

        PieDataSet dataSet = new PieDataSet(yValues, "Ingresos del Mes");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.md_theme_onPrimary));

        ingresosDelMesChartVw.setData(data);
        ingresosDelMesChartVw.animateY(1000);
        ingresosDelMesChartVw.invalidate();
    }

    private void setupIngresosEgresosDelMesChart() {
        Log.d(TAG, "setupIngresosEgresosDelMesChart: Configurando gráfico de INGRESOS/EGRESOS (MPAndroidChart)...");
        ingresosEgresosDelMesChartVw.getDescription().setEnabled(false);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        float totalIngresos = 0;
        List<DataEntry> incomeData = dbHelper.getMonthlyIncomeByCategory(year, month);
        for(DataEntry entry : incomeData) {
            totalIngresos += Float.parseFloat(String.valueOf(entry.getValue("value")));
        }
        
        float totalEgresos = 0;
        List<DataEntry> expensesData = dbHelper.getMonthlyExpensesByCategory(year, month);
        for(DataEntry entry : expensesData) {
            totalEgresos += Float.parseFloat(String.valueOf(entry.getValue("value")));
        }

        ArrayList<BarEntry> values = new ArrayList<>();
        values.add(new BarEntry(0, totalIngresos));
        values.add(new BarEntry(1, totalEgresos));

        BarDataSet set1 = new BarDataSet(values, "Resumen del Mes");
        set1.setColors(new int[]{ContextCompat.getColor(this, R.color.colorIncome), ContextCompat.getColor(this, R.color.colorExpense)});
        set1.setDrawValues(true);
        set1.setValueTextSize(12f);
        set1.setValueTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurface));

        BarData data = new BarData(set1);
        ingresosEgresosDelMesChartVw.setData(data);

        String[] labels = new String[]{"Ingresos", "Egresos"};
        XAxis xAxis = ingresosEgresosDelMesChartVw.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurface));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        ingresosEgresosDelMesChartVw.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurface));
        ingresosEgresosDelMesChartVw.getAxisLeft().setAxisMinimum(0);
        ingresosEgresosDelMesChartVw.getAxisRight().setEnabled(false);
        ingresosEgresosDelMesChartVw.getLegend().setEnabled(false);

        ingresosEgresosDelMesChartVw.setFitBars(true);
        ingresosEgresosDelMesChartVw.animateY(1000);
        ingresosEgresosDelMesChartVw.invalidate();
    }

    private void setupProgresoMetasChart() {
        Log.d(TAG, "setupProgresoMetasChart: Configurando gráfico de progreso de METAS (Columnas)...");
        Cartesian columnMetas = AnyChart.column();
        
        List<DataEntry> data = dbHelper.getFinancialGoalsAsDataEntries(); 
        
        if (data.isEmpty()){
            Log.d(TAG, "setupProgresoMetasChart: No hay datos de metas, añadiendo entrada de placeholder.");
            data.add(new ValueDataEntry("Sin metas definidas", 0)); 
        }

        columnMetas.data(data);
        columnMetas.title("Metas Financieras (Presupuesto Total)");
        columnMetas.yAxis(0).title("Presupuesto Meta (Q)");
        columnMetas.xAxis(0).title("Meta");
        columnMetas.animation(true);

        progresoMetasChartVw.setChart(columnMetas);
        Log.d(TAG, "setupProgresoMetasChart: Gráfico de METAS (Columnas) configurado.");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ChartsActivity, recargando gráficos.");
        setupGastosDelMesChart();
        setupIngresosPorCategoriaChart();
        setupIngresosEgresosDelMesChart();
        setupProgresoMetasChart();
    }
}
