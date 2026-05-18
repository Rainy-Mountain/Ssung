package com.example.test1;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;

public class LocationSelectFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;

    public LocationSelectFragment() { }

    public static LocationSelectFragment newInstance() {
        LocationSelectFragment fragment = new LocationSelectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_location_select, container, false);

        mapView = rootView.findViewById(R.id.navermap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap)
    {
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

        naverMap.setOnMapLongClickListener((pointF, latLng) -> {
            // 롱클릭된 위치의 좌표
            passCoordinatesToRecruitFragment(latLng);
        });
    }

    private void passCoordinatesToRecruitFragment(LatLng latLng) {
        // 좌표를 Bundle에 넣어서 전달
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", latLng);

        // RecruitFragment로 데이터 전달
        RecruitFragment recruitFragment = new RecruitFragment();
        recruitFragment.setArguments(bundle);

        // 프래그먼트 전환
        getParentFragmentManager().beginTransaction()
                .replace(R.id.containers, recruitFragment)  // R.id.container는 프래그먼트를 표시할 컨테이너 뷰 ID
                .addToBackStack(null)
                .commit();
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