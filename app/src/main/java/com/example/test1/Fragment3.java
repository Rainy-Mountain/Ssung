package com.example.test1;

import static android.content.Context.MODE_PRIVATE;
import static android.speech.tts.TextToSpeech.ERROR;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment3 extends Fragment implements SensorEventListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    SensorManager sensorManager;
    Sensor stepDetectorSensor;
    TextView stepCountView, timeTextView, calorieView;
    Button startTrackingButton, stopTrackingButton;

    int totalSteps = 0;
    int previousSteps = 0;
    private TextToSpeech tts;

    boolean isTrackingSteps = false;
    Handler timeUpdateHandler = new Handler();
    long trackingStartTime = 0;

    private Intent mapServiceIntent;
    private boolean isTracking = false;
    private Timer stepCheckTimer;

    Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if(isTrackingSteps) {
                long elapsedTime = System.currentTimeMillis() - trackingStartTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) ((elapsedTime / (1000 * 60)) % 60);
                int hours = (int) (elapsedTime / (1000 * 60 * 60));
                timeTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                timeUpdateHandler.postDelayed(this, 1000);
            }
        }
    };

    public Fragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment3.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment3 newInstance(String param1, String param2) {
        Fragment3 fragment = new Fragment3();
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
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 사용자 활동 데이터 권한 허용
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        stepCountView = view.findViewById(R.id.stepCountView);
        timeTextView = view.findViewById(R.id.timeTextView);
        calorieView = view.findViewById(R.id.calorieView);

        startTrackingButton = view.findViewById(R.id.startTrackingButton);
        stopTrackingButton = view.findViewById(R.id.stopTrackingButton);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mapServiceIntent = new Intent(requireContext(), MapService.class);

        startTrackingButton.setOnClickListener(v -> {
            if (!isTrackingSteps) {
                totalSteps = 0;
                previousSteps = 0;
                stepCountView.setText(String.valueOf(totalSteps));
                timeTextView.setText("00:00:00");
                calorieView.setText("0");

                isTrackingSteps = true;
                trackingStartTime = System.currentTimeMillis();
                sensorManager.registerListener(Fragment3.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
                timeUpdateHandler.post(updateTimeRunnable);

                startTracking();

                // 10초 후 실행하고 5초마다 재실행
                stepCheckTimer = new Timer();
                stepCheckTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkStepRate();
                    }
                }, 10000, 10000);
            }
        });

        stopTrackingButton.setOnClickListener(v -> {
            if (isTrackingSteps) {
                isTrackingSteps = false;
                sensorManager.unregisterListener(Fragment3.this, stepDetectorSensor);
                timeUpdateHandler.removeCallbacks(updateTimeRunnable);

                stepCheckTimer.cancel();
                stepCheckTimer.purge();
                stopTracking();

                // 걸음 수 파이어스토어에 저장
                saveStepCountToFirestore();
            }
        });

        tts = new TextToSpeech(requireContext(), i -> {
            if (i != ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTrackingSteps) {
            sensorManager.unregisterListener(this, stepDetectorSensor);
            timeUpdateHandler.removeCallbacks(updateTimeRunnable);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (sensorEvent.values[0] == 1.0f) {
                totalSteps++;
                stepCountView.setText(String.valueOf(totalSteps));
                calorieView.setText((totalSteps * 0.04) + "Kcal");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    // 지도 매핑 시작
    private void startTracking() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        requireContext().startService(mapServiceIntent);
        isTracking = true;
        Toast.makeText(requireContext(), "트래킹을 시작합니다!", Toast.LENGTH_SHORT).show();
    }

    // 지도 매핑 종료 및 화면 전환
    private void stopTracking() {
        requireContext().stopService(mapServiceIntent);
        isTracking = false;

        Intent mapIntent = new Intent(requireContext(), MapActivity.class);
        LatLng lastLocation = MapService.getLastKnownLocation();
        mapIntent.putExtra("lastLat", lastLocation.latitude);
        mapIntent.putExtra("lastLng", lastLocation.longitude);

        // 거리 계산
        double distanceInKm = (totalSteps * 0.7) / 1000.0;

        // 시속 변환
        long elapsedTimeMillis = System.currentTimeMillis() - trackingStartTime;
        double elapsedTimeHours = elapsedTimeMillis / (1000.0 * 60.0 * 60.0);

        // 페이스 계산
        double pace = distanceInKm / elapsedTimeHours;

        // 칼로리 계산
        double caloriesBurned = totalSteps * 0.04;

        // SharedPreferences에 데이터 저장
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("tracking_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("distance", (float) distanceInKm);
        editor.putFloat("pace", (float) pace);
        editor.putFloat("calories", (float) caloriesBurned);
        editor.apply();

        saveRunRecordToFirestore(distanceInKm, pace, caloriesBurned, totalSteps);

        startActivity(mapIntent);
    }

    private void checkStepRate() {
        int stepGap = totalSteps - previousSteps;
        previousSteps = totalSteps;

        if (stepGap < 10) {
            requireActivity().runOnUiThread(() -> {
                tts.speak("조금 더 힘내세요", TextToSpeech.QUEUE_FLUSH, null, "TTS");
                Toast.makeText(requireContext(), "조금 더 힘내세요", Toast.LENGTH_LONG).show();
            });
        }
    }

    // 리더보드용 걸음 수 저장
    private void saveStepCountToFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        long previousSteps = 0;
                        if (documentSnapshot.contains("totalSteps")) {
                            previousSteps = documentSnapshot.getLong("totalSteps");
                        }
                        long updatedSteps = previousSteps + totalSteps;

                        db.collection("users").document(userId)
                                .update("totalSteps", updatedSteps)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "걸음 수가 저장되었습니다", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "걸음 수 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        }
    }

    // 히스토리용 파이어베이스 데이터 저장
    private void saveRunRecordToFirestore(double distance, double pace, double calories, int steps) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> runData = new HashMap<>();
            runData.put("timestamp", System.currentTimeMillis());
            runData.put("distance", distance);
            runData.put("pace", pace);
            runData.put("calories", calories);
            runData.put("steps", steps);

            db.collection("users").document(userId)
                    .collection("runs")
                    .add(runData)
                    .addOnSuccessListener(documentReference -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

}