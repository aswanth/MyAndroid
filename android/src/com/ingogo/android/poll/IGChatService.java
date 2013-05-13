/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Delivers incoming chats, save unread chats.
 */

package com.ingogo.android.poll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGUtility;

public class IGChatService implements IGIncomingMessageListener {

	private static IGChatService _sharedService;

	private int _jobID;
	private IGIncomingMessagePollingTask _messagePollingTask;
	private ArrayList<String> _unreadMessages;
	private IGChatServiceListener _listener;
	private ArrayList<String> _chatRecord;

	public void setListener(IGChatServiceListener listener) {
		this._listener = listener;
	}

	public IGChatService(int jobId) {
		_jobID = jobId;
		readChatFromFile();
	}

	/**
	 * Start message polling for the job Id.
	 */
	public void start() {
		_messagePollingTask = new IGIncomingMessagePollingTask(this,
				this._jobID);
		_messagePollingTask
				.startPolling(IGConstants.kIncomingMessagePollingInterval);
	}

	/**
	 * Stop message polling.
	 */
	public void stop() {
		if (_messagePollingTask != null) {
			_messagePollingTask.stopPolling();
			_messagePollingTask = null;
		}
	}

	/**
	 * Clear all saved unread messages.
	 * 
	 * @param callListener
	 *            - Flag to call listener for any unread messages.
	 */
	public void clearAllMessages(Boolean callListener) {
		if (_unreadMessages != null) {
			if (_unreadMessages.size() > 0) {
				if (_listener != null && callListener) {
					// Call the listener with unread messages
					if (_unreadMessages.size() > 0) {
						for (int i = 0; i < _unreadMessages.size(); i++) {
							addChatToRecord(_unreadMessages.get(i), "I:");
						}
					}
					_listener.chatsRecieved(_chatRecord);
				}
				_unreadMessages.clear();
			}else {
				if (_listener != null && callListener) {
					if (_chatRecord != null && _chatRecord.size()>0)
					_listener.chatsRecieved(_chatRecord);
				}
			}
		
		}else {
			if (_listener != null && callListener) {
				if (_chatRecord != null && _chatRecord.size()>0)
				_listener.chatsRecieved(_chatRecord);
			}
		}

	}

	public int getJobID() {
		return _jobID;
	}

	public Activity getCallingActivity() {
		return (Activity) _listener;
	}

	@Override
	public void incomingMessageReceived(String incomingMessage, boolean playNewChatAlert) {

		if (incomingMessage.length() == 0)
			return;

		if (_unreadMessages == null) {
			_unreadMessages = new ArrayList<String>();
		}
		// Save incoming messages
		_unreadMessages.add(incomingMessage);

		if (playNewChatAlert) {
			IGUtility.playSoundNotification(getCallingActivity(), R.raw.new_chat);
		}
		if (_listener != null) {
			
			
			// Send the received message to listener
			_listener.incomingMessageReceived(incomingMessage, playNewChatAlert);
		}
	}

	@Override
	public void incomingMessageError(String errorMessage) {
		if (_listener != null) {
			// Call the listener with error
			_listener.incomingMessageError(errorMessage);
		}
	}

	@Override
	public void bookingStatusReceived(String bookingStatus) {
		if (_listener != null) {
			_listener.bookingStatusReceived(bookingStatus);
		}
	}
	
	/**
	 * Useful to get the unread message count
	 * 
	 * @return number of unread messages
	 */
	public int getNumberOfUnreadMessages() {
		if (_unreadMessages == null) {
			_unreadMessages = new ArrayList<String>();
		}
		return _unreadMessages.size();
	}

	/**
	 * Singleton instance to make sure that only one message polling instance is
	 * running.
	 * 
	 * @param jobID
	 * @return
	 */
	public static IGChatService getInstance(int jobID) {
		if (_sharedService == null) {
			_sharedService = new IGChatService(jobID);
		} else {
			if (_sharedService.getJobID() != jobID) {
				_sharedService.stop();
				_sharedService.saveChatRecord();
				_sharedService = new IGChatService(jobID);
			}
		}
		return _sharedService;
	}

