/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.majorkernelpanic.spydroid.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import net.majorkernelpanic.http.TinyHttpServer;
import net.majorkernelpanic.spydroid.R;
import net.majorkernelpanic.spydroid.SpydroidApplication;
import net.majorkernelpanic.spydroid.Utilities;
import net.majorkernelpanic.spydroid.api.CustomHttpServer;
import net.majorkernelpanic.spydroid.api.CustomRtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import org.jsoup.Jsoup;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HandsetFragment extends Fragment implements OnInitListener, OnClickListener, OnItemClickListener {

    public final static String TAG = "HandsetFragment";

    Handler hStairs;
    ListView lv_searchdestination_lv;
    Button bt_searchdestination_searchvoice;
    Button bt_searchdestination_search;
    EditText et_searchdestination_getText;

    String lat;
    String lng;

    String selectedLocation;

    ArrayList<String> al = new ArrayList<String>();

    TextToSpeech tts;

    private final int GOOGLE_STT = 1000, MY_UI = 1001;
    private ArrayList<String> mResult;
    private String mSelectedString;

    Intent ii;

    @Override
    public void onInit(int status) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(getActivity(), this);

        ii = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // intent
        ii.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "net.majorkernelpanic.spydroid.ui");
        ii.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        ii.putExtra(RecognizerIntent.EXTRA_PROMPT, "���� �ϼ���.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_activity, container, false);

        lv_searchdestination_lv = (ListView) rootView.findViewById(R.id.lv_searchdestination_lv);
        bt_searchdestination_searchvoice = (Button) rootView.findViewById(R.id.bt_searchdestination_searchvoice);
        bt_searchdestination_search = (Button) rootView.findViewById(R.id.bt_searchdestination_search);
        et_searchdestination_getText = (EditText) rootView.findViewById(R.id.et_searchdestination_getText);

        bt_searchdestination_search.setOnClickListener(this);
        bt_searchdestination_searchvoice.setOnClickListener(this);

        lv_searchdestination_lv.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_searchdestination_search) {
            String str1 = et_searchdestination_getText.getText().toString();

            String str1_1 = "";
            try {
                str1_1 = URLEncoder.encode(str1, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }

            String str2 = "http://map.naver.com/search2/local.nhn?sm=hty&searchCoord=128.3784%3B36.1376997&isFirstSearch=true&query=" + str1_1 + "&menu=location&mpx=04190690%3A36.1376997%2C128.3784%3AZ11%3A0.0336476%2C0.0065756";

            (new ParseURL()).execute(new String[] {"LIST", str2});

        } else if (v.getId() == R.id.bt_searchdestination_searchvoice) {
            // #
            startActivityForResult(ii, GOOGLE_STT);

        }// else if (v.getId() == R.id.bt_searchdestination_search) {
         // Intent intent3 = new Intent(getActivity(),
         // SearchDestination.class);
         // }

    }

    ArrayList<oneOfList> ool = new ArrayList<oneOfList>();

    public class ParseURL extends AsyncTask<String, Void, String> {
        String retStr = "";
        String rstType;

        @Override
        protected String doInBackground(String... strings) {
            String body = "";

            try {

                rstType = strings[0];
                if (rstType.compareTo("LIST") == 0) {
                    try {
                        body = (Jsoup.connect(strings[1]).timeout(20000).ignoreContentType(true).execute().body());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Log.i("BODY ",body);

                    body = body.replace("\n", "");
                    body = body.replace("\t", "");

                    String[] names = body.split("\"name\": \"");
                    String[] xs = body.split("\"x\": \"");
                    String[] ys = body.split("\"y\": \"");

                    String[] address = body.split("\"address\": \"");
                    String tmpName;
                    String tmpX;
                    String tmpY;

                    String tmpAddress;
                    ool.clear();

                    for (int i = 1; i < names.length; i++) {

                        tmpName = names[i].substring(0, names[i].indexOf("\""));
                        tmpX = xs[i].substring(0, xs[i].indexOf("\""));
                        tmpY = ys[i].substring(0, ys[i].indexOf("\""));
                        tmpAddress = address[i].substring(0, address[i].indexOf("\""));
                        ool.add(new oneOfList(tmpName, Double.parseDouble(tmpX), Double.parseDouble(tmpY), tmpAddress));

                    }

                } else if (rstType.compareTo("GPS") == 0) {
                    try {

                        Log.i("ASDASD", strings[1]);

                        body = (Jsoup.connect(strings[1]).timeout(20000).ignoreContentType(true).execute().body());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.i("TMPTMPTMP", body);

                    String[] str5 = body.split("location");

                    String str6 = str5[1].replace(" ", "");

                    System.out.println(str6);

                    lat = (str6.substring(str6.indexOf("lat") + 5, str6.indexOf(",")));

                    lng = (str6.substring(str6.indexOf("lng") + 5, str6.indexOf("}") - 1));

                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (rstType.compareTo("LIST") == 0) {

                al.clear();

                for (int i = 0; i < ool.size(); i++) {

                    al.add(ool.get(i).name + "\n" + ool.get(i).address);

                }

                // ����� �غ�
                ArrayAdapter<String> Adapter;
                Adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, al);
                // ����� ����
                lv_searchdestination_lv.setAdapter(Adapter);

            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LaunchNavi(position);

    }

    void LaunchNavi(int pos) {

        // ##
        // if(soccket.OPTION_NAVI_TYPE)
        // {
        Intent i = new Intent(getActivity(), NewNavi.class);
        i.putExtra("str", ool.get(pos).name);
        i.putExtra("lat", ool.get(pos).x);
        i.putExtra("lng", ool.get(pos).y);

        startActivity(i);
        // }
        /*
         * else { Intent i = new Intent(SearchDestination.this , Navi.class);
         * i.putExtra("str", ool.get(pos).name); i.putExtra("lat",
         * ool.get(pos).x); i.putExtra("lng", ool.get(pos).y);
         * startActivity(i); }
         */

    }

    class oneOfList {
        public String name;
        public double x;
        public double y;
        public String address;

        public oneOfList(String name_, double x_, double y_, String address_) {
            name = name_;
            x = x_;
            y = y_;
            address = address_;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if( resultCode == (-1) && (requestCode == GOOGLE_STT || requestCode
        // == MY_UI) ){ //����� ������

        if (true) {
            showSelectDialog(requestCode, data); // ����� ���̾�α׷� ���.
        } else { // ����� ������ ���� �޽��� ���
            String msg = null;

            // ���� ���� activity���� �Ѿ���� ���� �ڵ带 �з�
            switch (resultCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    msg = "����� �Է� �� ������ �߻��߽��ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    msg = "�ܸ����� ������ �߻��߽��ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    msg = "������ �����ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    msg = "��Ʈ��ũ ������ �߻��߽��ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    msg = "��ġ�ϴ� �׸��� �����ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    msg = "�����ν� ������ ������ �Ǿ����ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    msg = "�������� ������ �߻��߽��ϴ�.";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    msg = "�Է��� �����ϴ�.";
                    break;
            }

            if (msg != null) // ���� �޽����� null�� �ƴϸ� �޽��� ���
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ��� list ����ϴ� ���̾�α� ����
    private void showSelectDialog(int requestCode, Intent data) {
        String key = "";
        if (requestCode == GOOGLE_STT) // ���������ν��̸�
            key = RecognizerIntent.EXTRA_RESULTS; // Ű�� ����

        mResult = data.getStringArrayListExtra(key); // �νĵ� ������ list �޾ƿ�.
        String[] result = new String[mResult.size()]; // �迭����. ���̾�α׿���
                                                      // ����ϱ� ����
        mResult.toArray(result); // list �迭�� ��ȯ

        for (int i = 0; i < mResult.size(); i++) {
            String myText = result[i];
            tts.speak(myText, TextToSpeech.QUEUE_ADD, null);
        }
        tts.speak("�߿� �ϳ��� ������.", TextToSpeech.QUEUE_ADD, null);

        // 1�� �����ϴ� ���̾�α� ����
        AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle("�����ϼ���.").setSingleChoiceItems(result, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedString = mResult.get(which); // �����ϸ�
                                                      // �ش�
                                                      // ����
                                                      // ����
                String myText = mSelectedString;
                tts.stop();
                tts.speak(myText, TextToSpeech.QUEUE_ADD, null);

            }
        }).setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_searchdestination_getText.setText(mSelectedString); // Ȯ��
                                                                       // ��ư
                                                                       // ������
                                                                       // ���
                                                                       // ���

                String str1 = mSelectedString;
                String str1_1 = "";
                try {
                    str1_1 = URLEncoder.encode(str1, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }

                String str2 = "http://map.naver.com/search2/local.nhn?sm=hty&searchCoord=128.3784%3B36.1376997&isFirstSearch=true&query=" + str1_1 + "&menu=location&mpx=04190690%3A36.1376997%2C128.3784%3AZ11%3A0.0336476%2C0.0065756";

                (new ParseURL()).execute(new String[] {"LIST", str2});

                String myText = mSelectedString + "�� �˻��մϴ�.";

                tts.stop();
                tts.speak(myText, TextToSpeech.QUEUE_ADD, null);

            }
        }).setNegativeButton("���", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_searchdestination_getText.setText(""); // ��ҹ�ư
                                                          // ������
                                                          // �ʱ�ȭ
                mSelectedString = null;
            }
        }).create();
        ad.show();
    }

}
