package com.example.test1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolylineOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private NaverMap naverMap;
    private List<LatLng> coords = new ArrayList<>();
    private PolylineOverlay polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // 경로 데이터 가져오기
        coords = MapService.getRecordedCoords();

        Intent mapIntent = new Intent(this, MapActivity.class);
        LatLng lastLocation = MapService.getLastKnownLocation();
        mapIntent.putExtra("lastLat", lastLocation.latitude);
        mapIntent.putExtra("lastLng", lastLocation.longitude);

        // SharedPreferences에서 데이터 읽기
        SharedPreferences sharedPreferences = getSharedPreferences("tracking_data", MODE_PRIVATE);
        float distance = sharedPreferences.getFloat("distance", 0.0f);
        float pace = sharedPreferences.getFloat("pace", 0.0f);
        float calories = sharedPreferences.getFloat("calories", 0.0f);

        // 하단 정보 뷰 업데이트
        TextView distanceView = findViewById(R.id.distance_view);
        TextView paceView = findViewById(R.id.pace_view);
        TextView calorieView = findViewById(R.id.calorie_view);
        TextView evaluation = findViewById(R.id.evaluation_view);
        distanceView.setText(String.format("키로수: %.2fkm", distance));
        paceView.setText(String.format("페이스: %.2fkm/h", pace));
        calorieView.setText(String.format("칼로리 소모량: %.2fKcal", calories));

        // 평가 항목 텍스트뷰
        String evaluationMessage;
        if (pace < 4.0) {
            evaluationMessage = "페이스가 느립니다. 조금 더 속도를 내보세요!";
        } else if (pace < 6.0) {
            evaluationMessage = "좋은 페이스입니다! 꾸준히 유지하세요.";
        } else if (pace < 8.0) {
            evaluationMessage = "페이스가 뛰어납니다! 계속 이렇게 해보세요!";
        } else {
            evaluationMessage = "훌륭하시네요! 혹시 마라톤 선수?";
        }
        evaluation.setText(evaluationMessage);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        polyline = new PolylineOverlay();

        // 이동 안했을 때 오류 처리
        if (coords.size() >= 2) {
            polyline.setCoords(coords);
            polyline.setColor(Color.DKGRAY);
            polyline.setMap(naverMap);
        } else {
            polyline.setMap(null);
        }

        // 시작 위치 마커
        if (!coords.isEmpty()) {
            Marker startMarker = new Marker();
            startMarker.setPosition(coords.get(0));
            startMarker.setCaptionText("시작 위치");
            startMarker.setIconTintColor(Color.MAGENTA);
            startMarker.setMap(naverMap);
        }

        // 종료 위치 마커
        double lastLat = getIntent().getDoubleExtra("lastLat", 0);
        double lastLng = getIntent().getDoubleExtra("lastLng", 0);

        if (lastLat != 0 && lastLng != 0) {
            LatLng lastLocation = new LatLng(lastLat, lastLng);
            Marker endMarker = new Marker();
            endMarker.setPosition(lastLocation);
            endMarker.setCaptionText("종료 위치");
            endMarker.setIconTintColor(Color.BLUE);
            endMarker.setMap(naverMap);

            // 종료 위치 기준으로 지도 위치 조정
            naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(lastLocation));
            naverMap.moveCamera(com.naver.maps.map.CameraUpdate.zoomTo(17));
        }
    }
}