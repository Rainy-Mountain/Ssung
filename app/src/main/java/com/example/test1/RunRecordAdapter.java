package com.example.test1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RunRecordAdapter extends RecyclerView.Adapter<RunRecordAdapter.RunRecordViewHolder> {

    private List<RunRecord> runRecords;

    public RunRecordAdapter(List<RunRecord> runRecords) {
        this.runRecords = runRecords;
    }

    @NonNull
    @Override
    public RunRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run_record, parent, false);
        return new RunRecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RunRecordViewHolder holder, int position) {
        RunRecord record = runRecords.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateStr = sdf.format(record.getTimestamp());

        holder.dateTextView.setText("날짜: " + dateStr);
        holder.distanceTextView.setText("거리: " + String.format("%.2f km", record.getDistance()));
        holder.paceTextView.setText("페이스: " + String.format("%.2f km/h", record.getPace()));
        holder.caloriesTextView.setText("칼로리: " + String.format("%.2f Kcal", record.getCalories()));
        holder.stepsTextView.setText("걸음 수: " + record.getSteps());
    }

    @Override
    public int getItemCount() {
        return runRecords.size();
    }

    public void setRunRecords(List<RunRecord> runRecords) {
        this.runRecords = runRecords;
        notifyDataSetChanged();
    }

    static class RunRecordViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, distanceTextView, paceTextView, caloriesTextView, stepsTextView;

        public RunRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            paceTextView = itemView.findViewById(R.id.paceTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            stepsTextView = itemView.findViewById(R.id.stepsTextView);
        }
    }
}
