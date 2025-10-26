package com.example.oniria;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private Context context;
    private List<DatabaseHelper.ReminderData> reminderList;
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ReminderAdapter(Context context, List<DatabaseHelper.ReminderData> reminderList) {
        this.context = context;
        this.reminderList = reminderList != null ? reminderList : Collections.emptyList();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reminder_item_layout, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        if (reminderList.isEmpty() || position >= reminderList.size()) {
            return; // No hacer nada si la lista está vacía o la posición es inválida
        }
        DatabaseHelper.ReminderData reminder = reminderList.get(position);

        holder.tvTitle.setText(reminder.title);
        holder.tvContent.setText(reminder.content);

        // Formatear la fecha y hora
        try {
            Date reminderDate = new Date(reminder.timeMillis);
            holder.tvDateTime.setText("Fecha y Hora: " + dateTimeFormatter.format(reminderDate));
        } catch (Exception e) {
            holder.tvDateTime.setText("Fecha y Hora: Inválida");
        }

        if (reminder.isPeriodic && reminder.periodicity != null && !reminder.periodicity.equals("Una vez")) {
            holder.tvPeriodicity.setText("Periodicidad: " + reminder.periodicity);
            holder.tvPeriodicity.setVisibility(View.VISIBLE);
        } else {
            holder.tvPeriodicity.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public void updateData(List<DatabaseHelper.ReminderData> newReminderList) {
        this.reminderList.clear();
        if (newReminderList != null) {
            this.reminderList.addAll(newReminderList);
        }
        notifyDataSetChanged(); // Notificar al RecyclerView que los datos han cambiado
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDateTime, tvPeriodicity;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_reminder_item_title);
            tvContent = itemView.findViewById(R.id.tv_reminder_item_content);
            tvDateTime = itemView.findViewById(R.id.tv_reminder_item_datetime);
            tvPeriodicity = itemView.findViewById(R.id.tv_reminder_item_periodicity);
        }
    }
}
