package com.gizwits.opensource.appkit.ControlModule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.appkit.R;
import com.gizwits.opensource.appkit.utils.HexStrUtils;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.gizwits.opensource.appkit.utils.HexStrUtils.bytesToHexString;



public class GosDeviceControlActivity extends GosControlModuleBaseActivity
        implements OnClickListener {

    /** 设备列表传入的设备变量 */
    private GizWifiDevice mDevice;

    RelativeLayout relativeLayout1;
    LinearLayout linearLayout4;
    RelativeLayout layoutTime1;
    RelativeLayout layoutTime2;
    RelativeLayout layoutTime3;
    RelativeLayout layoutTime4;
    RelativeLayout layoutTime5;
    RelativeLayout setTimeLayout1;
    RelativeLayout setTimeLayout2;
    RelativeLayout setTimeLayout3;
    RelativeLayout setTimeLayout4;
    RelativeLayout setTimeLayout5;
    LinearLayout setupLayout;

    TextView alarm1;
    TextView alarm2;
    TextView alarm3;
    TextView alarm4;
    TextView alarm5;

    int option;
    TextView hint1;
    TextView hint2;
    TextView hint3;
    TextView hint4;
    TextView hint5;

    Context context;
    Intent my_intent;
    Calendar now;
    int nowTime;
    int adjustTimeNow;
    int clockTime1;
    int clockTime2;
    int clockTime3;
    int clockTime4;
    int clockTime5;
    int LidState;
    int remainingPack;
    int totalPack;
    String dosingTime;

    TimePicker alarm_timepicker1;
    TimePicker alarm_timepicker2;
    TimePicker alarm_timepicker3;
    TimePicker alarm_timepicker4;
    TimePicker alarm_timepicker5;

    String s_clock;

    TextView clock1;
    String s_clock1 = "00:00";

    TextView clock2;
    String s_clock2;

    TextView clock3;
    String s_clock3;

    TextView clock4;
    String s_clock4;

    TextView clock5;
    String s_clock5;
    int FlagBtSure = 0;
    int FlagRingOpen = 0;
    int Last_data_D_Remaining_Pack = 0;


    int ret = 0;
    private String TAG = "ivw";
    private Toast mToast;
    private TextView textView;
    private VoiceWakeuper mIvw;
    private SpeechRecognizer mAsr;
    private String recoString;
    private String mLocalGrammarID;
    private String mLocalGrammar = null;
    private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/msc/test";
    private String mEngineType = SpeechConstant.TYPE_LOCAL;

    private enum handler_key { UPDATE_UI, DISCONNECT, }

    private Runnable mRunnable = new Runnable() {
        public void run() {
        if (isDeviceCanBeControlled()) { progressDialog.cancel(); }
        else { toastDeviceNoReadyAndExit(); }
        }

    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        super.handleMessage(msg);
        handler_key key = handler_key.values()[msg.what];
        switch (key) { case UPDATE_UI:  updateUI();break; case DISCONNECT: toastDeviceDisconnectAndExit();break; }
        }
    };

    public static final int MSG_ONE = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) { case MSG_ONE: checkTime(); break;default: break; }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gos_device_control);
        initDevice();
        setActionBar(false, true, getDeviceName());
        initView();
        new TimeThread().start();

    }


    private void initView() {
        this.context = this;
        my_intent = new Intent(this.context, Alarm_Receiver.class);
        now = Calendar.getInstance();

        LidState = 2;
        dosingTime = "";

        linearLayout4 = (LinearLayout) findViewById(R.id.linearLayout4);
        layoutTime1 = (RelativeLayout) findViewById(R.id.layoutTime1);
        layoutTime2 = (RelativeLayout) findViewById(R.id.layoutTime2);
        layoutTime3 = (RelativeLayout) findViewById(R.id.layoutTime3);
        layoutTime4 = (RelativeLayout) findViewById(R.id.layoutTime4);
        layoutTime5 = (RelativeLayout) findViewById(R.id.layoutTime5);
        setTimeLayout1 = (RelativeLayout) findViewById(R.id.setTimeLayout1);
        setTimeLayout2 = (RelativeLayout) findViewById(R.id.setTimeLayout2);
        setTimeLayout3 = (RelativeLayout) findViewById(R.id.setTimeLayout3);
        setTimeLayout4 = (RelativeLayout) findViewById(R.id.setTimeLayout4);
        setTimeLayout5 = (RelativeLayout) findViewById(R.id.setTimeLayout5);

        alarm1 = (TextView) findViewById(R.id.alarm1);
        alarm2 = (TextView) findViewById(R.id.alarm2);
        alarm3 = (TextView) findViewById(R.id.alarm3);
        alarm4 = (TextView) findViewById(R.id.alarm4);
        alarm5 = (TextView) findViewById(R.id.alarm5);

        totalPack = 200;
        option = 4;
        alarm1.setText(turnStr(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)).substring(1,2)
                + turnStr(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)).substring(3,4)
                + ":" + turnStr(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)).substring(5,6)
                + turnStr(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)).substring(7,8));
        alarm2.setText("08:30");
        alarm3.setText("12:30");
        alarm4.setText("19:30");
        alarm5.setText("00:00");
        s_clock1 = alarm1.getText().toString();
        s_clock2 = alarm2.getText().toString();
        s_clock3 = alarm3.getText().toString();
        s_clock4 = alarm4.getText().toString();
        s_clock5 = alarm5.getText().toString();
        setDosingTime();
        setDosingTime();

        mIvw = VoiceWakeuper.createWakeuper(this, null);
        mAsr = SpeechRecognizer.createRecognizer(this, null);
        mLocalGrammar = readFile(this, "wake.bnf", "utf-8");
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        ret = mAsr.buildGrammar("bnf", mLocalGrammar, new GrammarListener() {
            @Override
            public void onBuildFinish(String grammarId, SpeechError error) {
                mLocalGrammarID = grammarId;
            }
        });

        findViewById(R.id.btn_oneshot).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mIvw = VoiceWakeuper.getWakeuper();
                recoString = "识别中...\n";
                textView.setText(recoString);
                final String resPath = ResourceUtil.generateResourcePath(GosDeviceControlActivity.this, RESOURCE_TYPE.assets, "ivw/5b3b1fa5.jet");
                mIvw.setParameter(SpeechConstant.PARAMS, null);
                mIvw.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
                mIvw.setParameter(ResourceUtil.IVW_RES_PATH, resPath);
                mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + 0);
                mIvw.setParameter(SpeechConstant.IVW_SST, "oneshot");
                mIvw.setParameter(SpeechConstant.RESULT_TYPE, "json");
                mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
                mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
                mIvw.setParameter(ResourceUtil.ASR_RES_PATH,
                        getResourcePath());
                mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
                mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR,
                        mLocalGrammarID);
                mIvw.startListening(mWakeuperListener);
            }
        });
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        textView = (TextView) findViewById(R.id.txt_show_msg);

        alarm1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog1, null);
				alarm_timepicker1 = (TimePicker) timeSelect.findViewById(R.id.TimePicker1);
				alarm_timepicker1.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							mHour = String.valueOf(alarm_timepicker1.getHour());
							mMinute = String.valueOf(alarm_timepicker1.getMinute());
							alarm1.setText(new StringBuilder().append(alarm_timepicker1.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker1.getMinute() < 10 ? 0 + mMinute : mMinute));
						} else {
							mHour = String.valueOf(alarm_timepicker1.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker1.getCurrentMinute());
							alarm1.setText(new StringBuilder().append(alarm_timepicker1.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker1.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
						}
						s_clock1 = alarm1.getText().toString();
                        setDosingTime();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
            }
        });

        alarm2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
                View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog2, null);
                alarm_timepicker2 = (TimePicker) timeSelect.findViewById(R.id.TimePicker2);
                alarm_timepicker2.setIs24HourView(true);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("设置闹钟");
                builder.setView(timeSelect);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mHour;
                        String mMinute;
                        if (Build.VERSION.SDK_INT >= 23 ){
                            alarm_timepicker2.getHour();
                            mHour = String.valueOf(alarm_timepicker2.getHour());
                            mMinute = String.valueOf(alarm_timepicker2.getMinute());
                            alarm2.setText(new StringBuilder().append(alarm_timepicker2.getHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker2.getMinute() < 10 ? 0 + mMinute : mMinute));
                        } else {
                            alarm_timepicker2.getCurrentHour();
                            mHour = String.valueOf(alarm_timepicker2.getCurrentHour());
                            mMinute = String.valueOf(alarm_timepicker2.getCurrentMinute());
                            alarm2.setText(new StringBuilder().append(alarm_timepicker2.getCurrentHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker2.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
                        }
                        s_clock2 = alarm2.getText().toString();
                        setDosingTime();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        alarm3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
                View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog3, null);
                alarm_timepicker3 = (TimePicker) timeSelect.findViewById(R.id.TimePicker3);
                alarm_timepicker3.setIs24HourView(true);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("设置闹钟");
                builder.setView(timeSelect);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mHour;
                        String mMinute;
                        if (Build.VERSION.SDK_INT >= 23 ){
                            alarm_timepicker3.getHour();
                            mHour = String.valueOf(alarm_timepicker3.getHour());
                            mMinute = String.valueOf(alarm_timepicker3.getMinute());
                            alarm3.setText(new StringBuilder().append(alarm_timepicker3.getHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker3.getMinute() < 10 ? 0 + mMinute : mMinute));
                        } else {
                            alarm_timepicker3.getCurrentHour();
                            mHour = String.valueOf(alarm_timepicker3.getCurrentHour());
                            mMinute = String.valueOf(alarm_timepicker3.getCurrentMinute());
                            alarm3.setText(new StringBuilder().append(alarm_timepicker3.getCurrentHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker3.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
                        }
                        s_clock3 = alarm3.getText().toString();
                        setDosingTime();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        alarm4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
                View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog4, null);
                alarm_timepicker4 = (TimePicker) timeSelect.findViewById(R.id.TimePicker4);
                alarm_timepicker4.setIs24HourView(true);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("设置闹钟");
                builder.setView(timeSelect);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mHour;
                        String mMinute;
                        if (Build.VERSION.SDK_INT >= 23 ){
                            alarm_timepicker4.getHour();
                            mHour = String.valueOf(alarm_timepicker4.getHour());
                            mMinute = String.valueOf(alarm_timepicker4.getMinute());
                            alarm4.setText(new StringBuilder().append(alarm_timepicker4.getHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker4.getMinute() < 10 ? 0 + mMinute : mMinute));
                        } else {
                            alarm_timepicker4.getCurrentHour();
                            mHour = String.valueOf(alarm_timepicker4.getCurrentHour());
                            mMinute = String.valueOf(alarm_timepicker4.getCurrentMinute());
                            alarm4.setText(new StringBuilder().append(alarm_timepicker4.getCurrentHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker4.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
                        }
                        s_clock4 = alarm4.getText().toString();
                        setDosingTime();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        alarm5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
                View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog5, null);
                alarm_timepicker5 = (TimePicker) timeSelect.findViewById(R.id.TimePicker5);
                alarm_timepicker5.setIs24HourView(true);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("设置闹钟");
                builder.setView(timeSelect);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mHour;
                        String mMinute;
                        if (Build.VERSION.SDK_INT >= 23 ){
                            alarm_timepicker5.getHour();
                            mHour = String.valueOf(alarm_timepicker5.getHour());
                            mMinute = String.valueOf(alarm_timepicker5.getMinute());
                            alarm5.setText(new StringBuilder().append(alarm_timepicker5.getHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker5.getMinute() < 10 ? 0 + mMinute : mMinute));
                        } else {
                            alarm_timepicker5.getCurrentHour();
                            mHour = String.valueOf(alarm_timepicker5.getCurrentHour());
                            mMinute = String.valueOf(alarm_timepicker5.getCurrentMinute());
                            alarm5.setText(new StringBuilder().append(alarm_timepicker5.getCurrentHour() < 10 ? 0 + mHour : mHour)
                                    .append(":").append(alarm_timepicker5.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
                        }
                        s_clock5 = alarm5.getText().toString();
                        setDosingTime();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        hint1 = (TextView) findViewById(R.id.hint1);
        hint2 = (TextView) findViewById(R.id.hint2);
        hint3 = (TextView) findViewById(R.id.hint3);
        hint4 = (TextView) findViewById(R.id.hint4);
        hint5 = (TextView) findViewById(R.id.hint5);

        clock1 = (TextView) findViewById(R.id.clock1);
        clock2 = (TextView) findViewById(R.id.clock2);
        clock3 = (TextView) findViewById(R.id.clock3);
        clock4 = (TextView) findViewById(R.id.clock4);
        clock5 = (TextView) findViewById(R.id.clock5);


    }



	/*
	   语音部分开始======================================================================
	 */

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();

    void speekText(String voiceStr) {
        //1. 创建 SpeechSynthesizer 对象 , 第二个参数： 本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer( this, null);
        //2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
        //设置发音人（更多在线发音人，用户可参见 附录 13.2
        mTts.setParameter(SpeechConstant. VOICE_NAME, "xiaoyan" ); // 设置发音人
        mTts.setParameter(SpeechConstant. SPEED, "35" );// 设置语速
        mTts.setParameter(SpeechConstant. VOLUME, "100" );// 设置音量，范围 0~100
        mTts.setParameter(SpeechConstant. ENGINE_TYPE, SpeechConstant. TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在 “./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant. TTS_AUDIO_PATH, "./sdcard/iflytek.pcm" );
        //3.开始合成
        mTts.startSpeaking(voiceStr, new MySynthesizerListener()) ;
    }

    class voiceRet {
        String output;
    }

    private voiceRet dialogResponce(String input) {
        voiceRet vr = new voiceRet();
        vr.output = "";

        if(input.indexOf("PILL_1") != -1){
            vr.output = "谢谢服药";
        } else if(input.indexOf("PILL_2") != -1) {
            vr.output = "您取走了两包药，请按规定时间服药";
        } else if(input.indexOf("PILL_3") != -1) {
            vr.output = "您取走了三包药，请按规定时间服药";
        } else if(input.indexOf("PILL_4") != -1) {
            vr.output = "您取走了四包药，请按规定时间服药";
        } else if(input.indexOf("PILL_5") != -1) {
            vr.output = "您取走了五包药，请按规定时间服药";
        } else if(input.indexOf("PILL_6") != -1) {
            vr.output = "您取走了六包药，请按规定时间服药";
        } else if(input.indexOf("PILL_MORE") != -1) {
            vr.output = "您取走了多包药，请按规定时间服药";
        } else if(input.indexOf("LID_OPEN") != -1) {
            vr.output = "药盒被打开";
        } else if(input.indexOf("LID_CLOSE") != -1) {
            vr.output = "药盒已关闭";
        } else if(input.indexOf("TIME_PILL") != -1) {
            vr.output = "服药时间已到，请按时服药";
        } else if(input.indexOf("PILL_WRONG") != -1) {
            vr.output = "请按规定时间服药";
        }
        return vr;
    }

    class MySynthesizerListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
//            showTip(" 开始播放 ");
        }

        @Override
        public void onSpeakPaused() {
//            showTip(" 暂停播放 ");
        }

        @Override
        public void onSpeakResumed() {
//            showTip(" 继续播放 ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos ,
                                     String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
//            if (error == null) {
//                showTip("播放完成 ");
//            } else if (error != null ) {
//                showTip(error.getPlainDescription( true));
//            }
        }

        @Override
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //     Log.d(TAG, "session id =" + sid);
            //}
        }
    }


//    private void showTip (String data) {
//        //Toast.makeText( this, data, Toast.LENGTH_SHORT).show() ;
//    }

	/*
	   语音部分结束======================================================================
	 */

    void checkTime(){

        clockTime1 = Integer.parseInt(alarm1.getText().toString().substring(0,2))*60
                + Integer.parseInt(alarm1.getText().toString().substring(3,5));
        clockTime2 = Integer.parseInt(alarm2.getText().toString().substring(0,2))*60
                + Integer.parseInt(alarm2.getText().toString().substring(3,5));
        clockTime3 = Integer.parseInt(alarm3.getText().toString().substring(0,2))*60
                + Integer.parseInt(alarm3.getText().toString().substring(3,5));
        clockTime4 = Integer.parseInt(alarm4.getText().toString().substring(0,2))*60
                + Integer.parseInt(alarm4.getText().toString().substring(3,5));
        clockTime5 = Integer.parseInt(alarm5.getText().toString().substring(0,2))*60
                + Integer.parseInt(alarm5.getText().toString().substring(3,5));
        now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        nowTime = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);

        switch (option) {
            case 1:
                if (nowTime == clockTime1) {
                    my_intent.putExtra("extra", "alarm on1");
                    sendBroadcast(my_intent);
                } else {
                    my_intent.putExtra("extra", "alarm off");
                    sendBroadcast(my_intent);
                }
                layoutTime1.setVisibility(View.VISIBLE);
                layoutTime2.setVisibility(View.GONE);
                layoutTime3.setVisibility(View.GONE);
                layoutTime4.setVisibility(View.GONE);
                layoutTime5.setVisibility(View.GONE);
                hint1.setVisibility(View.VISIBLE);
                alarm1.setTextColor(Color.parseColor("#464646"));
                if (Math.abs(nowTime - clockTime1) < 30) {
                    hint1.setText("本次服药");
                } else {
                    hint1.setText("下次服药");
                }
                break;
            case 2:
                if (nowTime == clockTime1) {
                    my_intent.putExtra("extra", "alarm on1");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime2){
                    my_intent.putExtra("extra", "alarm on2");
                    sendBroadcast(my_intent);
                } else{
                    my_intent.putExtra("extra", "alarm off");
                    sendBroadcast(my_intent);
                }
                layoutTime1.setVisibility(View.VISIBLE);
                layoutTime2.setVisibility(View.VISIBLE);
                layoutTime3.setVisibility(View.GONE);
                layoutTime4.setVisibility(View.GONE);
                layoutTime5.setVisibility(View.GONE);
                if(data_D_Remaining_Pack % 2 == totalPack % 2){
                    hint1.setVisibility(View.VISIBLE);
                    hint2.setVisibility(View.GONE);
                    alarm1.setTextColor(Color.parseColor("#464646"));
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime1) < 30){
                        hint1.setText("本次服药");
                    } else {
                        hint1.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 2 == (totalPack+1) % 2){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.VISIBLE);
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime2) < 30){
                        hint2.setText("本次服药");
                    } else {
                        hint2.setText("下次服药");
                    }
                }
                break;
            case 3:
                if (nowTime == clockTime1) {
                    my_intent.putExtra("extra", "alarm on1");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime2){
                    my_intent.putExtra("extra", "alarm on2");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime3){
                    my_intent.putExtra("extra", "alarm on3");
                    sendBroadcast(my_intent);
                } else{
                    my_intent.putExtra("extra", "alarm off");
                    sendBroadcast(my_intent);
                }
                layoutTime1.setVisibility(View.VISIBLE);
                layoutTime2.setVisibility(View.VISIBLE);
                layoutTime3.setVisibility(View.VISIBLE);
                layoutTime4.setVisibility(View.GONE);
                layoutTime5.setVisibility(View.GONE);
                if(data_D_Remaining_Pack % 3 == totalPack % 3){
                    hint1.setVisibility(View.VISIBLE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    alarm1.setTextColor(Color.parseColor("#464646"));
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime1) < 30){
                        hint1.setText("本次服药");
                    } else {
                        hint1.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 3 == (totalPack+2) % 3){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.VISIBLE);
                    hint3.setVisibility(View.GONE);
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime2) < 30){
                        hint2.setText("本次服药");
                    } else {
                        hint2.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 3 == (totalPack+1) % 3){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.VISIBLE);
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime3) < 30){
                        hint3.setText("本次服药");
                    } else {
                        hint3.setText("下次服药");
                    }
                }
                break;
            case 4:
                if (nowTime == clockTime1) {
                    my_intent.putExtra("extra", "alarm on1");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime2){
                    my_intent.putExtra("extra", "alarm on2");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime3){
                    my_intent.putExtra("extra", "alarm on3");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime4){
                    my_intent.putExtra("extra", "alarm on4");
                    sendBroadcast(my_intent);
                } else{
                    my_intent.putExtra("extra", "alarm off");
                    sendBroadcast(my_intent);
                }
                layoutTime1.setVisibility(View.VISIBLE);
                layoutTime2.setVisibility(View.VISIBLE);
                layoutTime3.setVisibility(View.VISIBLE);
                layoutTime4.setVisibility(View.VISIBLE);
                layoutTime5.setVisibility(View.GONE);
                if(data_D_Remaining_Pack % 4 == totalPack % 4){
                    hint1.setVisibility(View.VISIBLE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.GONE);
                    alarm1.setTextColor(Color.parseColor("#464646"));
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime1) < 30){
                        hint1.setText("本次服药");
                    } else {
                        hint1.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 4 == (totalPack+3) % 4){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.VISIBLE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.GONE);
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime2) < 30){
                        hint2.setText("本次服药");
                    } else {
                        hint2.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 4 == (totalPack+2) % 4){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.VISIBLE);
                    hint4.setVisibility(View.GONE);
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime3) < 30){
                        hint3.setText("本次服药");
                    } else {
                        hint3.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 4 == (totalPack+1) % 4){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.VISIBLE);
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm3.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime4) < 30){
                        hint4.setText("本次服药");
                    } else {
                        hint4.setText("下次服药");
                    }
                }
                break;
            case 5:
                if (nowTime == clockTime1) {
                    my_intent.putExtra("extra", "alarm on1");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime2){
                    my_intent.putExtra("extra", "alarm on2");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime3){
                    my_intent.putExtra("extra", "alarm on3");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime4){
                    my_intent.putExtra("extra", "alarm on4");
                    sendBroadcast(my_intent);
                } else if(nowTime == clockTime5){
                    my_intent.putExtra("extra", "alarm on5");
                    sendBroadcast(my_intent);
                } else{
                    my_intent.putExtra("extra", "alarm off");
                    sendBroadcast(my_intent);
                }
                layoutTime1.setVisibility(View.VISIBLE);
                layoutTime2.setVisibility(View.VISIBLE);
                layoutTime3.setVisibility(View.VISIBLE);
                layoutTime4.setVisibility(View.VISIBLE);
                layoutTime5.setVisibility(View.VISIBLE);
                if(data_D_Remaining_Pack % 5 == totalPack % 5){
                    hint1.setVisibility(View.VISIBLE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.GONE);
                    hint5.setVisibility(View.GONE);
                    alarm1.setTextColor(Color.parseColor("#464646"));
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm5.setTextColor(Color.parseColor("#464646"));
                    if(Math.abs(nowTime-clockTime1) < 30){
                        hint1.setText("本次服药");
                    } else {
                        hint1.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 5 == (totalPack+4) % 5){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.VISIBLE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.GONE);
                    hint5.setVisibility(View.GONE);
                    alarm2.setTextColor(Color.parseColor("#464646"));
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm5.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime2) < 30){
                        hint2.setText("本次服药");
                    } else {
                        hint2.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 5 == (totalPack+3) % 5){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.VISIBLE);
                    hint4.setVisibility(View.GONE);
                    hint5.setVisibility(View.GONE);
                    alarm3.setTextColor(Color.parseColor("#464646"));
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm5.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime3) < 30){
                        hint3.setText("本次服药");
                    } else {
                        hint3.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 5 == (totalPack+2) % 5){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.VISIBLE);
                    hint5.setVisibility(View.GONE);
                    alarm4.setTextColor(Color.parseColor("#464646"));
                    alarm5.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm3.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime4) < 30){
                        hint4.setText("本次服药");
                    } else {
                        hint4.setText("下次服药");
                    }
                } else if (data_D_Remaining_Pack % 5 == (totalPack+1) % 5){
                    hint1.setVisibility(View.GONE);
                    hint2.setVisibility(View.GONE);
                    hint3.setVisibility(View.GONE);
                    hint4.setVisibility(View.GONE);
                    hint5.setVisibility(View.VISIBLE);
                    alarm5.setTextColor(Color.parseColor("#464646"));
                    alarm1.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm2.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm3.setTextColor(Color.parseColor("#e7e7e7"));
                    alarm4.setTextColor(Color.parseColor("#e7e7e7"));
                    if(Math.abs(nowTime-clockTime5) < 30){
                        hint5.setText("本次服药");
                    } else {
                        hint5.setText("下次服药");
                    }
                }
                break;
            default:
                break;
        }
    }



    void checkLidState(){
        if(remainingPack != data_D_Remaining_Pack){
            if(remainingPack != 0){
                int num = remainingPack - data_D_Remaining_Pack;
                NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder2 = new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("智能药盒")
                        .setContentText("取出"+num+"包药（剩余"+data_D_Remaining_Pack+"包药）");
                notifyManager.notify(1, builder2.build());

                if(num == 1){
                    switch (option){
                        case 1:
                            if(Math.abs(nowTime-clockTime1) < 30){
                                voiceRet vr = dialogResponce("PILL_1");
                                speekText(vr.output);
                            } else{
                                voiceRet vr = dialogResponce("PILL_WRONG");
                                speekText(vr.output);
                            }
                            break;
                        case 2:

                            if((data_D_Remaining_Pack+1) % 2 == totalPack % 2){
                                if(Math.abs(nowTime-clockTime1) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 2 == (totalPack+1) % 2){
                                if(Math.abs(nowTime-clockTime2) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            }
                            break;
                        case 3:
                            if((data_D_Remaining_Pack+1) % 3 == totalPack % 3){
                                if(Math.abs(nowTime-clockTime1) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 3 == (totalPack+2) % 3){
                                if(Math.abs(nowTime-clockTime2) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 3 == (totalPack+1) % 3){
                                if(Math.abs(nowTime-clockTime3) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            }
                            break;
                        case 4:
                            if((data_D_Remaining_Pack+1) % 4 == totalPack % 4){
                                if(Math.abs(nowTime-clockTime1) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 4 == (totalPack+3) % 4){
                                if(Math.abs(nowTime-clockTime2) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 4 == (totalPack+2) % 4){
                                if(Math.abs(nowTime-clockTime3) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 4 == (totalPack+1) % 4){
                                if(Math.abs(nowTime-clockTime4) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            }
                            break;
                        case 5:
                            if((data_D_Remaining_Pack+1) % 5 == totalPack % 5){
                                if(Math.abs(nowTime-clockTime1) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 5 == (totalPack+4) % 5){
                                if(Math.abs(nowTime-clockTime2) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 5 == (totalPack+3) % 5){
                                if(Math.abs(nowTime-clockTime3) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 5 == (totalPack+2) % 5){
                                if(Math.abs(nowTime-clockTime4) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            } else if ((data_D_Remaining_Pack+1) % 5 == (totalPack+1) % 5){
                                if(Math.abs(nowTime-clockTime5) < 30){
                                    voiceRet vr = dialogResponce("PILL_1");
                                    speekText(vr.output);
                                } else{
                                    voiceRet vr = dialogResponce("PILL_WRONG");
                                    speekText(vr.output);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }

                if(num == 2) {
                    voiceRet vr = dialogResponce("PILL_2");
                    speekText(vr.output);
                } else if(num == 3) {
                    voiceRet vr = dialogResponce("PILL_3");
                    speekText(vr.output);
                } else if(num == 4) {
                    voiceRet vr = dialogResponce("PILL_4");
                    speekText(vr.output);
                } else if(num == 5) {
                    voiceRet vr = dialogResponce("PILL_5");
                    speekText(vr.output);
                } else if(num == 6) {
                    voiceRet vr = dialogResponce("PILL_6");
                    speekText(vr.output);
                } else if(num > 6) {
                    voiceRet vr = dialogResponce("PILL_MORE");
                    speekText(vr.output);
                }
            }
            remainingPack = data_D_Remaining_Pack;
        }

        if(data_Lid_State != LidState){
            if(data_Lid_State == 1){
                voiceRet vr = dialogResponce("LID_OPEN");
                speekText(vr.output);
                TextView msg = new TextView(this);
                msg.setText("药盒被打开");
                msg.setPadding(0,20,0,0);
                msg.setTextSize(18);
                msg.setGravity(Gravity.CENTER);
                NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder2 = new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("智能药盒")
                        .setContentText("药盒被打开");
                notifyManager.notify(1, builder2.build());
            } else if(data_Lid_State == 2){
                voiceRet vr = dialogResponce("LID_CLOSE");
                speekText(vr.output);
                TextView msg = new TextView(this);
                msg.setText("药盒已关闭");
                msg.setTextSize(18);
                msg.setPadding(0,20,0,0);
                msg.setGravity(Gravity.CENTER);
                NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder2 = new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("智能药盒")
                        .setContentText("药盒已关闭");
                notifyManager.notify(1, builder2.build());
            }
            LidState = data_Lid_State;
        }


    }

    private void initDevice() {
        Intent intent = getIntent();
        mDevice = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
        mDevice.setListener(gizWifiDeviceListener);
        Log.i("Apptest", mDevice.getDid());
    }

    private String getDeviceName() {
        if (TextUtils.isEmpty(mDevice.getAlias())) {
            return mDevice.getProductName();
        }
        return mDevice.getAlias();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStatusOfDevice();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    /*
     * ========================================================================
     * EditText 点击键盘“完成”按钮方法
     * ========================================================================
     */



    /*
     * ========================================================================
     * 菜单栏
     * ========================================================================
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_more, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.set_number_and_time:
                FlagBtSure = 0;
                FlagRingOpen = 0;

                relativeLayout1.setVisibility(View.VISIBLE);
                linearLayout4.setVisibility(View.GONE);
                setupLayout.setVisibility(View.VISIBLE);
                break;


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Description:根据保存的的数据点的值来更新UI
     */
    protected void updateUI() {

        Log.i("show_D_Remaining_Pack", ""+data_D_Remaining_Pack);
        Log.i("show_Remaining_Pack", ""+data_Remaining_Pack);
        Log.i("show_Dosing_Time", ""+bytesToHexString(data_Dosing_Time));
        Log.i("show_Time_Now", ""+data_Time_Now);

        checkLidState();


    }

    private void setEditText(EditText et, Object value) {
        et.setText(value.toString());
        et.setSelection(value.toString().length());
        et.clearFocus();
    }

    /**
     * Description:页面加载后弹出等待框，等待设备可被控制状态回调，如果一直不可被控，等待一段时间后自动退出界面
     */
    private void getStatusOfDevice() {
        // 设备是否可控
        if (isDeviceCanBeControlled()) {
            // 可控则查询当前设备状态
            mDevice.getDeviceStatus();

        } else {
            // 显示等待栏
            progressDialog.show();
            if (mDevice.isLAN()) {
                // 小循环10s未连接上设备自动退出
                mHandler.postDelayed(mRunnable, 10000);
            } else {
                // 大循环20s未连接上设备自动退出
                mHandler.postDelayed(mRunnable, 20000);
            }
        }
    }

    /**
     * 发送指令,下发单个数据点的命令可以用这个方法
     *
     * <h3>注意</h3>
     * <p>
     * 下发多个数据点命令不能用这个方法多次调用，一次性多次调用这个方法会导致模组无法正确接收消息，参考方法内注释。
     * </p>
     *
     * @param key
     *            数据点对应的标识名
     * @param value
     *            需要改变的值
     */
    private void sendCommand(String key, Object value) {
        if (value == null) {
            return;
        }
        int sn = 5;
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(key, value);
        // 同时下发多个数据点需要一次性在map中放置全部需要控制的key，value值
        // hashMap.put(key2, value2);
        // hashMap.put(key3, value3);
        mDevice.write(hashMap, sn);
        Log.i("liang", "下发命令：" + hashMap.toString());
    }

    private boolean isDeviceCanBeControlled() {
        return mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
    }

    private void toastDeviceNoReadyAndExit() {
        Toast.makeText(this, "设备无响应，请检查设备是否正常工作", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void toastDeviceDisconnectAndExit() {
        Toast.makeText(GosDeviceControlActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 展示设备硬件信息
     *
     * @param hardwareInfo
     */
    private void showHardwareInfo(String hardwareInfo) {
        String hardwareInfoTitle = "设备硬件信息";
        new AlertDialog.Builder(this).setTitle(hardwareInfoTitle).setMessage(hardwareInfo)
                .setPositiveButton(R.string.besure, null).show();
    }

    /**
     * Description:设置设备别名与备注
     */
    private void setDeviceInfo() {

        final Dialog mDialog = new AlertDialog.Builder(this).setView(new EditText(this)).create();
        mDialog.show();

        Window window = mDialog.getWindow();
        window.setContentView(R.layout.alert_gos_set_device_info);

        final EditText etAlias;
        final EditText etRemark;
        etAlias = (EditText) window.findViewById(R.id.etAlias);
        etRemark = (EditText) window.findViewById(R.id.etRemark);

        LinearLayout llNo, llSure;
        llNo = (LinearLayout) window.findViewById(R.id.llNo);
        llSure = (LinearLayout) window.findViewById(R.id.llSure);

        if (!TextUtils.isEmpty(mDevice.getAlias())) {
            setEditText(etAlias, mDevice.getAlias());
        }
        if (!TextUtils.isEmpty(mDevice.getRemark())) {
            setEditText(etRemark, mDevice.getRemark());
        }

        llNo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        llSure.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etRemark.getText().toString())
                        && TextUtils.isEmpty(etAlias.getText().toString())) {
                    myToast("请输入设备别名或备注！");
                    return;
                }
                mDevice.setCustomInfo(etRemark.getText().toString(), etAlias.getText().toString());
                mDialog.dismiss();
                String loadingText = (String) getText(R.string.loadingtext);
                progressDialog.setMessage(loadingText);
                progressDialog.show();
            }
        });

        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideKeyBoard();
            }
        });
    }

    /*
     * 获取设备硬件信息回调
     */
    @Override
    protected void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
                                      ConcurrentHashMap<String, String> hardwareInfo) {
        super.didGetHardwareInfo(result, device, hardwareInfo);
        StringBuffer sb = new StringBuffer();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
            myToast("获取设备硬件信息失败：" + result.name());
        } else {
            sb.append("Wifi Hardware Version:" + hardwareInfo.get(WIFI_HARDVER_KEY) + "\r\n");
            sb.append("Wifi Software Version:" + hardwareInfo.get(WIFI_SOFTVER_KEY) + "\r\n");
            sb.append("MCU Hardware Version:" + hardwareInfo.get(MCU_HARDVER_KEY) + "\r\n");
            sb.append("MCU Software Version:" + hardwareInfo.get(MCU_SOFTVER_KEY) + "\r\n");
            sb.append("Wifi Firmware Id:" + hardwareInfo.get(WIFI_FIRMWAREID_KEY) + "\r\n");
            sb.append("Wifi Firmware Version:" + hardwareInfo.get(WIFI_FIRMWAREVER_KEY) + "\r\n");
            sb.append("Product Key:" + "\r\n" + hardwareInfo.get(PRODUCT_KEY) + "\r\n");

            // 设备属性
            sb.append("Device ID:" + "\r\n" + mDevice.getDid() + "\r\n");
            sb.append("Device IP:" + mDevice.getIPAddress() + "\r\n");
            sb.append("Device MAC:" + mDevice.getMacAddress() + "\r\n");
        }
        showHardwareInfo(sb.toString());
    }

    /*
     * 设置设备别名和备注回调
     */
    @Override
    protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
        super.didSetCustomInfo(result, device);
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            myToast("设置成功");
            progressDialog.cancel();
            finish();
        } else {
            myToast("设置失败：" + result.name());
        }
    }

    /*
     * 设备状态改变回调，只有设备状态为可控才可以下发控制命令
     */
    @Override
    protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
        super.didUpdateNetStatus(device, netStatus);
        if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
            mHandler.removeCallbacks(mRunnable);
            progressDialog.cancel();
        } else {
            mHandler.sendEmptyMessage(handler_key.DISCONNECT.ordinal());
        }
    }

    /*
     * 设备上报数据回调，此回调包括设备主动上报数据、下发控制命令成功后设备返回ACK
     */
    @Override
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                  ConcurrentHashMap<String, Object> dataMap, int sn) {
        super.didReceiveData(result, device, dataMap, sn);
        Log.i("liang", "接收到数据");
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS && dataMap.get("data") != null) {
            getDataFromReceiveDataMap(dataMap);
            mHandler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
        }
        @Override
        public void onError(SpeechError error) {
//            showTip(error.getPlainDescription(true));
            textView.setText("（未识别成功）\n");
            speekText("主人我没听清，再说一遍吧");
        }
        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }
        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            Log.d(TAG, "eventType:"+eventType+ "arg1:"+isLast + "arg2:" + arg2);
            // 识别结果
            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
                RecognizerResult reslut = ((RecognizerResult)obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
                recoString = parseGrammarResult(reslut.getResultString());
                textView.setText(recoString);
            }
        }
        @Override
        public void onVolumeChanged(int volume) {

        }
    };


    public static String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append(ResourceUtil.generateResourcePath(this,
                RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    String parseGrammarResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for(int j = 0; j < items.length(); j++)
                {
                    JSONObject obj = items.getJSONObject(j);
                    if(obj.getString("w").contains("nomatch"))
                    {
                        ret.append("没有匹配结果.");
                        return ret.toString();
                    }
                    switch(obj.getString("w")){
                        case "五分钟后提醒我":
                            ret.append("药盒药盒，" + obj.getString("w") + "。\n");
                            ret.append("没问题，五分钟后再提醒您。");
                            speekText("没问题，五分钟后再提醒您");
                            delayDosingTime(5);
                            break;
                        case "十分钟后提醒我":
                            ret.append("药盒药盒，" + obj.getString("w") + "。\n");
                            ret.append("好的，十分钟后再提醒您。");
                            speekText("好的，十分钟后再提醒您");
                            delayDosingTime(10);
                            break;
                        case "待会再提醒我":
                            ret.append("药盒药盒，" + obj.getString("w") + "。\n");
                            ret.append("好的，可别忘了哟。");
                            speekText("好的，可别忘了哟");
                            delayDosingTime(15);
                            break;
                        case "我不想吃药":
                            ret.append("药盒药盒，" + obj.getString("w") + "。\n");
                            ret.append("生命宝贵，可不能放弃治疗喔。");
                            speekText("生命宝贵，可不能放弃治疗喔");
                            break;
                        default:
                            break;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.append("没有匹配结果.");
        }
        return ret.toString();
    }


    public class TimeThread extends Thread {
        //重写run方法
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = MSG_ONE;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    @Override
    public void onBackPressed() {
        FlagRingOpen = 0;

        my_intent.putExtra("extra", "alarm off");
        sendBroadcast(my_intent);
        option = 0;
        clockTime1 = 0;
        clockTime2 = 0;
        clockTime3 = 0;
        clockTime4 = 0;
        clockTime5 = 0;

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        // 退出页面，取消设备订阅
        mDevice.setSubscribe(false);
        mDevice.setListener(null);
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        } else {
            showTip("唤醒未初始化");
        }

        my_intent.putExtra("extra", "alarm off");
        sendBroadcast(my_intent);
        option = 0;
        clockTime1 = 0;
        clockTime2 = 0;
        clockTime3 = 0;
        clockTime4 = 0;
        clockTime5 = 0;
    }

    String turnStr(int hour, int minute){
        if(minute == 59){
            hour ++;
            if(hour < 10){
                return "000" + hour + "0000";
            } else {
                return "0" + String.valueOf(hour).substring(0,1) + "0"
                        + String.valueOf(hour).substring(1,2) + "0000";
            }
        } else {
            minute ++;
            if(hour < 10){
                if(minute < 10){
                    return "000" + hour + "000" + minute;
                } else{
                    return "000" + hour + "0" + String.valueOf(minute).substring(0,1) + "0"
                            + String.valueOf(minute).substring(1,2);
                }
            } else {
                if(minute < 10){
                    return "0" + String.valueOf(hour).substring(0,1) + "0"
                            + String.valueOf(hour).substring(1,2) + "000" + minute;
                } else{
                    return "0" + String.valueOf(hour).substring(0,1) + "0"
                            + String.valueOf(hour).substring(1,2) + "0"
                            + String.valueOf(minute).substring(0,1) + "0"
                            + String.valueOf(minute).substring(1,2);
                }
            }
        }
    }

    void delayDosingTime(int minute){
        now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        nowTime = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);
        int mHour = now.get(Calendar.HOUR_OF_DAY);
        int mMinute = now.get(Calendar.MINUTE) + minute;
        if( mMinute >= 60){
            mHour ++;
            mMinute = mMinute - 60;
        }
        switch (option){
            case 1:
                alarm1.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                        .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                s_clock1 = alarm1.getText().toString();
                setDosingTime();
                break;
            case 2:

                if(data_D_Remaining_Pack % 2 == totalPack % 2){
                    alarm1.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock1 = alarm1.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 2 == (totalPack+1) % 2){
                    alarm2.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock2 = alarm1.getText().toString();
                    setDosingTime();
                }
                break;
            case 3:
                if(data_D_Remaining_Pack % 3 == totalPack % 3){
                    alarm1.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock1 = alarm1.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 3 == (totalPack+2) % 3){
                    alarm2.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock2 = alarm2.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 3 == (totalPack+1) % 3){
                    alarm3.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock3 = alarm3.getText().toString();
                    setDosingTime();
                }
                break;
            case 4:
                if(data_D_Remaining_Pack % 4 == totalPack % 4){
                    alarm1.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock1 = alarm1.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 4 == (totalPack+3) % 4){
                    alarm2.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock2 = alarm2.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 4 == (totalPack+2) % 4){
                    alarm3.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock3 = alarm3.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 4 == (totalPack+1) % 4){
                    alarm4.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock4 = alarm4.getText().toString();
                    setDosingTime();
                }
                break;
            case 5:
                if(data_D_Remaining_Pack % 5 == totalPack % 5){
                    alarm1.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock1 = alarm1.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 5 == (totalPack+4) % 5){
                    alarm2.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock2 = alarm2.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 5 == (totalPack+3) % 5){
                    alarm3.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock3 = alarm3.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 5 == (totalPack+2) % 5){
                    alarm4.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock4 = alarm4.getText().toString();
                    setDosingTime();
                } else if (data_D_Remaining_Pack % 5 == (totalPack+1) % 5){
                    alarm5.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : "" + mHour)
                            .append(":").append(mMinute < 10 ? "0" + mMinute : "" + mMinute));
                    s_clock5 = alarm5.getText().toString();
                    setDosingTime();
                }
                break;
            default:
                break;
        }
    }

    void setDosingTime(){
        s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
                +"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
                +"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
                +"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
                +"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
                +"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
                +"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
                +"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
                +"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
                +"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
        sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
        sendCommand(KEY_TIME_NOW, now.get(Calendar.HOUR_OF_DAY)*10000 + now.get(Calendar.MINUTE)*100 + now.get(Calendar.SECOND));
    }

}