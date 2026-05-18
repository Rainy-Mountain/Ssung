package com.example.test1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunningSessionDetailsActivity extends AppCompatActivity {
    private TextView sessionNameTextView, sessionCourseTextView, sessionDateTextView, sessionTimeTextView, sessionPaceTextView, sessionMaxParticipantsTextView;
    private Button participateButton, cancelButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_session_details);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 레이아웃의 요소 연결
        sessionNameTextView=findViewById(R.id.sessionNameTextView);
        sessionCourseTextView = findViewById(R.id.sessionCourseTextView);
        sessionDateTextView = findViewById(R.id.sessionDateTextView);
        sessionTimeTextView = findViewById(R.id.sessionTimeTextView);
        sessionPaceTextView = findViewById(R.id.sessionPaceTextView);
        sessionMaxParticipantsTextView = findViewById(R.id.sessionMaxParticipantsTextView);
        participateButton = findViewById(R.id.participateButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Intent로 전달받은 좌표 값 가져오기
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);

        if (latitude != 0.0 && longitude != 0.0) {
            loadRunningSessionDetails(latitude, longitude);
        } else {
            Toast.makeText(this, "Invalid coordinates received.", Toast.LENGTH_SHORT).show();
        }

        // 참여 버튼 클릭 리스너
        participateButton.setOnClickListener(v -> {
            // 참여 기능 추가 (예: Firestore에 참가 정보 저장)
/*
이부분에 채팅방 화면으로 전환 하는 코드 집어넣기.
 */
            openChatRoomList("참여자");

        });

        // 취소 버튼 클릭 리스너
        cancelButton.setOnClickListener(v -> {
            // 현재 액티비티 종료
            finish();
        });
    }
    private void openChatRoomList(String userType){
        String roomName = sessionNameTextView.getText().toString();
        String userId = mAuth.getInstance().getCurrentUser().getUid(); // 로그인된 사용자 ID

        // Firestore에 데이터 저장
        db = FirebaseFirestore.getInstance();
        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("name", roomName); //필드명
        chatRoom.put("userType", userType);
        chatRoom.put("participants", new ArrayList<>(List.of(userId))); // 현재 사용자 추가

        db.collection("chatRooms")
                .add(chatRoom)
                .addOnSuccessListener(documentReference -> {
                    Intent intent = new Intent(this, ChatRoomList.class);
                    intent.putExtra("userType", userType);
                    intent.putExtra("roomName", roomName);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "채팅방 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void loadRunningSessionDetails(double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        db.collection("RunningSessions")
                .whereEqualTo("location", geoPoint)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Firestore 데이터 가져오기
                            String name=document.getString("name");
                            String course = document.getString("course");
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String pace = document.getString("pace");
                            long maxParticipants = document.getLong("maxParticipants");

                            // UI에 표시
                            sessionNameTextView.setText(name);
                            sessionCourseTextView.setText("Course: " + course);
                            sessionDateTextView.setText("Date: " + date);
                            sessionTimeTextView.setText("Time: " + time);
                            sessionPaceTextView.setText("Pace: " + pace);
                            sessionMaxParticipantsTextView.setText("Max Participants: " + maxParticipants);
                        }
                    } else {
                        Toast.makeText(this, "No running session found for this location.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
