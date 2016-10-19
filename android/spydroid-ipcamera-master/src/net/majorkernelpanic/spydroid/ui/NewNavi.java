package net.majorkernelpanic.spydroid.ui;

import java.util.ArrayList;

import net.majorkernelpanic.spydroid.R;
import net.majorkernelpanic.spydroid.ui.MyLocationListener.onChangedXYListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class NewNavi extends Activity implements OnInitListener {

	TextView tv_navi_meter;
	ImageView iv_navi_leftright;
	TextView tv_niavi_do;
	TextView tv_navi_tofrom;
	TextView tv_navi_test;

	boolean isDoChack = false;

	double endX;
	double endY;
	double startX;
	double startY;
	double firX;
	double firY;
	double curX;
	double curY;
	double befX = 0;
	double befY = 0;

	String Destination;

	boolean isGetEnd = false;
	boolean isGetRoute = false;
	boolean isOn = false;

	MyLocationListener listener;

	ArrayList<OneOfRoute> oneOfRouteList;

	int dange = -1;

	int outputMeterCycleNum = 0;
	int outputDiffDirectionCycleNum = 0;

	int spkJugi = 0;

	private TextToSpeech myTTS;

	//
	private boolean stopThread = true;
	Handler hStairs;

	@Override
	protected void onPause() {
		stopThread = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	//

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 화면항상밝게
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.navi_activity);

		tv_navi_meter = (TextView) findViewById(R.id.tv_navi_meter);
		iv_navi_leftright = (ImageView) findViewById(R.id.iv_navi_leftright);
		tv_niavi_do = (TextView) findViewById(R.id.tv_niavi_do);
		tv_navi_tofrom = (TextView) findViewById(R.id.tv_navi_tofrom);
		tv_navi_test = (TextView) findViewById(R.id.tv_navi_test);

		myTTS = new TextToSpeech(this, this);

		oneOfRouteList = new ArrayList<NewNavi.OneOfRoute>();

		Intent itt = getIntent();

		Destination = itt.getExtras().getString("str");
		double lat = itt.getExtras().getDouble("lng");
		double lng = itt.getExtras().getDouble("lat"); // 바뀜

		spkMsg("목적지 " + Destination + "까지 길 안내를 시작합니다");
		// talk_add("목적지 "+Destination+"로 길 안내를 시작합니다");

		listener = new MyLocationListener(this.getApplicationContext(),
				onchangedXYListener);

		// 목적지좌표 갖고옴
		(new ParseURL())
				.execute(new String[] {
						"GPS",
						"http://apis.daum.net/maps/transcoord?apikey=4ea71af07ab0504cf561d740334d4ce0&x="
								+ lng
								+ "&y="
								+ lat
								+ "&fromCoord=wgs84&toCoord=wcongnamul&output=xml" });

			hStairs = new Handler();

		

	}

	void spkMsg(String str) {
		Log.i("SPK_MSG", str);
	}

	public void changeGPSType(double dx, double dy) {
		(new ParseURL())
				.execute(new String[] {
						"GPS",
						"http://apis.daum.net/maps/transcoord?apikey=4ea71af07ab0504cf561d740334d4ce0&x="
								+ dx
								+ "&y="
								+ dy
								+ "&fromCoord=wgs84&toCoord=wcongnamul&output=xml" });
	}

	// private void getRoute(double x1,double y1, double x2, double y2)
	// {
	// ( new ParseURL() ).execute(new
	// String[]{"ROUTE","http://map.daum.net/route/walkset.json?callback=jQuery1810362975598545745_1435566230779&sName=%EA%B2%BD%EB%B6%81+%EA%B5%AC%EB%AF%B8%EC%8B%9C+%EA%B1%B0%EC%9D%98%EB%8F%99+468&sX="+x1+"&sY="+y1+"+&eName=%EA%B2%BD%EB%B6%81+%EA%B5%AC%EB%AF%B8%EC%8B%9C+%EA%B1%B0%EC%9D%98%EB%8F%99+468&eX="+x2+"+&eY="+y2+"&ids=%2C"});
	// }

	void getFullRoute() {
		if (isGetRoute == false) {
			isGetRoute = true;
			(new ParseURL())
					.execute(new String[] {
							"ROUTE",
							"http://map.daum.net/route/walkset.json?callback=jQuery1810362975598545745_1435566230779&sName=%EA%B2%BD%EB%B6%81+%EA%B5%AC%EB%AF%B8%EC%8B%9C+%EA%B1%B0%EC%9D%98%EB%8F%99+468&sX="
									+ startX
									+ "&sY="
									+ startY
									+ "+&eName=%EA%B2%BD%EB%B6%81+%EA%B5%AC%EB%AF%B8%EC%8B%9C+%EA%B1%B0%EC%9D%98%EB%8F%99+468&eX="
									+ endX + "+&eY=" + endY + "&ids=%2C" });
			return;
		}
	}

	void startNavi() {
		for (int i = 0; i < oneOfRouteList.size(); i++) {
			System.out.println(oneOfRouteList.get(i).guideMent);
		}
		isOn = true;

		bungi();

	}

	int getNumFromGuidement(String str) {
		String kk = str;

		kk = "_" + kk;

		boolean isDig = false;

		for (int i = 0; i < kk.length() - 1; i++) {
			if (kk.charAt(i) >= '0' && kk.charAt(i) <= '9') {
				isDig = true;
				break;
			}
		}

		if (isDig == true) {

			while (true) {
				if (kk.charAt(0) >= '0' && kk.charAt(0) <= '9') {
					break;
				} else {
					kk = kk.substring(1, kk.length());
				}
			}

			int numIdx = 0;
			for (;; numIdx++) {
				if (!(kk.charAt(numIdx) >= '0' && kk.charAt(numIdx) <= '9'))
					break;
			}

			kk = kk.substring(0, numIdx);

			System.out.println(kk);
			return Integer.valueOf(kk);
		}
		return -1;
	}

	void talk_add(final String str) {

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {

						myTTS.speak(str, TextToSpeech.QUEUE_ADD, null);

					}
				});
			}
		}).start();

	}

	void talk_flush(final String str) {

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						myTTS.speak(str, TextToSpeech.QUEUE_FLUSH, null);
					}
				});
			}
		}).start();
	}

	// ##
	void setView(final String tag, final String msg) {
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {

						if (tag.compareTo("tv_navi_meter") == 0) {
							tv_navi_meter.setText(msg);
						} else if (tag.compareTo("iv_navi_leftright") == 0) {
							if (msg.compareTo("TURN_LEFT") == 0) {
								iv_navi_leftright
										.setImageDrawable(getResources()
												.getDrawable(R.drawable.left));
							} else if (msg.compareTo("TURN_RIGHT") == 0) {
								iv_navi_leftright
										.setImageDrawable(getResources()
												.getDrawable(R.drawable.right));
							} else if (msg.compareTo("TURN_STRAGHT") == 0) {
								iv_navi_leftright
										.setImageDrawable(getResources()
												.getDrawable(R.drawable.straght));
							} else if (msg.compareTo("TURN_HDBD") == 0) {
								iv_navi_leftright
										.setImageDrawable(getResources()
												.getDrawable(R.drawable.hdbd));
							}

						} else if (tag.compareTo("tv_navi_do") == 0) {
							tv_niavi_do.setText(msg);
						} else if (tag.compareTo("tv_navi_tofrom") == 0) {
							tv_navi_tofrom.setText(msg);
						} else if (tag.compareTo("tv_navi_test") == 0) {
							tv_navi_test.setText(msg);
						}

					}
				});
			}
		}).start();
	}

	void bungi() {
		// ##

		if (oneOfRouteList.size() > dange)
			dange++;

		if (dange == -1)
			dange++;

		spkMsg("oneOfRouteList.get(dange).guideMent : "
				+ oneOfRouteList.get(dange).guideMent);

		// 가이드멘트가 횡단보도가 있을경우 로테이션코드 대신 가이드멘트 이용
		if (oneOfRouteList.get(dange).guideMent.contains("횡단보도") == true) {
			// if(oneOfRouteList.get(dange).rotationCode.compareTo("STRAIGHT")==0)
			// {
			spkMsg("횡단보도를 이용합니다.");
			talk_flush("횡단보도를 이용합니다.");
			talk_add("다시 한번 알려드립니다. 횡단보도를 이용합니다.");

			// }
		} else {
			int met = getNumFromGuidement(oneOfRouteList.get(dange).guideMent);

			if (met != -1) {
				if (oneOfRouteList.get(dange).rotationCode
						.compareTo("STRAIGHT") == 0) {
					if (met != -1) {
						spkMsg("전방으로 " + String.valueOf(met) + "m 이동합니다.");
						talk_flush("전방으로 이동합니다.");
						talk_add("다시 한번 알려드립니다. 전방으로 이동합니다.");

					}
				} else if (oneOfRouteList.get(dange).rotationCode
						.compareTo("TURN_LEFT") == 0) {
					if (met != -1) {
						spkMsg("좌측 방향으로 이동합니다.");
						talk_flush("좌측 방향으로 이동합니다.");
						talk_add("다시 한번 알려드립니다. 좌측 방향으로 이동합니다.");
						
						// 좌측

					}
				} else if (oneOfRouteList.get(dange).rotationCode
						.compareTo("TURN_RIGHT") == 0) {
					if (met != -1) {
						spkMsg("우측 방향으로 " + String.valueOf(met) + "m 이동합니다.");
						talk_flush("우측 방향으로 이동합니다.");
						talk_add("다시 한번 알려드립니다. 우측 방향으로 이동합니다.");
						
						// 우측

					}
				}
			}
		}

		if (oneOfRouteList.size() > dange) {
			if (oneOfRouteList.get(dange).guideMent.contains("횡단보도") == true)
				setView("iv_navi_leftright", "TURN_HDBD");
			else if (oneOfRouteList.get(dange + 1).rotationCode
					.compareTo("TURN_RIGHT") == 0)
				setView("iv_navi_leftright", "TURN_RIGHT");
			else if (oneOfRouteList.get(dange + 1).rotationCode
					.compareTo("TURN_LEFT") == 0)
				setView("iv_navi_leftright", "TURN_LEFT");
			else if (oneOfRouteList.get(dange + 1).rotationCode
					.compareTo("STRAIGHT") == 0)
				setView("iv_navi_leftright", "TURN_STRAGHT");

		}

	}

	public class ParseURL extends AsyncTask<String, Void, String> {
		String retStr = "";
		String TagString;
		double giveX, giveY;

		@Override
		protected String doInBackground(String... strings) {
			// Log.d("MAIN","CALL");

			TagString = strings[0];

			if (TagString.compareTo("ROUTE") == 0) {
				try {

					Log.i("NewNavi", "PASER START");

					String strBuf = "";
					strBuf = (Jsoup.connect(strings[1]).timeout(20000)
							.ignoreContentType(true).execute().body());

					strBuf = strBuf.substring(strBuf.indexOf('(') + 1,
							strBuf.length() - 1);

					try {

						JSONObject Jobject = new JSONObject(strBuf);

						JSONArray Jarray = new JSONArray(
								Jobject.getString("directions"));

						Jobject = Jarray.getJSONObject(0); // 큰길우선

						Jarray = new JSONArray(Jobject.getString("sections"));

						Jobject = Jarray.getJSONObject(0);

						Jarray = new JSONArray(Jobject.getString("guideList"));

						String tmpguideMent;
						String tmprotationCode = "NONE";
						double tmpx;
						double tmpy;

						for (int i = 0; i < Jarray.length(); i++) {
							Jobject = Jarray.getJSONObject(i);

							// i+1의 x,y까지가는대 남은거리 및 지나고있는길
							tmpguideMent = Jobject.getString("guideMent");

							// 좌표는 현재좌표임 i+1이 가야될좌표
							tmpx = Double.parseDouble(Jobject.getString("x"));
							tmpy = Double.parseDouble(Jobject.getString("y"));

							try {
								tmprotationCode = "NONE";
								// i+1도달했을때 나오는 rotationcCode
								tmprotationCode = Jobject
										.getString("rotationCode");
							} catch (Exception e) {
							}

							oneOfRouteList.add(new OneOfRoute(tmpguideMent,
									tmprotationCode, tmpx, tmpy));
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
				}

			}
			if (TagString.compareTo("GPS") == 0) {
				try {
					String strBuf = "";
					strBuf = (Jsoup.connect(strings[1]).timeout(20000)
							.ignoreContentType(true).execute().body());
					String[] tmpStrArr = strBuf.split("'");
					giveX = Double.parseDouble(tmpStrArr[1]);
					giveY = Double.parseDouble(tmpStrArr[3]);

				} catch (Throwable t) {
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (TagString.compareTo("ROUTE") == 0) {
				Log.i("NewNavi", "PASER QUIT");
				startNavi();
			} else if (TagString.compareTo("GPS") == 0) {
				if (isOn == true) {
					// $$
					// int outputMeterCycleNum=0;
					// int outputDiffDirectionCycleNum=0;

					curX = giveX;
					curY = giveY;

					firX = oneOfRouteList.get(dange + 1).x;
					firY = oneOfRouteList.get(dange + 1).y;

					// 도착판단

					int meter = (int) Math.sqrt(Math.pow((curX - firX), 2)
							+ Math.pow((curY - firY), 2));

					// spkMsg("TEST dange : " + dange+1 + "     x : " + curX +
					// "   y : " + curY + "         GPS : " + );

					meter = meter * 4 / 10;

					spkMsg(String.valueOf(meter) + "m 남았습니다.");

					setView("tv_navi_meter", String.valueOf(meter) + "m 남았습니다.");

					if (spkJugi++ >= 2) {
						spkJugi = 0;

						talk_add(String.valueOf(meter) + "m");

					}

					// xy[dange+1]에 도달
					if (meter < 25) {
						if (oneOfRouteList.get(dange + 1).guideMent
								.contains("도착") == true) {

							if (isDoChack == false) {
								isDoChack = true;

								spkMsg("목적지에 도달하였습니다. 길안내를 종료합니다.");
								talk_flush("목적지" + Destination
										+ "에 도달하였습니다. 길안내를 종료합니다.");

								Handler hh = new Handler();
								hh.postDelayed(new Runnable() {

									@Override
									public void run() {
										finish();
									}
								}, 9000);

							}

							return;
						}

						bungi();
						return;
					}

					// 몇도채크
					// giveX giveY 에서 oneOfRouteList.get(dange+1)XY 로몰기

					double curResult;

					if (firX == befX && firY == befY) {
						spkMsg("움직임없음");
						return;
					}

					double a = Math.sqrt(Math.pow(firX - befX, 2)
							+ Math.pow(firY - befY, 2));
					double b = Math.sqrt(Math.pow(curX - firX, 2)
							+ Math.pow(curY - firY, 2));
					double c = Math.sqrt(Math.pow(curX - befX, 2)
							+ Math.pow(curY - befY, 2));

					curResult = Math
							.acos((c * c + a * a - b * b) / (2 * c * a))
							* 180
							/ Math.PI;

					String doStr = String.valueOf(curResult);

					try {
						doStr = doStr.substring(0, doStr.indexOf("."));
					} catch (Exception e) {
						spkMsg(doStr);
					}

					spkMsg("테스트 : curResult : " + doStr);

					if (curResult > 60)
						outputDiffDirectionCycleNum++;
					else
						outputDiffDirectionCycleNum = 0;

					// 연산 후

					befX = curX;
					befY = curY;

					setView("tv_navi_do", doStr + "°\n");

					// outputDiffDirectionCycleNum>=2이상이면 spk
					if (outputDiffDirectionCycleNum >= 2) {
						// 쌓이는거x
						spkMsg(doStr + "° 틀어졌습니다.");
						talk_flush(doStr + "도 틀어졌습니다.");
						setView("tv_navi_do", doStr + "°\n틀어졌습니다.");
					}

					return;
				}

				if (isGetEnd == false) {
					Log.i("isGetEnd",
							String.valueOf(giveX) + "  "
									+ String.valueOf(giveY));
					endX = giveX;
					endY = giveY;
					isGetEnd = true;

					return;
				}

				if (isGetRoute == false) {
					Log.i("isGetRoute",
							String.valueOf(giveX) + "  "
									+ String.valueOf(giveY));
					startX = giveX;
					startY = giveY;
					getFullRoute();

					return;
				}
			}
		}
	}

	onChangedXYListener onchangedXYListener = new onChangedXYListener() {

		@Override
		public void onChangedXY(double lat_, double lon_) {
			Log.i("changeGPSType", "changeGPSType");

			changeGPSType(lon_, lat_);
			setView("tv_navi_tofrom",
					String.valueOf(lat_) + "\n" + String.valueOf(lon_));
		}
	};

	@Override
	protected void onDestroy() {
		listener.endGps();
		myTTS.shutdown();
		super.onDestroy();
	}

	class OneOfRoute {
		public String guideMent;
		public String rotationCode;
		public double x;
		public double y;

		public OneOfRoute(String guideMent_, String rotationCode_, double x_,
				double y_) {
			guideMent = guideMent_;
			rotationCode = rotationCode_;
			x = x_;
			y = y_;
		}
	}

	@Override
	public void onInit(int status) {
		myTTS.speak("목적지 " + Destination + "까지 길 안내를 시작합니다",
				TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			Log.i("KEYDOWN", "KEYCODE_VOLUME_DOWN");


		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			Log.i("KEYDOWN", "KEYCODE_VOLUME_UP");
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("KEYDOWN", "KEYCODE_BACK");
			
		}

		return true;
	}


}