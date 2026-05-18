package com.example.test1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        String userType=getIntent().getStringExtra("userType");
        if(userType==null) {
            Toast.makeText(this, "유효하지 않은 접근입니다.", Toast.LENGTH_SHORT).show();
            finish(); // 잘못된 호출일 경우 액티비티 종료
            return;
        }
        String roomName=getIntent().getStringExtra("roomName");
        if(roomName==null)
            Log.e("chatroomlist", "roomName is null");

        Fragment2 fragment2 = Fragment2.newInstance("defaultUserType", "defaultRoomName"); // 기본값? 설정
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment2)
                .commit();
    }
}