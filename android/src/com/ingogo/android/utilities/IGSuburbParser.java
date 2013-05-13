package com.ingogo.android.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGSuburbModel;
import com.ingogo.android.webservices.interfaces.IGSuburbsReadsFileListener;

public class IGSuburbParser {

	private ArrayList<String> _totalsuburbs = null;
	public static final String OUTPUT_FILE = "serialized_suburbs.igo";
	private HashMap<String, ArrayList<IGSuburbModel>> _serializedSuburbs;
	private IGSuburbsReadsFileListener _listener;

	public ArrayList<IGSuburbModel> getSuburbsForLocality(String locality) {
		ArrayList<IGSuburbModel> results = new ArrayList<IGSuburbModel>();
		if (_serializedSuburbs != null) {
			results = _serializedSuburbs.get(locality);
		} else {
			serializeSuburbs(locality);
			if (_serializedSuburbs != null) {
				results = _serializedSuburbs.get(locality);
			}
		}
		return results;
	}

	// public static HashMap<String, ArrayList<IGSuburbModel>>
	// getSerializedSuburbs(
	// String localityName) {
	// serializeSuburbs(localityName);
	// return _serializedSuburbs;
	// }
	public void getSerializedSuburbs(String localityName,
			IGSuburbsReadsFileListener listener) {
		_listener = listener;
		serializeSuburbs(localityName);
	}

	private static IGSuburbModel modelFromInfo(String[] suburbInfo) {
		IGSuburbModel model = new IGSuburbModel();
		model.setSuburbName(suburbInfo[2]);
		model.setPostcode(suburbInfo[4]);
		model.setLatitude(suburbInfo[5]);
		model.setLongitude(suburbInfo[6]);
		return model;
	}

	private void writeOutToFile(
			HashMap<String, ArrayList<IGSuburbModel>> suburbMap) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(OUTPUT_FILE);
			out = new ObjectOutputStream(fos);
			out.writeObject(suburbMap);
			out.close();
			System.out.println("Object Persisted");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void serializeSuburbs(String localityName) {
		new SuburbFileReadAsyncTask().execute(localityName);

		// ArrayList<String> totalSuburbs =readFile(localityName);
		// _serializedSuburbs = new HashMap<String, ArrayList<IGSuburbModel>>();
		// Log.i("Total suburb size",""+totalSuburbs.size());
		// for (int i = 1; i < totalSuburbs.size(); i++) {
		// String suburbLine = totalSuburbs.get(i);
		// String[] suburbInfo = suburbLine.split(",");
		// String locality = suburbInfo[1].toUpperCase();
		// ArrayList<IGSuburbModel> suburbList = null;
		//
		// if (!_serializedSuburbs.containsKey(locality)) {
		// suburbList = new ArrayList<IGSuburbModel>();
		// } else {
		// suburbList = _serializedSuburbs.get(locality);
		// }
		//
		// suburbList.add(modelFromInfo(suburbInfo));
		// _serializedSuburbs.put(locality, suburbList);
		// }
		//
		// // writeOutToFile(serializedSuburbs);
		// System.out.println(_serializedSuburbs.keySet().toString());
	}

	public ArrayList<String> readFile(String localityName) {
		Log.i("LOCALITY NAME", "" + localityName.toLowerCase());
		Context appContext = IngogoApp.getSharedApplication()
				.getApplicationContext();
		ArrayList<String> result = new ArrayList<String>();

		try {
			// Open the file that is the first
			// Get the object of DataInputStream
			int resourceId = 0;
			if (localityName.equalsIgnoreCase(IGConstants.kLocalityTvm)) {
				resourceId = R.raw.suburb_list_sydney;
			} else {
				resourceId = appContext.getResources().getIdentifier(
						IGConstants.KSUBURBLIST + localityName.toLowerCase(),
						"raw", "com.ingogo.android");
			}
			DataInputStream in = new DataInputStream(appContext.getResources()
					.openRawResource(resourceId));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				result.add(strLine);
				System.out.println(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		return result;
	}

	class SuburbFileReadAsyncTask extends
			AsyncTask<String, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			return readFile(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<String> totalSuburbs) {
			super.onPostExecute(totalSuburbs);
			_serializedSuburbs = new HashMap<String, ArrayList<IGSuburbModel>>();
			Log.i("Total suburb size", "" + totalSuburbs.size());
			for (int i = 1; i < totalSuburbs.size(); i++) {
				String suburbLine = totalSuburbs.get(i);
				String[] suburbInfo = suburbLine.split(",");
				String locality = suburbInfo[1].toUpperCase();
				ArrayList<IGSuburbModel> suburbList = null;

				if (!_serializedSuburbs.containsKey(locality)) {
					suburbList = new ArrayList<IGSuburbModel>();
				} else {
					suburbList = _serializedSuburbs.get(locality);
				}

				suburbList.add(modelFromInfo(suburbInfo));
				_serializedSuburbs.put(locality, suburbList);
			}
			if (_serializedSuburbs.size() == 0) {
				_listener.failedToReadSuburbs();
			} else {
				_listener.readSuburbsSuccessfully(_serializedSuburbs);
			}

			// writeOutToFile(serializedSuburbs);
			System.out.println(_serializedSuburbs.keySet().toString());

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}
}
