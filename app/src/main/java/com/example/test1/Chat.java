package com.example.test1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView chatRecyclerView;
    private EditText edtMessage;
    private Button btnSend;

    private FirebaseFirestore firestore;
    private String roomName;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        edtMessage = findViewById(R.id.edt_message);
        btnSend = findViewById(R.id.btn_send);

        // Firestore 초기화 및 방 이름 가져오기
        firestore = FirebaseFirestore.getInstance();
        roomName = getIntent().getStringExtra("roomName");

        setTitle(roomName);

        // RecyclerView 설정
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Firestore에서 메시지 실시간 업데이트
        listenForMessages();

        // 메시지 전송 버튼 클릭 리스너
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void listenForMessages() {
        firestore.collection("chatRooms")
                .document(roomName)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(Chat.this, "메시지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message message = change.getDocument().toObject(Message.class);
                            Log.d("Chat", "Loaded message: " + message.getText() + ", userId: " + message.getUserId());
                            messageList.add(message);
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();

        if (text.isEmpty()) {
            edtMessage.setError("메시지를 입력해주세요.");
            return;
        }

        String userUid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");

                        Map<String, Object> message = new HashMap<>();
                        message.put("text", text);
                        message.put("sender", nickname); // 필요시 사용자 정보 추가
                        message.put("userId", userUid);
                        message.put("timestamp", System.currentTimeMillis());

                        firestore.collection("chatRooms")
                                .document(roomName)
                                .collection("messages")
                                .add(message)
                                .addOnSuccessListener(documentReference -> edtMessage.setText(""))
                                .addOnFailureListener(e -> Toast.makeText(Chat.this, "메시지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(Chat.this, "닉네임 못불러옴", Toast.LENGTH_SHORT);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Chat.this, "닉네임 불러오기 오류", Toast.LENGTH_SHORT);
                });

    }
}
