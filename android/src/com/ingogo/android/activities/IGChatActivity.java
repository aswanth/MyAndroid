/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays chat Screen, where driver communicate to passenger.
 */

package com.ingogo.android.activities;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ingogo.android.R;
import com.ingogo.android.adapters.IGChatMessageAdapter;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.model.IGMessageModel;
import com.ingogo.android.poll.IGChatService;
import com.ingogo.android.poll.IGChatServiceListener;
import com.ingogo.android.webservices.IGSendMessageApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGSendMessageApiListener;

public class IGChatActivity extends IGBaseActivity implements
		IGChatServiceListener, IGExceptionApiListener, IGSendMessageApiListener {

	private EditText _chatText;
	private IGJob _job;
	private String _currentChatText = null;
	private ListView _chatList = null;
	private IGChatMessageAdapter _chatAdapter = null;
	private ArrayList<String> _currentMessages = null;
	private String allowableString = ",./-?";
	private Button _sendButton;
	private int _chatIndex = 0;
	private ArrayList<IGMessageModel> _mapMessages = new ArrayList<IGMessageModel>();
	private MediaPlayer mMediaPlayer;
	
	private static boolean _isModeChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.chat);

		Intent intent = getIntent();
		_job = (IGJob) intent.getSerializableExtra(IGConstants.kJob);

		initViews();
		setupMessaging();

		if (intent.hasExtra("content") && !_isModeChanged) {
			_mapMessages = (ArrayList<IGMessageModel>) getIntent().getSerializableExtra("content");

			for (int i = 0; i < _mapMessages.size(); i++) {
				// Save the incoming chat from passenger.
				IGChatService
						.getInstance(Integer.parseInt(_job.getId()))
						.addChatToRecord(_mapMessages.get(i).getContent(), "I:");
				IGChatService.getInstance(Integer.parseInt(_job.getId()))
						.clearAllMessages(false);
			}

			new Thread(new Runnable() {
				public void run() {
					
					mMediaPlayer = MediaPlayer.create(IGChatActivity.this, R.raw.new_chat);
					if (mMediaPlayer.isPlaying())
						mMediaPlayer.stop();
					mMediaPlayer.start();
				}
			}).start();

		}

		_isModeChanged = false;
		// Add the chat service listener and clear all messages.
		IGChatService.getInstance(Integer.parseInt(_job.getId())).setListener(
				this);
		IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.clearAllMessages(true);
	}

	/**
	 * Function to set up the chat environment. Initialises the chat set up.
	 */
	private void setupMessaging() {
		_currentMessages = new ArrayList<String>();
		_chatAdapter = new IGChatMessageAdapter(this);
		_chatAdapter.setMessages(_currentMessages);
		_chatList.setAdapter(_chatAdapter);
	}
	
	public void onClickBackButton(View v) {
		v.setEnabled(false);
		v.setClickable(false);
		finish();
	}

	@Override
	protected void onPause() {

		// Stop the chat service listener.
		IGChatService.getInstance(Integer.parseInt(_job.getId())).stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		// Start the chat service listener.
		IGChatService.getInstance(Integer.parseInt(_job.getId())).setListener(
				this);
		IGChatService.getInstance(Integer.parseInt(_job.getId())).start();

		// If there are unread messages then clear previous messages.
		if (IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.getNumberOfUnreadMessages() > 0) {
			IGChatService.getInstance(Integer.parseInt(_job.getId()))
					.setListener(this);
			IGChatService.getInstance(Integer.parseInt(_job.getId()))
					.clearAllMessages(true);
		}

		super.onResume();
	}

	/**
	 * Initialises the view.
	 * 
	 */
	private void initViews() {

		_chatText = (EditText) findViewById(R.id.chat_textinput);
		_chatList = (ListView) findViewById(R.id.chat_messages_list);
		_sendButton = (Button) findViewById(R.id.sendbutton);

		// If the job status is collected then the driver is not allowed
		// to enter any new messages. So the chat text and send buttons
		// are disabled.
		if (_job.getStatus() == IGConstants.kJobCollected) {
			_chatText.setEnabled(false);
			_sendButton.setEnabled(false);
			_sendButton.setBackgroundResource(R.drawable.send_btn_disabled);
		} else {
			_sendButton.setEnabled(true);
			_chatText.setEnabled(true);
			_sendButton.setBackgroundResource(R.drawable.send_button_selector);
		}

		// Defines an input filter for entering the chat messages.
		// Only letters, digits, space character
		// and the characters described in the allowableString are
		// allowed to enter.
		// The max character limit is set as 100.
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {

					if (!Character.isLetterOrDigit(source.charAt(i))
							&& !Character.isSpace(source.charAt(i))
							&& !allowableString.contains(source)) {
						return "";
					}
				}
				return null;
			}
		};
		InputFilter[] FilterArray = new InputFilter[2];
		FilterArray[0] = new InputFilter.LengthFilter(100);
		FilterArray[1] = filter;
		_chatText.setFilters(FilterArray);

	}

	/*
	 * Function to send the chat message on send button click.
	 */

	public void onSendButtonClick(View view) {
		Log.e("SendButton", "SendButton is Tapped");

		// To avoid sending blanl messages.
		if (_chatText.getText().toString().trim().length() == 0)
			return;

		// Dismiss the keyboard.
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(_chatText.getWindowToken(), 0);

		_currentChatText = _chatText.getText().toString();

		// Save the chat messages.
		IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.addChatToRecord(_currentChatText, "O:");

		// Send the message to server.

		_currentMessages.add("O:" + _currentChatText);
		if (_currentMessages.size() > IGConstants.kChatHistoryLimit) {
			_currentMessages.remove(0);
		}
		_chatIndex = _currentMessages.size() - 1;
		sendMessages();
		_chatAdapter.notifyDataSetChanged();
		_chatText.setText("");
	}

	/**
	 * Function to call the driverSendMessage api.
	 */
	private void sendMessages() {
		IGSendMessageApi sendApi = new IGSendMessageApi(this, this);
		sendApi.sendMessage(_job.getId(), _currentChatText, _chatIndex);
	}

	@Override
	public void incomingMessageReceived(String incomingMessage,
			boolean playMusic) {

		// Save the incoming chat from passenger.
		IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.addChatToRecord(incomingMessage, "I:");
		IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.clearAllMessages(false);

		if (_currentMessages.size() >= IGConstants.kChatHistoryLimit) {
			_currentMessages.remove(0);
		}
		Log.e("Test", "=" + _currentMessages.size());
		_currentMessages.add("I:" + incomingMessage);
		_chatAdapter.setMessages(_currentMessages);
		_chatAdapter.notifyDataSetChanged();
	}

	@Override
	public void incomingMessageError(String errorMessage) {
	}

	@Override
	public void chatsRecieved(ArrayList<String> chats) {

		int count = chats.size();

		// Add all the messages in the chat array to the _currentMessages array.
		for (int i = 0; i < count; i++) {

			_currentMessages.add(chats.get(i));

		}
		_chatAdapter.setMessages(_currentMessages);
		_chatAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		super.onResponseReceived(response, apiID);
	}

	@Override
	public void bookingStatusReceived(String bookingStatus) {
		Intent intent = new Intent();

		// If the passenger cancels the job in between chat then finish
		// the chat activity with the proper intent data.
		// If the passenger not confirms the job then
		// finish this activity with the proper intent data.
		if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerCancelledJob)) {
			intent.putExtra(IGApiConstants.kBookingStatusKey,
					IGApiConstants.kPassengerCancelledJob);
			setResult(RESULT_OK, intent);
			this.finish();
		} else if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerNotConfirmedJob)) {
			intent.putExtra(IGApiConstants.kBookingStatusKey,
					IGApiConstants.kPassengerNotConfirmedJob);
			setResult(RESULT_OK, intent);
			this.finish();
		} else if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerDispatched)) {
			intent.putExtra(IGApiConstants.kBookingStatusKey,
					IGApiConstants.kPassengerDispatched);
			setResult(RESULT_OK, intent);
			this.finish();
		}

	}

	@Override
	public void messageSent() {
		Log.i("MESSAGE SENT SUCCESSFULLY", "MESSAGE SENT SUCCESSFULLY");
	}

	@Override
	public void messageSentingFailed(String sentMessage, int chatIndex) {
		Log.i("MESSAGE SENT FAILED", "MESSAGE SENT FAILED");
		showChatNotReceivedNotification(sentMessage, chatIndex);

	}

	private void showChatNotReceivedNotification(String sentMessage,
			int chatIndex) {
		_currentMessages.set(chatIndex, "O:" + sentMessage
				+ IGConstants.kChatFailures);
		IGChatService.getInstance(Integer.parseInt(_job.getId()))
				.replaceOutGoingChat(sentMessage, "O:",
						IGConstants.kChatFailures, chatIndex);
		_chatAdapter.notifyDataSetChanged();
	}

	
	/**
	 * Button action to change the current theme If the current theme is day
	 * then set night as current theme and restart the activity. If the current
	 * theme is night then set day as current theme and restart the activity.
	 * 
	 * @param view
	 */
	@Override
	public void changeTheme(View view) {
		if (IngogoApp.getThemeID() == 1) {
			IngogoApp.setThemeID(2);
		} else {
			IngogoApp.setThemeID(1);
		}

		_isModeChanged = true;
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

}
