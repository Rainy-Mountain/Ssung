package com.example.test1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;

public class Fragment1 extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private MapView mapView;
    private NaverMap naverMap;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;

    public Fragment1() { }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static Fragment1 newInstance(String param1, String param2) {
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_1, container, false);

        mapView = rootView.findViewById(R.id.navermap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Button recruitButton = rootView.findViewById(R.id.recruit);
        recruitButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.recruit) {
            // RecruitFragment의 인스턴스 생성
            RecruitFragment recruitFragment = new RecruitFragment();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containers, recruitFragment) // 인스턴스를 전달
                    .addToBackStack(null) // 백 스택에 추가
                    .commit();
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap)
    {
        this.naverMap = naverMap;
        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Basic);

        //건물 표시
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, true);

        //위치 및 각도 조정
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(33.38, 126.55), 10);

        //ui 세팅
        UiSettings uiSettings = naverMap.getUiSettings();

        //현재 위치로 카메라 위치 변환
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // 카메라 현재 위치로 이동
                        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(currentLatLng);
                        naverMap.moveCamera(cameraUpdate);

                        // 현재 위치 오버레이 설정
                        naverMap.getLocationOverlay().setVisible(true);
                        naverMap.getLocationOverlay().setPosition(currentLatLng);
                    }
                });
        // Firestore에서 위치 데이터 가져오기
        fetchLocationsFromFirestore();
    }

    private void fetchLocationsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("RunningSessions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Firestore에서 location 필드가 GeoPoint로 저장된 경우
                            GeoPoint geoPoint = document.getGeoPoint("location");
                            if (geoPoint != null) {
                                double latitude = geoPoint.getLatitude();
                                double longitude = geoPoint.getLongitude();
                                addMarkerToMap(latitude, longitude);
                            }
                        }
                    } else {
                        Log.e("Fragment1", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addMarkerToMap(double latitude, double longitude) {
        Marker marker = new Marker();
        marker.setPosition(new LatLng(latitude, longitude));
        marker.setMap(naverMap);

        // 마커 클릭 리스너 설정
        marker.setOnClickListener(overlay -> {
            Intent intent = new Intent(requireContext(), RunningSessionDetailsActivity.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);

            return true;
        });
    }

    @Override
    public void onStart()
    {
        String addr;

        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}