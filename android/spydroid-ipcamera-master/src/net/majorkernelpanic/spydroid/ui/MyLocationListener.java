package net.majorkernelpanic.spydroid.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;


class MyLocationListener implements LocationListener {

    LocationManager manager;
    Location loc;
    
    boolean useChanger;
	
    Context mContext;
    
    public void endGps()
    {
    	manager.removeUpdates(this);
    }
    
    public MyLocationListener(Context c) {
    	useChanger=false;
		mContext = c;
		
		setOption();
	}
    
    /**
     * 경도값을 가져옵니다. 
     * */
    void setOption()
    {
    	
    	
		if (manager == null) {
            manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        // provider 기지국||GPS 를 통해서 받을건지 알려주는 Stirng 변수
        // minTime 최소한 얼마만의 시간이 흐른후 위치정보를 받을건지 시간간격을 설정 설정하는 변수
        // minDistance 얼마만의 거리가 떨어지면 위치정보를 받을건지 설정하는 변수
        // manager.requestLocationUpdates(provider, minTime, minDistance, listener);
        // 10초
        long minTime = 2000;

        // 거리는 0으로 설정
        // 그래서 시간과 거리 변수만 보면 움직이지않고 10초뒤에 다시 위치정보를 받는다
        float minDistance = 0;

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
		
        
    }
    
	public MyLocationListener(Context c, onChangedXYListener onChangedXYListener_) {

		useChanger=true;
		mContext = c;
		 
		setOnChangedXYListener(onChangedXYListener_);
		
		setOption();
		
	}
	
    // 위치정보는 아래 메서드를 통해서 전달된다.
    @Override
    public void onLocationChanged(Location location) {

//        appendText("onLocationChanged()가 호출되었습니다");

    	
    	
        double latitude = location.getLatitude();
        double longitude = location.getLongitude(); 
        
        
        if(useChanger==true)
        	OnChangeXYListener.onChangedXY(latitude,longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    onChangedXYListener OnChangeXYListener;
   
    public void setOnChangedXYListener(onChangedXYListener OnChangeXYListener_)
    {
    	OnChangeXYListener = OnChangeXYListener_;
    }
    
    interface onChangedXYListener{
    	
    	void onChangedXY(double lat_, double lon_);
    	
    }
    /**
     * GPS 정보를 가져오지 못했을때 
     * 설정값으로 갈지 물어보는 alert 창
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
 
        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않있습니다.\n 설정창으로 가시겠습니까?");
   
        // OK 를 누르게 되면 설정창으로 이동합니다. 
        alertDialog.setPositiveButton("설정", 
                                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // Cancle 하면 종료 합니다. 
        alertDialog.setNegativeButton("취소", 
                              new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        alertDialog.show();
    }
}