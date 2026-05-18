package com.example.test1;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserHistoryActivity extends AppCompatActivity {

    private RecyclerView runHistoryRecyclerView;
    private RunRecordAdapter adapter;
    private List<RunRecord> runRecords = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        runHistoryRecyclerView = findViewById(R.id.runHistoryRecyclerView);
        runHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RunRecordAdapter(runRecords);
        runHistoryRecyclerView.setAdapter(adapter);

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserRunHistory(userId);
    }

    private void loadUserRunHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("runs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    runRecords.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        RunRecord record = document.toObject(RunRecord.class);
                        if (record != null) {
                            runRecords.add(record);
                        }
                    }
                    adapter.setRunRecords(runRecords);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "기록을 불러오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
