package com.iflytek.voicedemo;

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.GrammarListener;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.LexiconListener;
import com.iflytek.speech.RecognizerListener;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechRecognizer;
import com.iflytek.speech.setting.AbnfSettings;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.speech.util.XmlParser;

public class AbnfDemo extends Activity implements OnClickListener{
	private static String TAG = "AbnfDemo";
	// 语音识别对象
	private SpeechRecognizer mRecognizer;
	private Toast mToast;	
	// 缓存
	private SharedPreferences mSharedPreferences;
	private SharedPreferences mParamPreferences;
	private String mLocalGrammar = null;
	private String mCloudGrammar = null;
	private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
	private static final String GRAMMAR_TYPE = "abnf";
	private String mEngineType = "cloud";
	
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.abnfdemo);	
		initLayout();
		
		// 初始化识别对象
		mRecognizer = new SpeechRecognizer(this, mInitListener);
		mToast = Toast.makeText(this,"",Toast.LENGTH_LONG);
	}
	
	/**
	 * 初期化。
	 */
	private void initLayout(){
		findViewById(R.id.isr_recognize).setOnClickListener(this);
		findViewById(R.id.isr_recognize).setEnabled(false);
		
		findViewById(R.id.isr_build_grammar).setOnClickListener(this);
		findViewById(R.id.isr_stop).setOnClickListener(this);
		findViewById(R.id.isr_cancel).setOnClickListener(this);
		findViewById(R.id.isr_lexcion).setOnClickListener(this);
		findViewById(R.id.image_abnf_set).setOnClickListener(this);
		
		// 初始化缓存对象
		mSharedPreferences = getSharedPreferences(getPackageName(),	MODE_PRIVATE);
		
		mLocalGrammar = readFile("call.bnf", "utf-8");
		mCloudGrammar = readFile("grammar_sample.abnf","gb2312");

		mParamPreferences = getSharedPreferences(AbnfSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		
		mEngineType = mParamPreferences.getString("abnf_engine_preference", "cloud");
		if(mEngineType.equalsIgnoreCase("cloud"))
		{
			((EditText)findViewById(R.id.isr_text)).setText(getString(R.string.text_isr_abnf_hint));
		}else{
			((EditText)findViewById(R.id.isr_text)).setText(mLocalGrammar);
		}
	}

	/**
     * 初期化监听器。
     */
    private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
        	if (code == ErrorCode.SUCCESS) {
        		findViewById(R.id.isr_recognize).setEnabled(true);
        	}
		}
    };		
    
	@Override
	public void onClick(View view) {		
		switch(view.getId())
		{
			case R.id.image_abnf_set:
				Intent intent = new Intent(AbnfDemo.this,AbnfSettings.class);
				startActivityForResult(intent, 100);
				break;
			case R.id.isr_build_grammar:
				// 取得语法内容
				String[] mGrammars = new String[0]; 
				for (String grammar : mGrammars) {
					Log.d("", grammar);
				}
				
				String grammarContent;
				if("cloud".equalsIgnoreCase(mEngineType))
				{
					grammarContent = new String(mCloudGrammar);
					mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ENCODEING,"gb2312");
				}
				else
				{
					grammarContent = new String(mLocalGrammar);
					mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ENCODEING,"utf-8");
				}
				
				mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				
				int ret = mRecognizer.buildGrammar(GRAMMAR_TYPE, grammarContent, grammarListener);
				if(ret != ErrorCode.SUCCESS)
					showTip("语法构建失败：" + ret);
				break;
			case R.id.isr_lexcion:
				
				mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				int result;
				if("cloud".equalsIgnoreCase(mEngineType))
				{
					// 更新用户词典，云端上传的词典只在听写时生效
					mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ENCODEING, "utf-8");
					result =mRecognizer.updateLexicon("userword", readFile("userwords", "utf-8"), lexiconListener);
					if(result == 0)
						showTip("更新词典成功!\n云端更新词典在听写中生效\n内容如下：\n\t随机存储器\n\t只读存储器\n\t扩充数据输出\n");
				}
				else
				{
					// 本地上传的联系人在命令词识别时生效
					mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_LIST, "call");
					result =mRecognizer.updateLexicon("<contact>", "张海羊\n刘婧\n王锋\n", lexiconListener);	
					if(result == 0)
						showTip("更新词典成功!\n内容如下：\n\t张海羊\n\t刘婧\n\t王锋\n");
				}
				break;
			case R.id.isr_recognize:
				((EditText)findViewById(R.id.isr_text)).setText("");
				
				mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				
				mRecognizer.setParameter(SpeechConstant.VAD_BOS, mParamPreferences.getString("abnf_vadbos_preference", "4000"));
				mRecognizer.setParameter(SpeechConstant.VAD_EOS, mParamPreferences.getString("abnf_vadeos_preference", "700"));
				
				if("cloud".equalsIgnoreCase(mEngineType))
				{
					((EditText)findViewById(R.id.isr_text)).setHint(getString(R.string.text_isr_abnf_hint));
					String grammarId = mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
					if(TextUtils.isEmpty(grammarId))
					{
						showTip("请先构建语法。");
						return;
					}
					mRecognizer.setParameter(SpeechRecognizer.CLOUD_GRAMMAR, grammarId);
				}
				else
				{
					((EditText)findViewById(R.id.isr_text)).setHint(mLocalGrammar);
					mRecognizer.setParameter(SpeechConstant.PARAMS, "local_grammar=call,mixed_threshold=40");
					//mRecognizer.setParameter(SpeechConstant.PARAMS, "local_scn=call");
					//mRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ID, "call");
				}
				//mRecognizer.setParameter(SpeechConstant.PARAMS, "mixed_threshold=20");
				int recode = mRecognizer.startListening(mRecognizerListener);
				if(recode != ErrorCode.SUCCESS)
					showTip("语法识别失败：" + recode);
				break;
			case R.id.isr_stop:
				mRecognizer.stopListening(mRecognizerListener);
				break;			
			case R.id.isr_cancel:
				mRecognizer.cancel(mRecognizerListener);
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		mRecognizer.cancel(mRecognizerListener);
		mRecognizer.destory();
		super.onDestroy();
	}
	
	private LexiconListener lexiconListener = new LexiconListener.Stub() {
		
		@Override
		public void onLexiconUpdated(String arg0, int arg1) throws RemoteException {
			if(ErrorCode.SUCCESS != arg1)
				showTip("词典更新失败，错误码："+arg1);
		}
	};
	
	private GrammarListener grammarListener = new GrammarListener.Stub() {
		
		@Override
		public void onBuildFinish(String grammarId, int errorCode) throws RemoteException {
			if(errorCode == ErrorCode.SUCCESS){
				String grammarID = new String(grammarId);
				Editor editor = mSharedPreferences.edit();
				if(!TextUtils.isEmpty(grammarId))
					editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
				editor.commit();				
				showTip("语法构建成功：" + grammarId);
			}else{
				showTip("语法构建失败，错误码：" + errorCode);
			}
		}
	};
	
	/**
     * 识别回调。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener.Stub() {
        
        @Override
        public void onVolumeChanged(int v) throws RemoteException {
            showTip("onVolumeChanged："	+ v);
        }
        
        @Override
        public void onResult(final RecognizerResult result, boolean isLast)
                throws RemoteException {
        	runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
						if(mEngineType.equalsIgnoreCase("cloud"))
						{
							Log.i(TAG, "recognizer result：" + result.getResultString());
							String text = JsonParser.parseGrammarResult(result.getResultString());
			            	// 显示
							((EditText)findViewById(R.id.isr_text)).setText(text);  
						}else{
							Log.i(TAG, "recognizer result：" + result.getResultString());
							String text = XmlParser.parseNluResult(result.getResultString());
			            	// 显示
							((EditText)findViewById(R.id.isr_text)).setText(text);  
						}
						              
		            } else {
		                Log.d(TAG, "recognizer result : null");
		            }	
					//mUiHandler.sendMessageDelayed(new Message(), 2000);
				}
			});
            
        }
        
        @Override
        public void onError(int errorCode) throws RemoteException {
			showTip("onError Code："	+ errorCode);
			//mUiHandler.sendMessageDelayed(new Message(), 2000);
        }
        
        @Override
        public void onEndOfSpeech() throws RemoteException {
			showTip("onEndOfSpeech");
        }
        
        @Override
        public void onBeginOfSpeech() throws RemoteException {
			showTip("onBeginOfSpeech");
        }
    };
    
    /**
     * setting activity结果回调
     * @return
     */
    @Override  
	protected void onActivityResult(int requestCode, int resultCode,Intent data) {
		mEngineType = mParamPreferences.getString("abnf_engine_preference", "cloud");
		if("cloud".equalsIgnoreCase(mEngineType))
		{
			((EditText)findViewById(R.id.isr_text)).setText(getString(R.string.text_isr_abnf_hint));
		}else{
			((EditText)findViewById(R.id.isr_text)).setText(mLocalGrammar);
		}
		
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    
	/**
	 * 读取语法文件。
	 * @return
	 */
	private String readFile(String file,String code)
	{
		int len = 0;
		byte []buf = null;
		String grammar = "";
		try {
			InputStream in = getAssets().open(file);			
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			
			grammar = new String(buf,code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return grammar;
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
}
