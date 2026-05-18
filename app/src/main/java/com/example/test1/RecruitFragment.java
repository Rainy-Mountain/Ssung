package com.example.test1;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecruitFragment extends Fragment {

    // Firestore 인스턴스
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    // UI 요소들
    private TextView editName;
    private TextView editLocation;
    private TextView editDate;
    private TextView editTime;
    private EditText editCourse;
    private EditText editMaxParticipants;
    private EditText editPace;
    private LatLng location;
    private EditText edtRoomName;

    public RecruitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // LocationSelectFragment로부터 전달된 LatLng 받기
        if (getArguments() != null) {
            location = getArguments().getParcelable("location");
            Log.d("location", String.valueOf(location));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_recruit, container, false);

        // UI 요소 초기화
        editName=rootView.findViewById(R.id.edit_name);
        editLocation = rootView.findViewById(R.id.edit_location);
        editCourse = rootView.findViewById(R.id.edit_course);
        editMaxParticipants = rootView.findViewById(R.id.edit_max_participants);
        editPace = rootView.findViewById(R.id.edit_pace);
        editDate = rootView.findViewById(R.id.edit_date);  // 결과를 표시할 TextView
        editTime = rootView.findViewById(R.id.edit_time);

        if (location != null) {
            double latitude = location.latitude;
            double longitude = location.longitude;
            String locaton = latitude + ", " + longitude;
            editLocation.setText(locaton);
            // latitude, longitude로 작업 수행
        }

        rootView.findViewById(R.id.edit_date).setOnClickListener(v -> {
            // DatePickerDialog 띄우기
            showDatePickerDialog();
        });

        rootView.findViewById(R.id.edit_time).setOnClickListener(v -> {
            // DatePickerDialog 띄우기
            showTimePickerDialog();
        });

        // 버튼 클릭 리스너 설정
        rootView.findViewById(R.id.btn_recruit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveRunningSession();
                openChatRoomList("모집자");
//                Fragment1 fragment1 = new Fragment1();
//                getParentFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.containers, fragment1)
//                        .commit();
            }
        });


        rootView.findViewById(R.id.edit_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationSelectFragment locationSelectFragment = new LocationSelectFragment();

                // FragmentTransaction을 사용하여 프래그먼트 전환
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.containers, locationSelectFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return rootView;
    }

//    private void moveToFragment1() {
//        Fragment1 fragment1 = new Fragment1();
//        getParentFragmentManager()
//                .beginTransaction()
//                .replace(R.id.containers, fragment1)
//                .commit();
//    }

    private void openChatRoomList(String userType){
        String roomName = editName.getText().toString();
        String userId = mAuth.getInstance().getCurrentUser().getUid(); // 로그인된 사용자 ID

        // Firestore에 데이터 저장
        firestore = FirebaseFirestore.getInstance();
        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("name", roomName); //필드명
        chatRoom.put("userType", userType);
        chatRoom.put("participants", new ArrayList<>(List.of(userId))); // 현재 사용자 추가

        firestore.collection("chatRooms")
                .add(chatRoom)
                .addOnSuccessListener(documentReference -> {
//                    onSuccess.onSuccess(null);
                    Intent intent = new Intent(getActivity(), ChatRoomList.class);
                    intent.putExtra("userType", userType);
                    intent.putExtra("roomName", roomName);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "채팅방 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePickerDialog() {
        // 현재 날짜를 가져옴
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // 날짜 선택 후 처리
                    String selectedDate = year1 + "/" + (monthOfYear + 1) + "/" + dayOfMonth;
                    editDate.setText(selectedDate);  // TextView에 날짜 표시
                }, year, month, day);

        // 다이얼로그 보여주기
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        // 현재 시간을 가져옴
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // 24시간 형식
        int minute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog 생성
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute1) -> {
                    // 시간 선택 후 처리
                    String selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                    editTime.setText(selectedTime);  // TextView에 시간 표시
                }, hour, minute, false);  // true는 24시간 형식

        // 다이얼로그 보여주기
        timePickerDialog.show();
    }


    private void saveRunningSession() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 로그인 상태 확인
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return; // 로그인 상태가 아니면 함수 종료
        }

        String userId = mAuth.getCurrentUser().getUid(); // 로그인된 사용자 ID

        // 입력 필드에서 값 가져오기
        String name=editName.getText().toString();
        String locationText = editLocation.getText().toString();
        String course = editCourse.getText().toString();
        String pace = editPace.getText().toString();
        String date = editDate.getText().toString();
        String time = editTime.getText().toString();

        int maxParticipants;
        try {
            maxParticipants = Integer.parseInt(editMaxParticipants.getText().toString());
        } catch (NumberFormatException e) {
            maxParticipants = 0;
        }

        // 유효성 검사
        if (name.isEmpty()||locationText.isEmpty() || course.isEmpty() || maxParticipants <= 0 || pace.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getActivity(), "모든 필드를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // location 문자열을 위도와 경도로 분리
        String[] latLng = locationText.split(",");
        double latitude = Double.parseDouble(latLng[0].trim());
        double longitude = Double.parseDouble(latLng[1].trim());

        // GeoPoint 생성
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        // Firestore에 저장할 데이터 모델 생성
        RunningSession session = new RunningSession(name, geoPoint, date, time, course, maxParticipants, pace);

        // Firestore에 데이터 저장
        db.collection("RunningSessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "러닝팟 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show();
                    clearFields(); // 입력 필드 초기화
                    //onSuccess.onSuccess(null); // 작업 성공 시 콜백 실행
                    Fragment1 fragment1 = new Fragment1();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.containers, fragment1)  // R.id.container는 프래그먼트를 표시할 컨테이너 뷰 ID
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        editName.setText("");
        editLocation.setText("");
        editDate.setText("");
        editTime.setText("");
        editCourse.setText("");
        editMaxParticipants.setText("");
        editPace.setText("");
    }
}

