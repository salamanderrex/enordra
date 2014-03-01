package com.iflytek.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechSynthesizer;
import com.iflytek.speech.SynthesizerListener;
import com.iflytek.speech.setting.TtsSettings;

public class TtsDemo extends Activity implements OnClickListener {
 	private static String TAG = "TtsDemo"; 	
 	// 语音合成对象
	private SpeechSynthesizer mTts;
	private Toast mToast;
	public static String SPEAKER = "speaker"; 
	
    private SharedPreferences mSharedPreferences;
	
 	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.ttsdemo);
 		initLayout();
 		 		
 		// 初始化合成对象
 		mTts = new SpeechSynthesizer(this, mTtsInitListener);
 		mToast = Toast.makeText(this,"",Toast.LENGTH_LONG); 		
 	}
 	
 	/**
 	 * 初期化。
 	 */
 	private void initLayout() {
 		findViewById(R.id.tts_play).setOnClickListener(this);
 		findViewById(R.id.tts_play).setEnabled(false);
 		
 		findViewById(R.id.tts_cancel).setOnClickListener(this);
 		findViewById(R.id.tts_pause).setOnClickListener(this);
 		findViewById(R.id.tts_resume).setOnClickListener(this);
 		findViewById(R.id.image_tts_set).setOnClickListener(this);
 		
 		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
 	}	

 	@Override
 	public void onClick(View view) {
 		switch(view.getId()) {
			case R.id.image_tts_set:
				String speaker = mTts.getParameter(SpeechSynthesizer.LOCAL_SPEAKERS);
				System.out.println("yi xia zai fa yin ren" + speaker);
				Intent intent = new Intent(TtsDemo.this, TtsSettings.class);
				intent.putExtra(SPEAKER, speaker);
				startActivity(intent);
				break;
			case R.id.tts_play:
				
				String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
				setParam();
				// 设置参数
				int code = mTts.startSpeaking(text, mTtsListener);
				if (code != 0) {
					showTip("start speak error : " + code);
				} else
					showTip("start speak success.");
				break;
			case R.id.tts_cancel:
				mTts.stopSpeaking(mTtsListener);
				break;
			case R.id.tts_pause:
				mTts.pauseSpeaking(mTtsListener);
				break;
			case R.id.tts_resume:
				mTts.resumeSpeaking(mTtsListener);
				break;
 		}
 	}

	/**
     * 初期化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			Log.d(TAG, "InitListener init() code = " + code);
        	if (code == ErrorCode.SUCCESS) {
        		((Button)findViewById(R.id.tts_play)).setEnabled(true);
        	}
		}
    };
        
    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener.Stub() {
        @Override
        public void onBufferProgress(int progress) throws RemoteException {
        	 Log.d(TAG, "onBufferProgress :" + progress);
//        	 showTip("onBufferProgress :" + progress);
        }

        @Override
        public void onCompleted(int code) throws RemoteException {
        	Log.d(TAG, "onCompleted code =" + code);
            showTip("onCompleted code =" + code);
        }

        @Override
        public void onSpeakBegin() throws RemoteException {
            Log.d(TAG, "onSpeakBegin");
            showTip("onSpeakBegin");
        }

        @Override
        public void onSpeakPaused() throws RemoteException {
        	 Log.d(TAG, "onSpeakPaused.");
        	 showTip("onSpeakPaused.");
        }

        @Override
        public void onSpeakProgress(int progress) throws RemoteException {
        	Log.d(TAG, "onSpeakProgress :" + progress);
        	showTip("onSpeakProgress :" + progress);
        }

        @Override
        public void onSpeakResumed() throws RemoteException {
        	Log.d(TAG, "onSpeakResumed.");
        	showTip("onSpeakResumed");
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking(mTtsListener);
        // 退出时释放连接
        mTts.destory();
    }
	
	private void showTip(final String str){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
		    }
		});
	}
	
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		mTts.setParameter(SpeechConstant.ENGINE_TYPE,
				mSharedPreferences.getString("engine_preference", "local"));
		
		if(mSharedPreferences.getString("engine_preference", "local").equalsIgnoreCase("local")){
			mTts.setParameter(SpeechSynthesizer.VOICE_NAME,
					mSharedPreferences.getString("role_cn_preference", "xiaoyan"));
		}else{
			mTts.setParameter(SpeechSynthesizer.VOICE_NAME,
					mSharedPreferences.getString("role_cn_preference", "xiaoyan")); 
		}
		mTts.setParameter(SpeechSynthesizer.SPEED,
				mSharedPreferences.getString("speed_preference", "50"));
		
		mTts.setParameter(SpeechSynthesizer.PITCH,
				mSharedPreferences.getString("pitch_preference", "50"));
		
		mTts.setParameter(SpeechSynthesizer.VOLUME,
				mSharedPreferences.getString("volume_preference", "50"));
	}
}
