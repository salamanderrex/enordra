package com.iflytek.voicedemo;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechUnderstander;
import com.iflytek.speech.SpeechUnderstanderListener;
import com.iflytek.speech.TextUnderstander;
import com.iflytek.speech.TextUnderstanderListener;
import com.iflytek.speech.UnderstanderResult;
import com.iflytek.speech.setting.UnderstanderSettings;
import com.iflytek.speech.util.XmlParser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class UnderstanderDemo extends Activity implements OnClickListener{
	private static String TAG = "UnderstanderDemo";
	// 语义理解对象（语音到语义）。
	private SpeechUnderstander mSpeechUnderstander;
	// 语义理解对象（文本到语义）。
	private TextUnderstander   mTextUnderstander;	
	private Toast mToast;	
	private EditText mUnderstanderText;
	
	private SharedPreferences mSharedPreferences;
	
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.understander);
		initLayout();
		
		// 初始化识别对象
		mSpeechUnderstander = new SpeechUnderstander(this, speechUnderstanderListener);
		mTextUnderstander = new TextUnderstander(this, textUnderstanderListener);
		mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
	}
	
    /**
     * 初期化监听器（语音到语义）。
     */
    private InitListener speechUnderstanderListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			Log.d(TAG, "speechUnderstanderListener init() code = " + code);
        	if (code == ErrorCode.SUCCESS) {
        		findViewById(R.id.start_understander).setEnabled(true);
        	}
		}
    };
    /**
     * 初期化监听器（文本到语义）。
     */
    private InitListener textUnderstanderListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
        	if (code == ErrorCode.SUCCESS) {
        		findViewById(R.id.text_understander).setEnabled(true);
        	}
		}
    };
	
	/**
	 * 初期化。
	 */
	private void initLayout(){
		findViewById(R.id.text_understander).setOnClickListener(this);
		findViewById(R.id.start_understander).setOnClickListener(this);

		findViewById(R.id.start_understander).setEnabled(false);
		findViewById(R.id.text_understander).setEnabled(false);
		
		mUnderstanderText = (EditText)findViewById(R.id.understander_text);
		
		findViewById(R.id.understander_stop).setOnClickListener(this);
		findViewById(R.id.understander_cancel).setOnClickListener(this);
		findViewById(R.id.image_understander_set).setOnClickListener(this);
		
		mSharedPreferences = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE);
	}
	
	@Override
	public void onClick(View view) 
	{				
		switch (view.getId()) {
		case R.id.image_understander_set:
			Intent intent = new Intent(UnderstanderDemo.this, UnderstanderSettings.class);
			startActivity(intent);
			break;
		case R.id.text_understander:
			mUnderstanderText.setText("");
			// 文本理解
			String text = "合肥明天的天气怎么样？";	
			showTip(text);
			
			if(mTextUnderstander.isUnderstanding())
				mTextUnderstander.cancel(textListener);
			int tcode = mTextUnderstander.understandText(text, textListener);
			if(tcode != 0)
			{
				showTip("Error Code："	+ tcode);
			}
			break;
		case R.id.start_understander:
			mUnderstanderText.setText("");
			
			setParam();
			// 启动语义理解
			int scode = mSpeechUnderstander.startUnderstanding(mRecognizerListener);
			if(scode != 0)
			{
				showTip("Error Code："	+ scode);
			}
			break;
		case R.id.understander_stop:
			mSpeechUnderstander.stopUnderstanding(mRecognizerListener);
			break;
		case R.id.understander_cancel:
			mSpeechUnderstander.cancel(mRecognizerListener);
			break;
		default:
			break;
		}
	}
	
	private TextUnderstanderListener textListener = new TextUnderstanderListener.Stub() {
		
		@Override
		public void onResult(final UnderstanderResult result) throws RemoteException {
	       	runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (null != result) {
			            	// 显示
							Log.d(TAG, "understander result：" + result.getResultString());
							String text = XmlParser.parseNluResult(result.getResultString());
							mUnderstanderText.setText(text);
			            } else {
			                Log.d(TAG, "understander result:null");
			                showTip("识别结果不正确。");
			            }
					}
				});
		}
		
		@Override
		public void onError(int errorCode) throws RemoteException {
			showTip("onError Code："	+ errorCode);
		}
	};
	
    /**
     * 识别回调。
     */
    private SpeechUnderstanderListener mRecognizerListener = new SpeechUnderstanderListener.Stub() {
        
        @Override
        public void onVolumeChanged(int v) throws RemoteException {
            showTip("onVolumeChanged："	+ v);
        }
        
        @Override
        public void onError(int errorCode) throws RemoteException {
			showTip("onError Code："	+ errorCode);
        }
        
        @Override
        public void onEndOfSpeech() throws RemoteException {
			showTip("onEndOfSpeech");
        }
        
        @Override
        public void onBeginOfSpeech() throws RemoteException {
			showTip("onBeginOfSpeech");
        }

		@Override
		public void onResult(final UnderstanderResult result) throws RemoteException {
	       	runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (null != result) {
			            	// 显示
							Log.d(TAG, "understander result：" + result.getResultString());
							String text = XmlParser.parseNluResult(result.getResultString());
							mUnderstanderText.setText(text);
			            } else {
			                Log.d(TAG, "understander result:null");
			                showTip("识别结果不正确。");
			            }	
					}
				});
		}
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        // 退出时释放连接
    	mSpeechUnderstander.destory();
    	mTextUnderstander.destory();        
    }
	
	private void showTip(final String str)
	{
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
	public void setParam(){
		mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, mSharedPreferences.getString("understander_language_preference", "zh_cn"));
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("understander_vadbos_preference", "4000"));
//		mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, mSharedPreferences.getString("accent_preference", "mandarin"));
//		mSpeechUnderstander.setParameter(SpeechConstant.DOMAIN, mSharedPreferences.getString("domain_perference", "iat"));
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("understander_vadeos_preference", "1000"));
//		mSpeechUnderstander.setParameter(SpeechUnderstander.SCENE, mSharedPreferences.getString("scene_preference", "all"));

		String param = null;
		param = "asr_ptt="+mSharedPreferences.getString("understander_punc_preference", "1");
		mSpeechUnderstander.setParameter(SpeechConstant.PARAMS, param+",asr_audio_path=/sdcard/iflytek/wavaudio.pcm");
	}	
}
