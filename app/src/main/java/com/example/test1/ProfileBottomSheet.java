package com.example.test1;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileBottomSheet extends BottomSheetDialogFragment {
    private String userId;
    private Context context;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public ProfileBottomSheet(String userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        TextView txtNickname = view.findViewById(R.id.txt_nickname);
        TextView txtRecommendations = view.findViewById(R.id.txt_recommendations);

        Button btnRunningKing = view.findViewById(R.id.btn_running_king);
        Button btnMoodMaker = view.findViewById(R.id.btn_mood_maker);
        Button btnNextRunning = view.findViewById(R.id.btn_next_running);

        // Firestore에서 사용자 정보 가져오기
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nickname = documentSnapshot.getString("nickname");
                txtNickname.setText(nickname);

                Map<String, Long> recommendations = (Map<String, Long>) documentSnapshot.get("recommendations");
                if (recommendations != null) {
                    txtRecommendations.setText("추천 수: 러닝왕(" + recommendations.getOrDefault("러닝왕", 0L) +
                            "), 분위기메이커(" + recommendations.getOrDefault("분위기메이커", 0L) +
                            "), 다음에도 같이 러닝해요(" + recommendations.getOrDefault("다음에도 같이 러닝해요", 0L) + ")");
                }
            }
        });

        // 추천 버튼 동작
        btnRunningKing.setOnClickListener(v -> updateRecommendation(userId, "러닝왕", btnRunningKing));
        btnMoodMaker.setOnClickListener(v -> updateRecommendation(userId, "분위기메이커", btnMoodMaker));
        btnNextRunning.setOnClickListener(v -> updateRecommendation(userId, "다음에도 같이 러닝해요", btnNextRunning));

        return view;
    }

    private void updateRecommendation(String userId, String category, Button button) {
        String currentUserId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 이미 추천했는지 확인
                        List<String> recommendedBy = (List<String>) documentSnapshot.get("recommendedBy");
                        if (recommendedBy != null && recommendedBy.contains(currentUserId)) {
                            Toast.makeText(requireContext(), "이미 추천하셨습니다!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 추천 업데이트
                        firestore.runTransaction(transaction -> {
                            DocumentReference userRef = firestore.collection("users").document(userId);

                            Map<String, Long> recommendations = (Map<String, Long>) transaction.get(userRef).get("recommendations");
                            if (recommendations == null) recommendations = new HashMap<>();

                            long currentCount = recommendations.getOrDefault(category, 0L);
                            recommendations.put(category, currentCount + 1);

                            // recommendedBy 업데이트
                            List<String> updatedRecommendedBy = new ArrayList<>(recommendedBy != null ? recommendedBy : new ArrayList<>());
                            updatedRecommendedBy.add(currentUserId);

                            transaction.update(userRef, "recommendations", recommendations);
                            transaction.update(userRef, "recommendedBy", updatedRecommendedBy);

                            return null;
                        }).addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), category + " 추천 완료!", Toast.LENGTH_SHORT).show();
                            button.setEnabled(false);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "추천 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}
