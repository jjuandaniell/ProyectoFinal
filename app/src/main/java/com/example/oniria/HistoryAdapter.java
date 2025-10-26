package com.example.oniria;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<DatabaseHelper.TransactionData> transactionList;
    private Context context;
    private SimpleDateFormat dateFormat;
    private DecimalFormat currencyFormat;

    public HistoryAdapter(Context context, List<DatabaseHelper.TransactionData> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Formato para asegurar que se use Q y no dependa del Locale
        this.currencyFormat = new DecimalFormat("'Q'#,##0.00");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.TransactionData transaction = transactionList.get(position);

        holder.descriptionTextView.setText(transaction.description);
        holder.amountTextView.setText(currencyFormat.format(transaction.amount));
        holder.dateTextView.setText(dateFormat.format(new Date(transaction.dateMillis)));

        if (transaction.category != null && !transaction.category.isEmpty()) {
            holder.categoryTextView.setText("Categoría: " + transaction.category);
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }

        if ("income".equalsIgnoreCase(transaction.type)) {
            holder.typeIndicatorView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.amountTextView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else { // expense
            holder.typeIndicatorView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.amountTextView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView categoryTextView;
        TextView amountTextView;
        TextView dateTextView;
        View typeIndicatorView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.text_view_transaction_description);
            categoryTextView = itemView.findViewById(R.id.text_view_transaction_category);
            amountTextView = itemView.findViewById(R.id.text_view_transaction_amount);
            dateTextView = itemView.findViewById(R.id.text_view_transaction_date);
            typeIndicatorView = itemView.findViewById(R.id.view_type_indicator);
        }
    }

    public void updateData(List<DatabaseHelper.TransactionData> newTransactions) {
        this.transactionList.clear();
        this.transactionList.addAll(newTransactions);
        notifyDataSetChanged();
    }
}
