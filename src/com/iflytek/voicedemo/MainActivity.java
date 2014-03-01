package com.iflytek.voicedemo;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.speech.SpeechUtility;
import com.iflytek.speech.util.ApkInstaller;

public class MainActivity extends Activity implements OnClickListener{
	private Toast mToast;
	private Handler mHandler;
	private Dialog mLoadDialog;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// 设置标题栏（无标题）
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		SimpleAdapter listitemAdapter = new SimpleAdapter();		
        ((ListView)findViewById(R.id.listview_main)).setAdapter(listitemAdapter);
        mHandler=new Myhandler();
	}
	@Override
	public void onClick(View view) {
		if(view.getTag() == null)
		{
			showTip("未知错误");
			return;
		}
		
		// 没有可用的引擎
//		if (SpeechUtility.getUtility(this).queryAvailableEngines() == null
//				|| SpeechUtility.getUtility(this).queryAvailableEngines().length <= 0) {
//			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//			dialog.setMessage(getString(R.string.download_confirm_msg));
//			dialog.setNegativeButton(R.string.dialog_cancel_button, null);			
//			dialog.setPositiveButton(getString(R.string.dialog_confirm_button),
//					new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialoginterface, int i) {
//							String url = SpeechUtility.getUtility(MainActivity.this).getComponentUrl();
//							String assetsApk="SpeechService.apk";
//                           processInstall(MainActivity.this,url,assetsApk);
//						}
//					});
//			dialog.show();			
//			return;
//		}
//		String []engines=SpeechUtility.getUtility(this).queryAvailableEngines();
//		if(engines!=null){
//			int engineNum=engines.length;
//			
//			for(int i=0;i<engineNum;i++){
//				Log.d("debug", "-----Flag---SpeechDemo---MainActivity--OnCreate---engineList["+i+"]=="+engines[i]);
//			}
//		}
		
//		if (SpeechUtility.getUtility(this).queryAvailableEngines() == null
//				|| SpeechUtility.getUtility(this).queryAvailableEngines().length <= 0) 
		if(!checkSpeechServiceInstall()){
			final Dialog dialog=new Dialog(this,R.style.dialog);
			
			LayoutInflater inflater = getLayoutInflater();
			View alertDialogView = inflater.inflate(R.layout.superman_alertdialog, null);
			dialog.setContentView(alertDialogView);
			Button okButton = (Button) alertDialogView.findViewById(R.id.ok);
			Button cancelButton = (Button) alertDialogView.findViewById(R.id.cancel);
			TextView comeText=(TextView) alertDialogView.findViewById(R.id.title);
//			SpannableString spanString=new SpannableString(comeText.getText());
//			spanString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, spanString.length(), SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
			comeText.setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
//			TextView contentText=(TextView) alertDialogView.findViewById(R.id.content);
			okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					mLoadDialog = new AlertDialog.Builder(MainActivity.this).create();
					mLoadDialog.show();
			        // 注意此处要放在show之后 否则会报异常
					mLoadDialog.setContentView(R.layout.loading_process_dialog_anim);
			        Message message=new Message();
			        message.what=0;
			        mHandler.sendMessage(message);
				}
			});
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();			
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = (int)(display.getWidth()); //设置宽度
			dialog.getWindow().setAttributes(lp);
			
			return;
		}
		// 设置你申请的应用appid
		SpeechUtility.getUtility(MainActivity.this).setAppid("4d6774d0");
		int tag = Integer.parseInt(view.getTag().toString());
		Intent intent = null;
		if(tag == 0)
		{
			// 语音转写
			intent = new Intent(this, IatDemo.class);
		}else if(tag == 1)
		{
			// 语义理解
			intent = new Intent(this, UnderstanderDemo.class);
		}else if(tag == 2)
		{
			// 语法识别
			intent = new Intent(this, AbnfDemo.class);
		}else if(tag==3)
		{
			// 合成
			intent = new Intent(this, TtsDemo.class);
		}
		startActivity(intent);		
	}
	
	private class SimpleAdapter extends BaseAdapter{

	   	  public SimpleAdapter() 
	   	  {
	   	       super();
	   	  }
	   	  public View getView(int position, View convertView, ViewGroup parent) 
	   	  {	   		  
	   		  if(null == convertView){
	   			  LayoutInflater factory = LayoutInflater.from(MainActivity.this);
	  			  View mView = factory.inflate(R.layout.list_items, null);
	  			  convertView = mView;
	   		  }
	   		  Button btn = (Button)convertView.findViewById(R.id.btn);
	   		  btn.setOnClickListener(MainActivity.this);
	   		  btn.setTag(position);
	   		  
	   		  if(position == 0)
	   		  {
	   			btn.setText("立刻体验语音听写");
	   		  }else if(position == 1)
	   		  {
	   			btn.setText("立刻体验语义理解");
	   		  }else if(position == 2)
	   		  {
	   			btn.setText("立刻体验命令词识别");
		   	  }else if(position == 3)
		   	  {
		   		btn.setText("立刻体验语音合成");
		   	  }
	   		  return convertView;
	   	  }

	   	@Override
		public int getCount() {
  	  		return 4;
		}
  	  	
  	  	@Override
		public Object getItem(int position) {
			return null;
		}
	
		@Override
		public long getItemId(int position) {
			return 0;
		}
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
	 * 如果服务组件没有安装，有两种安装方式。
	 * 1.直接打开语音服务组件下载页面，进行下载后安装。
	 * 2.把服务组件apk安装包放在assets中，为了避免被编译压缩，修改后缀名为mp3，然后copy到SDcard中进行安装。
	 */
	private boolean processInstall(Context context ,String url,String assetsApk){
		// 直接下载方式
//		ApkInstaller.openDownloadWeb(context, url);
		// 本地安装方式
		if(!ApkInstaller.installFromAssets(context, assetsApk)){
		    Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_SHORT).show();
		    return false;
		}
		return true;		
	}

	class Myhandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				String url = SpeechUtility.getUtility(MainActivity.this)
						.getComponentUrl();
				String assetsApk = "SpeechService.apk";
				if (processInstall(MainActivity.this, url, assetsApk)) {
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				}
				break;
			case 1:
				if (mLoadDialog != null) {
					mLoadDialog.dismiss();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}
	 
	 // 判断手机中是否安装了讯飞语音+
	 private boolean checkSpeechServiceInstall(){
		 String packageName = "com.iflytek.speechcloud";
		 List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		 for(int i = 0; i < packages.size(); i++){
			 PackageInfo packageInfo = packages.get(i);
			 if(packageInfo.packageName.equals(packageName)){
				 return true;
			 }else{
				 continue;
			 }
		 }
		 return false;
	 }
}
