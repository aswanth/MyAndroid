package com.QLog.qlogtest;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QLogListActivity extends Activity {
	
	private ListView _loglist;
	private String _filenametag = "QLog.";
	private boolean[] _checkStatusArray = new boolean[10];
	private Button _sendmailbutton;
	private QLogFileManager _qlogFileManager;
	private TextView _model, _brand, _version, _appid, _deviceid;
	private String _modelValue;
	private String _versionValue;
	private String _brandValue;
	private String _appIdValue;
	private String _deviceIdValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qlog_list);
		
		_qlogFileManager = new QLogFileManager();
		initViews();
		setUpViews();
		setListeners();
		
		_loglist.setAdapter(new QLogListAdapter(this, _qlogFileManager.getLogFileCount()));
	}
	
	public void setListeners() {
		_sendmailbutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ArrayList<String> filePaths = new ArrayList();
				for(int i = 0; i < _checkStatusArray.length; i++){
					if(_checkStatusArray[i]){
						filePaths.add(_qlogFileManager.getFilePath(i+1));
					}
				}
				if(filePaths.size() > 0){
					email(filePaths);
				}else {
					Toast.makeText(QLogListActivity.this, "No attachments added..", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	}
	
	public void initViews() {
		_sendmailbutton = (Button) findViewById(R.id.sendmail);
		_loglist = (ListView) findViewById(R.id.loglist);
		_model = (TextView) findViewById(R.id.phonemodelvalue);
		_brand = (TextView) findViewById(R.id.brandvalue);
		_version = (TextView) findViewById(R.id.androidversionvalue);
		_appid = (TextView) findViewById(R.id.appidvalue);
		_deviceid = (TextView) findViewById(R.id.deviceidvalue);
		
	}
	
	public void setUpViews() {
				_modelValue = android.os.Build.MODEL;
				_versionValue = android.os.Build.VERSION.RELEASE;
				_brandValue = android.os.Build.BRAND;
				_appIdValue = getApplication().getPackageName();
//				String deviceid = android.os.Build.SERIAL;
				_deviceIdValue = Secure.getString(getContentResolver(),
                        Secure.ANDROID_ID);
				
				_model.setText(": " + _modelValue);
				_brand.setText(": " + _brandValue);
				_version.setText(": " + _versionValue);
				_appid.setText(": " + _appIdValue);
				_deviceid.setText(": " + _deviceIdValue);
				
	}
	
	public String getEmailbody() {
		
		String body = "Device details : \n\n\t Model : " + _modelValue +
				"\n\n\t Brand : " + _brandValue +
				"\n\n\t Android version : " + _versionValue +
				"\n\n\t Device id : " + _deviceIdValue +
				"\n\n\t Package name : " + _appIdValue ;
		return body;
	}
	
	public void email( ArrayList<String> filePaths)
		{
		
			String emailTo = "aswanth@qburst.com";
			String emailCC = "";
			String subject = "QLog";
			String emailText = getEmailbody();
		
		    //need to "send multiple" to get more than one attachment
		    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		    emailIntent.setType("text/plain");
		    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, 
		    		new String[] {emailTo, "aswanthv3@gmail.com"});
		    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		    emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
//		    emailIntent.putExtra(android.content.Intent.EXTRA_CC, 
//		        new String[]{emailCC});
		    //has to be an ArrayList
		    ArrayList<Uri> uris = new ArrayList<Uri>();
		    //convert from paths to Android friendly Parcelable Uri's
		    for (String file : filePaths)
		    {
		        File fileIn = new File(file);
//		        fileIn.setReadable(true);
		        Uri u = Uri.fromFile(fileIn);
		        uris.add(u);
		    }
		    emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.qlog_list, menu);
		return true;
	}
	
	class QLogListAdapter extends BaseAdapter{

		private int _logcount;
		private Activity _activity;
		private LayoutInflater _inflater;
		private String[] _logfilelist;
		
		public QLogListAdapter(Activity activity, int count) {
			
			_logcount = count;
			_activity = activity;
			_inflater = (LayoutInflater) _activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			
		}
		
		@Override
		public int getCount() {
			return _logcount;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			View view = convertView;
			if(view == null){
				view = (View)_inflater.inflate(R.layout.loglistitemview,
                        null);
			}
			int pos = position + 1;
			((TextView) view.findViewById(R.id.filename)).setText(_filenametag + pos);
			((CheckBox) view.findViewById(R.id.filecheckbox)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
						_checkStatusArray[position] = isChecked;
						Log.i("onCheckedChanged", "" + position);
					
				}
			});
			
			
			return view;
		}
		
	}
	

}
