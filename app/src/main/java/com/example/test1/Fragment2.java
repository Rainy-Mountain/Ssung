package com.example.test1;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment2 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private FirebaseFirestore firestore;
    private List<String> chatRooms;

    public Fragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userType Parameter 1.
     * @param roomName Parameter 2.
     * @return A new instance of fragment Fragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment2 newInstance(String userType, String roomName) {
        Fragment2 fragment2 = new Fragment2();
        Bundle args = new Bundle();
        args.putString("userType", userType);
        args.putString("roomName", roomName);
        fragment2.setArguments(args);
        return fragment2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        String userType;
        String roomName;
        if (args != null) {
            userType = args.getString("userType", "defaultUserType"); // 기본값? 설정
            roomName = args.getString("roomName", "defaultRoomName");
        } else {
            userType = "defaultUserType";
            roomName = "defaultRoomName";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);
        // FirebaseFirestore 초기화
        firestore = FirebaseFirestore.getInstance();
        if (firestore == null) {
            Log.e("Fragment2", "FirebaseFirestore initialization failed!");
            return view;
        }


        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if(recyclerView==null)
            Log.e("Fragment2", "Recyclerview is null");

        //채팅방 목록 리스트 초기화
        chatRooms = new ArrayList<>();
        adapter = new ChatRoomAdapter(requireContext(), chatRooms);
        recyclerView.setAdapter(adapter);

        loadChatRooms();

        return view;
    }

    private void loadChatRooms() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID

        firestore.collection("chatRooms")
                .whereArrayContains("participants", userId) // 현재 사용자가 참여한 채팅방만
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatRooms.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        chatRooms.add(document.getString("name")); // Firestore에서 name 필드 가져오기
                    }
                    adapter.notifyDataSetChanged(); // 데이터 갱신
                })
                .addOnFailureListener(e -> Log.e("Fragment2", "Error loading chat rooms: " + e.getMessage()));
    }

}