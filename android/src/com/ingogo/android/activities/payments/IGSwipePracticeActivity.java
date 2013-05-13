package com.ingogo.android.activities.payments;

import java.util.Map;

import IDTech.MSR.uniMag.UniMagReader;
import IDTech.MSR.uniMag.UniMagReaderMsg;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.activities.IGPaymentBaseActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.cardreader.helpers.CardInfo;
import com.ingogo.android.cardreader.helpers.CardInfoParseException;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;

public class IGSwipePracticeActivity extends IGPaymentBaseActivity implements
		 UniMagReaderMsg {

	private ImageButton _intialiseCardReader, _exit;
	Button _themeButton;
	private ImageButton _swipeBtn1, _swipeBtn2, _swipeBtn3, _swipeBtn4,
			_swipeBtn5;
	private TextView _swipeMessageTv;
	private ProgressDialog _progressDialog;
	private CardInfo _cardInfo;
	private boolean _isBadSwipe;
	private ProgressDialog _readerDialog;
	private static int _screenStatus;
	private static int _successCount;
	private boolean _isSwipeInactive = true;

	private static boolean _isBadReadRefresh = true;
	private boolean _initUnimagInProgress;
	private String _unimagMessage;
	Handler _unimagHandler = new Handler();
	private UniMagReader unimagReader = null ; 
	private String _cardDataString;
	private boolean _isUnimagReaderConnected;

	public static interface swipeStatus {
		int initialising = 1;
		int badswipe = 2;
		int cardnotready = 3;
		int defaultStatus = 4;
		int pleaseSwipe = 5;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swipe_card_practice);

		int status = _screenStatus;
		_isBadSwipe = false;

		initViews();

		if (!isDeviceConnected()) {
			Exception newExp = new Exception("Card reader not attached: ");
			IGUtility.logExceptionInQLogger(newExp);

		}

		if (status == swipeStatus.badswipe) {
			_swipeMessageTv.setText("BAD READ! YOU SWIPED " + _successCount
					+ " IN A ROW, \n TRY AND BEAT YOUR SCORE");
			_isBadSwipe = true;
		} else {
			if (_successCount > 0) {
				if (_successCount == 5) {
					_swipeMessageTv
							.setText("CONGRATULATIONS! \n YOU HAVE PASSED THE SWIPE TEST");
				} else {
					_swipeMessageTv.setText("SUCCESSFUL READS: "
							+ _successCount + " IN A ROW");
				}
			} else {
				_swipeMessageTv.setText("Swipe card please... ");
			}
		}

		initSwipePractice();

		if (!_isBadReadRefresh) {
			new Handler().postDelayed(new Runnable() {
				// @Override
				public void run() {
					if (_isBadSwipe) {
						_successCount = 0;
						resetSwipePractice();
						_isBadReadRefresh = true;
					}
				}
			}, 3000);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case menuEnumerator.JOBS:
			_screenStatus = swipeStatus.defaultStatus;
			_successCount = 0;
			break;
		case menuEnumerator.LOGOUT:
			_screenStatus = swipeStatus.defaultStatus;
			_successCount = 0;
			break;
		default:
			break;
		}

		super.onOptionsItemSelected(item);
		return true;
	}

	@Override
	protected void onResume() {
		if(unimagReader!=null)
		{
			unimagReader.registerListen();
		}
		// TODO Auto-generated method stub
		super.onResume();
		// set speaker phone off
		// AudioManager audioManager = (AudioManager)
		// getSystemService(getApplicationContext().AUDIO_SERVICE);
		// audioManager.setMode(AudioManager.MODE_NORMAL);
		// audioManager.setSpeakerphoneOn(false);
		if (isDeviceConnected()) {
			Exception newExp = new Exception("Swipe practise started");
			IGUtility.logExceptionInQLogger(newExp);
		}

		IGUpdatePositionPollingTask.ignoreStaleState = true;
		if (_cardInfo == null) {
			if (_screenStatus != swipeStatus.cardnotready) {
				onSwipeInit();
			} else {
				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				_swipeMessageTv.setText("CARD READER NOT READY...");
				_exit.setEnabled(true);

			}
		}
	}

	private void initViews() {
		_intialiseCardReader = (ImageButton) findViewById(R.id.intCardReaderBtn);
		_exit = (ImageButton) findViewById(R.id.exitBtn);
		_swipeMessageTv = (TextView) findViewById(R.id.swipeMsgTv);
		_themeButton = (Button) findViewById(R.id.themeButton);

		_swipeBtn1 = (ImageButton) findViewById(R.id.swipeButton1);
		_swipeBtn2 = (ImageButton) findViewById(R.id.swipeButton2);
		_swipeBtn3 = (ImageButton) findViewById(R.id.swipeButton3);
		_swipeBtn4 = (ImageButton) findViewById(R.id.swipeButton4);
		_swipeBtn5 = (ImageButton) findViewById(R.id.swipeButton5);
	}
	
	/**
	 * Button action to change the current theme If the current theme is day
	 * then set night as current theme and restart the activity. If the current
	 * theme is night then set day as current theme and restart the activity.
	 * 
	 * @param view
	 */
	public void changeTheme(View view) {
		if (IngogoApp.getThemeID() == 1) {
			IngogoApp.setThemeID(2);
		} else {
			IngogoApp.setThemeID(1);
		}
		resetUnimagReader();


		restartActivty();
	}
	
	/**
	 * Function to restart the activity to apply the new theme.
	 */
	private void restartActivty() {

		Intent intent = getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	private void initSwipePractice() {

		if (_successCount > 5) {
			_successCount = 1;
		}

		if (_successCount == 0) {
			_swipeBtn1.setImageResource(R.drawable.swipe_gray);
			_swipeBtn2.setImageResource(R.drawable.swipe_gray);
			_swipeBtn3.setImageResource(R.drawable.swipe_gray);
			_swipeBtn4.setImageResource(R.drawable.swipe_gray);
			_swipeBtn5.setImageResource(R.drawable.swipe_gray);

		} else if (_successCount == 1) {
			_swipeBtn1.setImageResource(R.drawable.swipe_blue);
			_swipeBtn2.setImageResource(R.drawable.swipe_gray);
			_swipeBtn3.setImageResource(R.drawable.swipe_gray);
			_swipeBtn4.setImageResource(R.drawable.swipe_gray);
			_swipeBtn5.setImageResource(R.drawable.swipe_gray);
		} else if (_successCount == 2) {
			_swipeBtn1.setImageResource(R.drawable.swipe_blue);
			_swipeBtn2.setImageResource(R.drawable.swipe_blue);
			_swipeBtn3.setImageResource(R.drawable.swipe_gray);
			_swipeBtn4.setImageResource(R.drawable.swipe_gray);
			_swipeBtn5.setImageResource(R.drawable.swipe_gray);
		} else if (_successCount == 3) {
			_swipeBtn1.setImageResource(R.drawable.swipe_blue);
			_swipeBtn2.setImageResource(R.drawable.swipe_blue);
			_swipeBtn3.setImageResource(R.drawable.swipe_blue);
			_swipeBtn4.setImageResource(R.drawable.swipe_gray);
			_swipeBtn5.setImageResource(R.drawable.swipe_gray);
		} else if (_successCount == 4) {
			_swipeBtn1.setImageResource(R.drawable.swipe_blue);
			_swipeBtn2.setImageResource(R.drawable.swipe_blue);
			_swipeBtn3.setImageResource(R.drawable.swipe_blue);
			_swipeBtn4.setImageResource(R.drawable.swipe_blue);
			_swipeBtn5.setImageResource(R.drawable.swipe_gray);
		} else if (_successCount == 5) {
			_swipeBtn1.setImageResource(R.drawable.swipe_blue);
			_swipeBtn2.setImageResource(R.drawable.swipe_blue);
			_swipeBtn3.setImageResource(R.drawable.swipe_blue);
			_swipeBtn4.setImageResource(R.drawable.swipe_blue);
			_swipeBtn5.setImageResource(R.drawable.swipe_blue);
		}

		if (_isBadSwipe) {
			if (_successCount == 1) {
				_swipeBtn2.setImageResource(R.drawable.swipe_red);
			} else if (_successCount == 2) {
				_swipeBtn3.setImageResource(R.drawable.swipe_red);
			} else if (_successCount == 3) {
				_swipeBtn4.setImageResource(R.drawable.swipe_red);
			} else if (_successCount == 4) {
				_swipeBtn5.setImageResource(R.drawable.swipe_red);
			} else if (_successCount == 5 || _successCount == 0) {
				_swipeBtn1.setImageResource(R.drawable.swipe_red);
				_swipeBtn2.setImageResource(R.drawable.swipe_gray);
				_swipeBtn3.setImageResource(R.drawable.swipe_gray);
				_swipeBtn4.setImageResource(R.drawable.swipe_gray);
				_swipeBtn5.setImageResource(R.drawable.swipe_gray);
			}
		}

	}

	private void resetSwipePractice() {
		_swipeBtn1.setImageResource(R.drawable.swipe_gray);
		_swipeBtn2.setImageResource(R.drawable.swipe_gray);
		_swipeBtn3.setImageResource(R.drawable.swipe_gray);
		_swipeBtn4.setImageResource(R.drawable.swipe_gray);
		_swipeBtn5.setImageResource(R.drawable.swipe_gray);

		_swipeMessageTv.setText("Swipe card please... ");

		_isBadSwipe = false;
		_screenStatus = swipeStatus.defaultStatus;
	}

	@Override
	public void onBackPressed() {
		if (_isSwipeInactive) {
			Exception newExp = new Exception("TEST SWIPE COMPLETE: "
					+ "Success count = " + _successCount + " ");
			IGUtility.logExceptionInQLogger(newExp);
			_screenStatus = swipeStatus.defaultStatus;
			_successCount = 0;
			finish();
		}
	}
	
	private void showReaderDialog() {
		if (_readerDialog!=null) {
			if (!_readerDialog.isShowing()) {
				_readerDialog = IGUtility.showProgressDialogWithMsg(this,
				"Initialising Card reader. Please wait...");
			}
		} else {
			_readerDialog = IGUtility.showProgressDialogWithMsg(this,
			"Initialising Card reader. Please wait...");
		}
	}


	/**
	 * to initialize a reader
	 */
	private void onSwipeInit() {
		onUnimagSwipeInit();
       
	}
	
	/**
	 * to initialize a unimag reader
	 */	
	private void onUnimagSwipeInit() {

		if (!_initUnimagInProgress) {
			_cardInfo = null;
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {
				
					showReaderDialog();
					// _swipeMessageTv.setText("");
					_initUnimagInProgress = true;
					
					if(unimagReader==null)
						unimagReader =  new UniMagReader(this,this);
			        unimagReader.registerListen();
			        unimagReader.setTimeoutOfSwipeCard(10000);
			        String fileNameWithPath = IGUtility.getXMLFileFromRaw();
			        if(!IGUtility.isFileExist(fileNameWithPath)) { fileNameWithPath = null; } 
			        unimagReader.setXMLFileNameWithPath(fileNameWithPath);
			        unimagReader.loadingConfigurationXMLFile(true);
			        Log.e("UNIMAG", "UNIMAG" + "SDK--> "+ unimagReader.getSDKVersionInfo());

				} else {
					_isUnimagReaderConnected = false;
					_initUnimagInProgress = false;
					_swipeMessageTv.setText("CARD READER NOT READY...");
					IGUtility.dismissProgressDialog(_readerDialog);
					_themeButton.setClickable(true);

					Dialog dlg = new AlertDialog.Builder(IGSwipePracticeActivity.this)
							.setTitle("")
							.setMessage(
									"Attach card reader and then select the Initialise Card Reader")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									_exit.setEnabled(true);
									_intialiseCardReader.setEnabled(true);
								}
							}).create();
					dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
					dlg.setCancelable(false);
					dlg.show();

					Log.i("CARDREADER", "CARDREADER- onReaderDisconnected");
					_screenStatus = swipeStatus.defaultStatus;
					_successCount = 0;
					_isBadSwipe = false;

					initSwipePractice();

					_swipeMessageTv.setText("CARD READER NOT READY...");
					// _payOffline.setVisibility(View.VISIBLE);
					_intialiseCardReader.setVisibility(View.VISIBLE);
					_screenStatus = swipeStatus.cardnotready;
					// _intialiseCardReader.setEnabled(true);


				}

				_swipeMessageTv.setVisibility(View.VISIBLE);
				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						// _swipeMessageTv.setText("Swipe Card please...");
						_exit.setEnabled(true);
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}

			} else {
				_screenStatus = swipeStatus.badswipe;
			}
			
		} else {
			showReaderDialog();

			startSwipe();
		}
		
	
		
	}


	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (unimagReader!=null) {
			unimagReader.release();

		}
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		if(unimagReader!=null)
		{
			//you should stop swipe card and unregister when the application go to background
			unimagReader.stopSwipeCard();			
			unimagReader.unregisterListen();
		}

		IGUtility.dismissProgressDialog(_readerDialog);
		super.onPause();
	}

	/**
	 * on click initilaise card button
	 * 
	 * @param v
	 */
	public void onClickInitialiseCardButton(View v) {
		_intialiseCardReader.setEnabled(false);
		onSwipeInit();
	}

	private void showOKAlertWithMessage(String title, String message) {
		Dialog dlg = new AlertDialog.Builder(this)
				.setTitle(title)

				.setMessage(message)
				.setPositiveButton(IGConstants.OKMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								onSwipeInit();

							}
						}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	/**
	 * exit button tapped
	 * 
	 * @param v
	 */
	public void onExitButtonClick(View v) {
		if (_isSwipeInactive) {
			Log.d("SWIPE PRACTICE", "EXIT");
			Exception newExp = new Exception("TEST SWIPE COMPLETE: "
					+ "Success count = " + _successCount + " ");
			IGUtility.logExceptionInQLogger(newExp);
			goToJobsActivity();
		}
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog); // completed offline
															// web service.
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			_exit.setEnabled(true);

			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			goToJobsActivity();
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		super.onFailedToGetResponse(errorResponse, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			// Enable the payoffline button which is disabled earlier.
			_exit.setEnabled(true);
		}
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * SwipeCardActivity activity and jobs activity.
	 */
	private void goToJobsActivity() {
		_screenStatus = swipeStatus.defaultStatus;
		_successCount = 0;

		Intent intent = new Intent(IGSwipePracticeActivity.this,
				IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IGJobsActivity.checkDriverStatus = true;

		startActivity(intent);

		finish();
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);

	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);

	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);

	}

		
	private void resetUnimagReader() {
		if (unimagReader!=null) {
			unimagReader.stopSwipeCard();			
			unimagReader.unregisterListen();
			unimagReader.release();
			unimagReader = null;
		}
	}
	
	private void startSwipe() {
		if(unimagReader!=null)
		{	if(unimagReader.startSwipeCard()) {
			
        		Log.e("UNIMAG","UNIMAG "+ "to startSwipeCard called");
			}
      		  else {
      			  IGUtility.dismissProgressDialog(_readerDialog);
    			  Log.e("UNIMAG ","UNIMAG "+"to startSwipeCard call failed");
//    			  _unimagMessage = (String) this
//    				.getText(R.string.card_reader_not_ready);
    			  _unimagMessage = "CARD READER NOT READY...";
    			  _unimagHandler.post(doUpdateStatus);

      		  }
		} else {
			_cardInfo = null;
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {
					showReaderDialog();


					// _swipeMessageTv.setText("");
					_initUnimagInProgress = true;
					
					if(unimagReader==null)
						unimagReader =  new UniMagReader(IGSwipePracticeActivity.this,IGSwipePracticeActivity.this);
			        unimagReader.registerListen();
			        unimagReader.setTimeoutOfSwipeCard(10000);
			        String fileNameWithPath = IGUtility.getXMLFileFromRaw();
			        if(!IGUtility.isFileExist(fileNameWithPath)) { fileNameWithPath = null; } 
			        unimagReader.setXMLFileNameWithPath(fileNameWithPath);
			        unimagReader.loadingConfigurationXMLFile(true);
			        Log.e("UNIMAG", "UNIMAG" + "SDK--> "+ unimagReader.getSDKVersionInfo());

				} else {
					_swipeMessageTv.setText("CARD READER NOT READY...");
				}

				_swipeMessageTv.setVisibility(View.VISIBLE);
				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						// _swipeMessageTv.setText("Swipe Card please...");
						_exit.setEnabled(true);
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}

			} else {
				_screenStatus = swipeStatus.badswipe;
			}
		}
		
	}

	@Override
	public boolean getUserGrant(int type, String message) {
		Log.e("UNIMAG", "UNIMAG getUserGrant  " + " type = " + type + " message = " + message);

		return true;
	}

	@Override
	public void onReceiveMsgAutoConfigProgress(int arg0) {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgAutoConfigProgress" + 
				"flagOfCardData = " +arg0);
		
	}

	@Override
	public void onReceiveMsgCardData(byte flagOfCardData, byte[] cardData) {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgCardData" + 
				"flagOfCardData = " +flagOfCardData +
				"cardData = " + cardData);

		byte flag = (byte) (flagOfCardData&0x04);
		_cardDataString = null;
		if(flag==0x00) {
			Log.e("UNIMAG", "Demo Info "+"no need of decryption");

			_cardDataString = new String (cardData);
		}
		if(flag==0x04)
		{
			//You need to dencrypt the data here first.
			Log.e("UNIMAG", "Demo Info "+"need decryption");

			_cardDataString = new String (cardData);
		}
			
		_themeButton.setClickable(true);

		if (_isBadSwipe)
			_successCount = 0;

		_successCount++;
		_isBadSwipe = false;

		//resetUnimagReader(); // sus : remove it
		_unimagHandler.post(onSwipeSuccess);
		
		
	}

	@Override
	public void onReceiveMsgCommandResult(int commandID, byte[] cmdReturn) {
		Log.e("UNIMAG" , 
				"UNIMAG onReceiveMsgCommandResult " + " commandID= " + commandID 
				+ " cmdReturn=" + cmdReturn 
				+ " cmdHexString = " + IGUtility.getHexStringFromBytes(cmdReturn));
		
	}

	@Override
	public void onReceiveMsgConnected() {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgConnected");
		//startSwipe();
		_unimagHandler.post(startSwipe);

		
	}

	@Override
	public void onReceiveMsgDisconnected() {

		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgDisconnected");
		_isUnimagReaderConnected = false;

		_initUnimagInProgress = false;

		_unimagHandler.post(onDisconnected);

		
	}

	@Override
	public void onReceiveMsgFailureInfo(int arg0, String message) {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgFailureInfo" + 
				"index = " +arg0 +
				"message = " + message);
		
	}

	@Override
	public void onReceiveMsgSDCardDFailed(String message) {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgSDCardDFailed" + 
				"message = " + message);
		
	}
	
	private void showReaderNotDetectedDialog() {
		Dialog dlg = new AlertDialog.Builder(IGSwipePracticeActivity.this)
		.setTitle("")
		.setMessage(
				"Unable to detect ingogo certified card reader. Please attach the ingogo card reader and try again.")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				resetUnimagReader();
				_initUnimagInProgress = false;
				_unimagMessage = "CARD READER NOT READY...";
				_unimagHandler.post(onDisconnected);
			}
		}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.setCancelable(false);
		dlg.show();
	}

	@Override
	public void onReceiveMsgTimeout(String message) {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgTimeout" + 
				"message = " + message);
		if (!_isUnimagReaderConnected) {
			Log.e("UNIMAG" , 
					"UNIMAG " + "onReceiveMsgTimeout" + 
					"unimag reader is not connected");
			_unimagHandler.post(showReaderNotDetectedDialog);

		} else {
			Log.e("UNIMAG" , 
					"UNIMAG " + "onReceiveMsgTimeout" + 
					"unimag reader is connected");
			_unimagHandler.post(startSwipe);
		}
	
		
	}

	@Override
	public void onReceiveMsgToConnect() {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgToConnect");
		
	}

	@Override
	public void onReceiveMsgToSwipeCard() {
		Log.e("UNIMAG" , 
				"UNIMAG " + "onReceiveMsgToSwipeCard");
		_isUnimagReaderConnected = true;
		IngogoApp.setPrimaryCardReaderAttached(true);
		_unimagHandler.post(onReadyToSwipe);
	}
	
	private Runnable onReadyToSwipe = new Runnable()
	{
		public void run()
		{
			IGUtility.dismissProgressDialog(_readerDialog);
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {
					if (_successCount == 0)
						_swipeMessageTv.setText("Swipe card please... ");

					_intialiseCardReader.setVisibility(View.GONE);
					_intialiseCardReader.setEnabled(false);
					_screenStatus = swipeStatus.pleaseSwipe;
					_exit.setEnabled(true);
				}
			}
		}
	};
	
	
	private Runnable onDisconnected = new Runnable()
	{
		public void run()
		{
			IGUtility.dismissProgressDialog(_readerDialog);
			_themeButton.setClickable(true);

			Dialog dlg = new AlertDialog.Builder(IGSwipePracticeActivity.this)
					.setTitle("")
					.setMessage(
							"Attach card reader and then select the Initialise Card Reader")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							_exit.setEnabled(true);
							_intialiseCardReader.setEnabled(true);
						}
					}).create();
			dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			dlg.setCancelable(false);
			dlg.show();

			Log.i("CARDREADER", "CARDREADER- onReaderDisconnected");
			_screenStatus = swipeStatus.defaultStatus;
			_successCount = 0;
			_isBadSwipe = false;

			initSwipePractice();

			_swipeMessageTv.setText("CARD READER NOT READY...");
			// _payOffline.setVisibility(View.VISIBLE);
			_intialiseCardReader.setVisibility(View.VISIBLE);
			_screenStatus = swipeStatus.cardnotready;
			// _intialiseCardReader.setEnabled(true);
			resetUnimagReader();
		}
	};
	
	private Runnable startSwipe = new Runnable()
	{
		public void run()
		{
			startSwipe();
		}
	}; 
	
	private Runnable onSwipeSuccess = new Runnable()
	{
		public void run()
		{
			
			_isSwipeInactive = true;

			initSwipePractice();

			if (_successCount == 5) {
				_swipeMessageTv
						.setText("CONGRATULATIONS! \n YOU HAVE PASSED THE SWIPE TEST");
			} else {
				if (_successCount == 6) {
					_successCount = 1;
				}
				_swipeMessageTv.setText("SUCCESSFUL READS: " + _successCount
						+ " IN A ROW");
			}

			try {
				_cardInfo = new CardInfo(_cardDataString);
			} catch (CardInfoParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onSwipeInit();
		}
	};
	
	private Runnable doUpdateStatus = new Runnable()
	{
		public void run()
		{
			try
			{
				_swipeMessageTv.setText(_unimagMessage);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	};
	private Runnable showReaderNotDetectedDialog = new Runnable()
	{
		public void run()
		{
			showReaderNotDetectedDialog();
		}
	}; 

}
