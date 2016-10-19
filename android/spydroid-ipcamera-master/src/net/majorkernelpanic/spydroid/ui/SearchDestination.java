package net.majorkernelpanic.spydroid.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import net.majorkernelpanic.spydroid.R;

import org.jsoup.Jsoup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchDestination extends Activity implements OnInitListener,
		OnClickListener, OnItemClickListener {
	private boolean stopThread=true;
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

	private final int GOOGLE_STT = 1000, MY_UI = 1001; // requestCode. 구글음성인식
	private ArrayList<String> mResult; // 음성인식 결과 저장할 list
	private String mSelectedString; // 결과 list 중 사용자가 선택한 텍스트

	Intent ii;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		tts.shutdown();
		stopThread=false;
		
	}

	@Override
	public void onInit(int status) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 화면항상밝게
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_activity);

		lv_searchdestination_lv = (ListView) findViewById(R.id.lv_searchdestination_lv);
		bt_searchdestination_searchvoice = (Button) findViewById(R.id.bt_searchdestination_searchvoice);
		bt_searchdestination_search = (Button) findViewById(R.id.bt_searchdestination_search);
		et_searchdestination_getText = (EditText) findViewById(R.id.et_searchdestination_getText);

		bt_searchdestination_search.setOnClickListener(this);
		bt_searchdestination_searchvoice.setOnClickListener(this);
		
		lv_searchdestination_lv.setOnItemClickListener(this);

		tts = new TextToSpeech(this, this);
		
		 ii = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);			//intent 생성
		 ii.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());	//음성인식을 호출한 패키지
		 ii.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");							//음성인식 언어 설정
		 ii.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하세요.");						//사용자에게 보여 줄 글자
		
		
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

			String str2 = "http://map.naver.com/search2/local.nhn?sm=hty&searchCoord=128.3784%3B36.1376997&isFirstSearch=true&query="
					+ str1_1
					+ "&menu=location&mpx=04190690%3A36.1376997%2C128.3784%3AZ11%3A0.0336476%2C0.0065756";

			(new ParseURL()).execute(new String[] { "LIST", str2 });

		} else if (v.getId() == R.id.bt_searchdestination_searchvoice) {
			//#
			startActivityForResult(ii, GOOGLE_STT);
			
		}

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
						body = (Jsoup.connect(strings[1]).timeout(20000)
								.ignoreContentType(true).execute().body());
					} catch (IOException e) {
						e.printStackTrace();
					}
					
	//				Log.i("BODY ",body);
	
					body = body.replace("\n", "");
					body = body.replace("\t", "");
					
					String [] names = body.split("\"name\": \"");
					String [] xs = body.split("\"x\": \"");
					String [] ys = body.split("\"y\": \"");
	
		            String [] address = body.split("\"address\": \"");
					String tmpName;
					String tmpX;
					String tmpY;
	
		            String tmpAddress;
					ool.clear();
					
					for(int i = 1 ; i < names.length ; i++)
					{
						
						tmpName  = names[i].substring(0, names[i].indexOf("\"") );
						tmpX  = xs[i].substring(0, xs[i].indexOf("\"") );
						tmpY = ys[i].substring(0, ys[i].indexOf("\"") );
						 tmpAddress = address[i].substring(0,address[i].indexOf("\""));
			               ool.add(new oneOfList(tmpName, Double.parseDouble(tmpX), Double.parseDouble(tmpY),tmpAddress));
			               
					}
					
					
					
				} else if (rstType.compareTo("GPS") == 0) {
					try {
						
						
						Log.i("ASDASD",strings[1]);
						
						body = (Jsoup.connect(strings[1]).timeout(20000)
								.ignoreContentType(true).execute().body());
					} catch (IOException e) {
						e.printStackTrace();
					}
	
					Log.i("TMPTMPTMP",body);
	
					
					String[] str5 = body.split("location");
	
					
					String str6 = str5[1].replace(" ", "");
	
					System.out.println(str6);
	
					lat = (str6.substring(str6.indexOf("lat") + 5,
							str6.indexOf(",")));
	
					lng = (str6.substring(str6.indexOf("lng") + 5,
							str6.indexOf("}") - 1));
	
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

					al.add(ool.get(i).name+"\n"+ool.get(i).address);

				}

				// 어댑터 준비
				ArrayAdapter<String> Adapter;
				Adapter = new ArrayAdapter<String>(SearchDestination.this,
						android.R.layout.simple_list_item_1, al);
				// 어댑터 연결
				lv_searchdestination_lv.setAdapter(Adapter);

			}  
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LaunchNavi(position);

	}

	void LaunchNavi(int pos) {
		
		//##
		//if(soccket.OPTION_NAVI_TYPE)
		//{
		Intent i = new Intent(SearchDestination.this ,
				 NewNavi.class);
				 i.putExtra("str", ool.get(pos).name);
				 i.putExtra("lat", ool.get(pos).x);
				 i.putExtra("lng", ool.get(pos).y);
				
				 startActivity(i);
		//}
		/*
		else
		{
			Intent i = new Intent(SearchDestination.this ,
					 Navi.class);
					 i.putExtra("str", ool.get(pos).name);
					 i.putExtra("lat", ool.get(pos).x);
					 i.putExtra("lng", ool.get(pos).y);
					
					 startActivity(i);
		}*/
				 
	}

	class oneOfList
	{
		public String name;
	      public double x;
	      public double y;
	      public String address;
	      
	      public oneOfList(String name_, double x_, double y_,String address_) {
	         name = name_;
	         x=x_;
	         y=y_;
	         address = address_;
	         
	      }
		
		
	}
	
	
	
	
	
	
	
	
	
	
	// STT 함수

	 @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data){
			if( resultCode == RESULT_OK  && (requestCode == GOOGLE_STT || requestCode == MY_UI) ){		//결과가 있으면
				showSelectDialog(requestCode, data);				//결과를 다이얼로그로 출력.
			}
			else{															//결과가 없으면 에러 메시지 출력
				String msg = null;
				
				//내가 만든 activity에서 넘어오는 오류 코드를 분류
				switch(resultCode){
					case SpeechRecognizer.ERROR_AUDIO:
						msg = "오디오 입력 중 오류가 발생했습니다.";
						break;
					case SpeechRecognizer.ERROR_CLIENT:
						msg = "단말에서 오류가 발생했습니다.";
						break;
					case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
						msg = "권한이 없습니다.";
						break;
					case SpeechRecognizer.ERROR_NETWORK:
					case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
						msg = "네트워크 오류가 발생했습니다.";
						break;
					case SpeechRecognizer.ERROR_NO_MATCH:
						msg = "일치하는 항목이 없습니다.";
						break;
					case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
						msg = "음성인식 서비스가 과부하 되었습니다.";
						break;
					case SpeechRecognizer.ERROR_SERVER:
						msg = "서버에서 오류가 발생했습니다.";
						break;
					case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
						msg = "입력이 없습니다.";
						break;
				}
				
				if(msg != null)		//오류 메시지가 null이 아니면 메시지 출력
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		        super.onActivityResult(requestCode, resultCode, data);
		}
		
		//결과 list 출력하는 다이얼로그 생성
		private void showSelectDialog(int requestCode, Intent data){
			String key = "";
			if(requestCode == GOOGLE_STT)					//구글음성인식이면
				key = RecognizerIntent.EXTRA_RESULTS;	//키값 설정
			
			mResult = data.getStringArrayListExtra(key);		//인식된 데이터 list 받아옴.
			String[] result = new String[mResult.size()];			//배열생성. 다이얼로그에서 출력하기 위해
			mResult.toArray(result);									//	list 배열로 변환
			
				for(int i=0;i<mResult.size();i++)
				{
					String myText = result[i];
					tts.speak(myText, TextToSpeech.QUEUE_ADD, null);
				}
			tts.speak("중에 하나를 고르세요.", TextToSpeech.QUEUE_ADD, null);
			
			//1개 선택하는 다이얼로그 생성
			AlertDialog ad = new AlertDialog.Builder(this).setTitle("선택하세요.")
								.setSingleChoiceItems(result, -1, new DialogInterface.OnClickListener() {
									@Override public void onClick(DialogInterface dialog, int which) {
											mSelectedString = mResult.get(which);		//선택하면 해당 글자 저장
											String myText = mSelectedString;
												tts.stop();
												tts.speak(myText, TextToSpeech.QUEUE_ADD, null);
											
									}
								})
								.setPositiveButton("확인", new DialogInterface.OnClickListener() {
									@Override public void onClick(DialogInterface dialog, int which) {
										et_searchdestination_getText.setText(mSelectedString);		//확인 버튼 누르면 결과 출력
										
										
										
										
										
										String str1 = mSelectedString;
										String str1_1 = "";
										try {
											str1_1 = URLEncoder.encode(str1, "UTF-8");
										} catch (UnsupportedEncodingException e) {
										}

										String str2 = "http://map.naver.com/search2/local.nhn?sm=hty&searchCoord=128.3784%3B36.1376997&isFirstSearch=true&query="
												+ str1_1
												+ "&menu=location&mpx=04190690%3A36.1376997%2C128.3784%3AZ11%3A0.0336476%2C0.0065756";

										(new ParseURL()).execute(new String[] { "LIST", str2 });
										
										
										
										
										
										
										
										
										String myText = mSelectedString+"로 검색합니다.";
										
											tts.stop();
											tts.speak(myText, TextToSpeech.QUEUE_ADD, null);
										
									}
								})
								.setNegativeButton("취소", new DialogInterface.OnClickListener() {
									@Override public void onClick(DialogInterface dialog, int which) {
										et_searchdestination_getText.setText("");		//취소버튼 누르면 초기화
										mSelectedString = null;
									}
								}).create();
			ad.show();
		}

		
}//et_searchdestination_getText
