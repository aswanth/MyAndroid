package com.ingogo.android.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ingogo.android.R;
import com.ingogo.android.adapters.IGSuburbListAdapter;
import com.ingogo.android.model.IGSuburbModel;

public class IGSuburbListDialog extends Dialog {
	private ListView _suburbListView;
	private EditText _suburbFilteringEditText;
	private IGSuburbListAdapter _suburbListAdapter;
	private Button _cancelSmallButton;
	private ArrayList<String> _suburbList;
	private ArrayList<String> _newSuburbList;
	private String _localityName;
	private boolean _isKeyBoardShownAtStartUp;

	public ArrayList<String> getNewSuburbList() {
		return _newSuburbList;
	}

	public void setNewSuburbList(ArrayList<String> _newSuburbList) {
		this._newSuburbList = _newSuburbList;
	}

	private Context _context;

	public IGSuburbListDialog(Context context) {
		super(context);
		_context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suburb_list_dialog);
		initViews();
		setUpViews();
		
	}
	
	public IGSuburbListDialog(Context context, boolean isKeyBoardShownAtStartUp) {
		super(context);
		_context = context;
		_isKeyBoardShownAtStartUp = isKeyBoardShownAtStartUp;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suburb_list_dialog);
		initViews();
		setUpViews();
		
	}
	
	public void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager)_context. getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				_suburbFilteringEditText.getWindowToken(), 0);
//		imm.hideSoftInputFromWindow(_suburbFilteringEditText,
//				0);
	}
	
	private void showKeyboard() {
		_suburbFilteringEditText.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (_isKeyBoardShownAtStartUp) {
					InputMethodManager imm = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(_suburbFilteringEditText,
							InputMethodManager.SHOW_FORCED);
				}
		
			}
		}, 500);

		_suburbFilteringEditText.setSelection(_suburbFilteringEditText.getText().length());
	}

	private void setUpViews() {
		_suburbFilteringEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (s.length() > 0) {
					_cancelSmallButton.setVisibility(View.VISIBLE);
				} else {
					_cancelSmallButton.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (_suburbList == null || _suburbList.size() == 0) {
					return;
				}
				if (_newSuburbList != null && !_newSuburbList.isEmpty())
					_newSuburbList.clear();
				if (s.length() >= 3) {
					for (String data : _suburbList) {
						if (!(data.toUpperCase().indexOf(
								(s.toString().toUpperCase())) == -1)) {
							_newSuburbList.add(new String(data));
						}

					}

				} else {
					for (String data : _suburbList) {
						_newSuburbList.add(new String(data));
					}
				}

				if (_suburbListAdapter != null)
					_suburbListAdapter.notifyDataSetChanged();

			}

		});
		_cancelSmallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_suburbFilteringEditText.setText("");
				_cancelSmallButton.setVisibility(View.GONE);
			}
		});

	}

	public void setListAdapter(ArrayList<IGSuburbModel> suburbs) {
//		String localityName = IngogoApp.getSharedApplication()
//				.getLocalityName();
//		ArrayList<IGSuburbModel> suburbs = null;
//		suburbs = IGSuburbParser.getSerializedSuburbs(
//				localityName.toLowerCase()).get(localityName.toUpperCase());
		if (suburbs == null || suburbs.size() == 0) {
			return;
		}

		IGSuburbModel igSuburbModel[] = new IGSuburbModel[suburbs.size()];
		if (_suburbList != null && !_suburbList.isEmpty())
			_suburbList.clear();

		for (int i = 0; i < suburbs.size(); i++) {

			igSuburbModel[i] = suburbs.get(i);
		}

		Arrays.sort(igSuburbModel, new SuburbListComparator());

		for (int j = 0; j < suburbs.size(); j++) {
			_suburbList.add(igSuburbModel[j].getSuburbName().replace(";", " "));
		}
		if (_newSuburbList != null && !_newSuburbList.isEmpty())
			_newSuburbList.clear();

		_newSuburbList.addAll(_suburbList);
		_suburbListAdapter = new IGSuburbListAdapter(_context, _newSuburbList);
		_suburbListView.setAdapter(_suburbListAdapter);
		showKeyboard();

	}
	
	public void setListAdapterFromSuburbNameArray(ArrayList<String> suburbs) {
		if (suburbs == null || suburbs.size() == 0) {
			return;
		}
		if (_suburbList != null && !_suburbList.isEmpty())
			_suburbList.clear();

		for (int j = 0; j < suburbs.size(); j++) {
			_suburbList.add(suburbs.get(j).replace(";", " "));
		}
		if (_newSuburbList != null && !_newSuburbList.isEmpty())
			_newSuburbList.clear();

		_newSuburbList.addAll(_suburbList);
		_suburbListAdapter = new IGSuburbListAdapter(_context, _newSuburbList);
		_suburbListView.setAdapter(_suburbListAdapter);
		showKeyboard();


	}

	private void initViews() {
		_suburbListView = (ListView) findViewById(R.id.suburbList);
		_suburbFilteringEditText = (EditText) findViewById(R.id.filter_suburb_edittext);
		_cancelSmallButton = (Button) findViewById(R.id.cancel_small_button);
		_suburbList = new ArrayList<String>();
		_newSuburbList = new ArrayList<String>();

	}

	public ListView getSuburbListView() {
		return _suburbListView;
	}

	public void setSuburbListView(ListView _suburbListView) {
		this._suburbListView = _suburbListView;
	}

	private class SuburbListComparator implements Comparator<IGSuburbModel> {

		@Override
		public int compare(IGSuburbModel firstModel, IGSuburbModel secondModel) {
			String firstModelSuburbName = firstModel.getSuburbName();
			String secondModelSuburbName = secondModel.getSuburbName();
			return firstModelSuburbName
					.compareToIgnoreCase(secondModelSuburbName);
		}
	}
	
	

	

}
