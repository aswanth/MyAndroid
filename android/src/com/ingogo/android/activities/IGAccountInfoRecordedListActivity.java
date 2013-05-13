package com.ingogo.android.activities;

import java.util.ArrayList;
import com.ingogo.android.R;
import com.ingogo.android.adapters.IGLoadAndGoAccountsAdapter;
import com.ingogo.android.model.IGLoadAndGoAccountDetailsModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGViewLoadAndGoAccountsApi;
import com.ingogo.android.webservices.interfaces.IGViewLoadAndGoAccountsListener;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class IGAccountInfoRecordedListActivity extends IGBaseActivity implements
		IGViewLoadAndGoAccountsListener {

	private ListView _accountDetailsList;
	//private TextView _noteText;
	private IGLoadAndGoAccountsAdapter _adapter;
	private ProgressDialog _progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accountinfo_recorded_list);
		invokeLoadAndGoAccountApi();
		initViews();
	}

	private void invokeLoadAndGoAccountApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGViewLoadAndGoAccountsApi api = new IGViewLoadAndGoAccountsApi(
					this, this);
			api.viewLoadAndGoAccounts();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	private void initViews() {
		_accountDetailsList = (ListView) findViewById(R.id.accountDetailsList);
		//_noteText = (TextView) findViewById(R.id.noteText);

	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.ignoreStaleState = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.ignoreStaleState = false;
		IGUtility.dismissProgressDialog(_progressDialog);
	}

	@Override
	public void successToFetchLoadAndGoAccounts(
			ArrayList<IGLoadAndGoAccountDetailsModel> accounts,
			String balancesAreAsAt) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (accounts != null && accounts.size() > 0) {
			 _adapter = new IGLoadAndGoAccountsAdapter(this, accounts);
			_accountDetailsList.setAdapter(_adapter);
		}
		
//		if( balancesAreAsAt != null ) {
//			String formatDate = IGUtility.getDateStringFromFormat(balancesAreAsAt,"dd/MM hh:mma");
//			String noteText = getString( R.string.note_text_message);
//			_noteText.setText(noteText.replace("time", formatDate));
//			
//		}

	}

	@Override
	public void failedToFetchLoadAndGoAccounts(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);

	}

}
