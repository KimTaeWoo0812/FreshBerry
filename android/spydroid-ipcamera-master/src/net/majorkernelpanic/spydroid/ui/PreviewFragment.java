/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.spydroid.ui;

import net.majorkernelpanic.http.TinyHttpServer;
import net.majorkernelpanic.spydroid.R;
import net.majorkernelpanic.spydroid.Utilities;
import net.majorkernelpanic.spydroid.api.CustomHttpServer;
import net.majorkernelpanic.spydroid.api.CustomRtspServer;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class PreviewFragment extends Fragment {

	public final static String TAG = "PreviewFragment";

	private SurfaceView mSurfaceView;
	private TextView mTextView, mIpAddress;
    private CustomHttpServer mHttpServer;
    private RtspServer mRtspServer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onPause() {
		super.onPause();
    	getActivity().unbindService(mHttpServiceConnection);
    	getActivity().unbindService(mRtspServiceConnection);
	}
	
	@Override
    public void onResume() {
    	super.onResume();
		getActivity().bindService(new Intent(getActivity(),CustomHttpServer.class), mHttpServiceConnection, Context.BIND_AUTO_CREATE);
		getActivity().bindService(new Intent(getActivity(),CustomRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.preview,container,false);

		mTextView = (TextView)rootView.findViewById(R.id.tooltip);
		mIpAddress = (TextView)rootView.findViewById(R.id.ip_address);
		mIpAddress.setText(getIpAddress());
		
		return rootView;
	}
	
	public void update() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mTextView != null) {
					if ((mRtspServer != null && mRtspServer.isStreaming()) || (mHttpServer != null && mHttpServer.isStreaming())) {
						mTextView.setVisibility(View.INVISIBLE);
						mIpAddress.setVisibility(View.INVISIBLE);
					} else { 
						mTextView.setVisibility(View.VISIBLE);
                        mIpAddress.setVisibility(View.VISIBLE);                        
					}
				}
			}
		});
	}
	
    private final ServiceConnection mRtspServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mRtspServer = (RtspServer) ((RtspServer.LocalBinder)service).getService();
			update();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};
    
    private final ServiceConnection mHttpServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mHttpServer = (CustomHttpServer) ((TinyHttpServer.LocalBinder)service).getService();
			update();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};
	
    private String getIpAddress() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ip = null;
        String result = "";
        
        if (info != null && info.getNetworkId() > -1) {
            int i = info.getIpAddress();
            ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff, i >> 16 & 0xff, i >> 24 & 0xff);
            result += "rtsp://";
            result += ip;
            result += ":8086";
        } else if ((ip = Utilities.getLocalIpAddress(true)) != null) {
            result += "rtsp://";
            result += ip;
            result += ":8086";
        }
        
        return result;
    }

}