	public void addChatToRecord(String message, String flag) {
		if (message == null)
			return;
		if (_chatRecord == null) {
			_chatRecord = new ArrayList<String>();
			
		}
		_chatRecord.add(flag + message);
		if(_chatRecord.size() > IGConstants.kChatHistoryLimit)
		{
		_chatRecord.remove(0);
		}
		
		
	}
	
	public void replaceOutGoingChat(String message, String flag, String chatFailureFlag, int index) {
		if (message == null)
			return;
		if (_chatRecord == null) {
			_chatRecord = new ArrayList<String>();
			
		}
		
		_chatRecord.remove(index);
		_chatRecord.add(index, flag + message + chatFailureFlag);
		if(_chatRecord.size() > IGConstants.kChatHistoryLimit)
		{
		_chatRecord.remove(0);
		}
	} 

	public ArrayList<String> getChatRecord() {

		return _chatRecord;
	}

	private void saveChatRecord() {
		saveChatToFile();
	}

	private void addUnreadMessagesToChatRecord() {
		if (_unreadMessages == null)
			return;
		int count = _unreadMessages.size();
		for (int i = 0; i < count; i++) {

			String data = "U:" + _unreadMessages.get(i);
			if (_chatRecord == null) {
				_chatRecord = new ArrayList<String>();
			}

			addChatToRecord(_unreadMessages.get(i), "U:");

			_chatRecord.add(data);
		}

	}
	
	private String getChatRecordSerialized(){
		int count=_chatRecord.size();
		  if (count==0)
		    return null;
		  StringBuilder out=new StringBuilder();
		  out.append(_chatRecord.get(0));
		  for (int i=1;i<count;++i)
		    out.append("\n").append(_chatRecord.get(i));
		  return out.toString();
	}
	
	private boolean saveChatToFile() {
		if(_chatRecord==null)
			return false;
		try {
			
			String fileName = _jobID + ".txt";
			FileOutputStream fOut = IngogoApp.getSharedApplication().openFileOutput(fileName, Activity.MODE_WORLD_READABLE);;
			Log.e("PackageName",IngogoApp.getSharedApplication().getPackageName());
			addUnreadMessagesToChatRecord();
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			osw.write(getChatRecordSerialized());
			osw.flush();
			osw.close();
		} catch (IOException ioe) {
			Log.e("serializeObject", "error", ioe);
			return false;
		}
		return true;
	}

	private boolean readChatFromFile() {
		try {

			
			FileInputStream fIn = IngogoApp.getSharedApplication().openFileInput("" + _jobID + ".txt");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader br = new BufferedReader(isr);
            
            
            
			String data = null;
			do {
				data = br.readLine();
				if (data != null && data.length() > 0) {
					//Log.e("DATA", data);
					if (data.startsWith("U:")) {
						if (_unreadMessages == null) {
							_unreadMessages = new ArrayList<String>();
						}
						_unreadMessages.add(data.substring(2));
						continue;
					}
					if (_chatRecord == null) {
						_chatRecord = new ArrayList<String>();
					} 
					//Log.e("Chat Size",""+_chatRecord.size());
					_chatRecord.add(data);
					if(_chatRecord.size() > IGConstants.kChatHistoryLimit)
					{
					_chatRecord.remove(0);
					}
					
				}
			} while (data != null);

		} catch (FileNotFoundException e) {
			Log.w("Reading file", "Unable to open file");
			return false;
		} catch (IOException e) {
			Log.w("Reading file", "Error reading file");
			return false;
		}
		return true;
	}

	public static void delete_chatHistory(int jobId) {
		String filePath = "/data/data/"+IngogoApp.getSharedApplication().getPackageName()+"/files/"+jobId+".txt";
		File file = new File(filePath);
		if(file.delete()){
			Log.i("IG_CHAT_SERVICE", "Chat Record for Job Id "+jobId+" is deleted successfully.");
		} else {
			Log.i("IG_CHAT_SERVICE", "Error in deletion.");
		}

	}



}
