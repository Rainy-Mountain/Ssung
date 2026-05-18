package com.example.test1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment4#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment4 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String currentUserId;

    private ListView leaderboardListView;

    public Fragment4() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment4.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment4 newInstance(String param1, String param2) {
        Fragment4 fragment = new Fragment4();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        leaderboardListView = view.findViewById(R.id.leaderboardListView);

        loadLeaderboardData();
    }

//    private void loadLeaderboardData() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users")
//                .orderBy("totalSteps", com.google.firebase.firestore.Query.Direction.DESCENDING)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Map<String, String>> leaderboardData = new ArrayList<>();
//
//                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        String nickname = document.contains("nickname") ? document.getString("nickname") : "Unknown";
//                        long totalSteps = document.contains("totalSteps") ? document.getLong("totalSteps") : 0;
//                        String userId = document.getId();
//
//                        Map<String, String> entry = new HashMap<>();
//                        entry.put("userId", userId);
//                        entry.put("nickname", nickname);
//                        entry.put("totalSteps", totalSteps + " steps");
//                        leaderboardData.add(entry);
//                    }
//
//                    SimpleAdapter adapter = new SimpleAdapter(
//                            requireContext(),
//                            leaderboardData,
//                            android.R.layout.simple_list_item_2,
//                            new String[]{"nickname", "totalSteps"},
//                            new int[]{android.R.id.text1, android.R.id.text2}
//                    );
//
//                    leaderboardListView.setAdapter(adapter);
//
//                    leaderboardListView.setOnItemClickListener((parent, view, position, id) -> {
//                        Map<String, String> selectedItem = leaderboardData.get(position);
//                        String selectedUserId = selectedItem.get("userId");
//
//                        // UserHistoryActivity로 이동
//                        Intent intent = new Intent(requireContext(), UserHistoryActivity.class);
//                        intent.putExtra("userId", selectedUserId);
//                        startActivity(intent);
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(requireContext(), "리더보드 데이터를 불러오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
private void loadLeaderboardData() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("users")
            .orderBy("totalSteps", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, String>> leaderboardData = new ArrayList<>();

                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String nickname = document.contains("nickname") ? document.getString("nickname") : "Unknown";
                    long totalSteps = document.contains("totalSteps") ? document.getLong("totalSteps") : 0;
                    String userId = document.getId();

                    Map<String, String> entry = new HashMap<>();
                    entry.put("userId", userId);
                    entry.put("nickname", nickname);
                    entry.put("totalSteps", totalSteps + " steps");
                    leaderboardData.add(entry);
                }

                LeaderboardAdapter adapter = new LeaderboardAdapter(requireContext(), leaderboardData, currentUserId);
                leaderboardListView.setAdapter(adapter);

                leaderboardListView.setOnItemClickListener((parent, view, position, id) -> {
                    Map<String, String> selectedItem = leaderboardData.get(position);
                    String selectedUserId = selectedItem.get("userId");

                    Intent intent = new Intent(requireContext(), UserHistoryActivity.class);
                    intent.putExtra("userId", selectedUserId);
                    startActivity(intent);
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "리더보드 데이터를 불러오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}



}