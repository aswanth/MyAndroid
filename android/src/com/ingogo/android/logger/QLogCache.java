package com.ingogo.android.logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGUtility;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

public class QLogCache {

	private static QLogCache _qlogCahe;
	private static final String KCACHED_LOG_FOLDER_NAME = "CacheLog";
	private static final String FILE_NAME_INITIAL = KCACHED_LOG_FOLDER_NAME
			+ "/";

	public static synchronized QLogCache getSharedInstance() {
		if (_qlogCahe == null) {
			_qlogCahe = new QLogCache();
		}
		return _qlogCahe;
	}

	public void cacheLog(final String message) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				File folder = getFileWithName(KCACHED_LOG_FOLDER_NAME);

				if (!folder.exists()) {

					folder.mkdir();

				}

				String fileName = IGUtility.getDateStringFromFormat(
						String.valueOf(System.currentTimeMillis()),
						"dd MM yyyy") + ".txt";

				File file = getFileWithName(FILE_NAME_INITIAL + fileName);

				if (!file.exists()) {
					deleteOlderFile(folder);
					Log.e("FILE", "Created a new File");
					// If file not exists, Create new file
					try {

						file.createNewFile();

					} catch (IOException e) {

						e.printStackTrace();
					}

				}

				writeToFile(file, message);

			}
		}).start();
	}

	private void deleteOlderFile(File folder) {

		File allFiles[] = folder.listFiles();

		if (allFiles != null && allFiles.length >= 2) {

			Arrays.sort(allFiles, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {

					if (((File) o1).lastModified() < ((File) o2).lastModified()) {
						return -1;
					} else if (((File) o1).lastModified() > ((File) o2)
							.lastModified()) {
						return +1;
					} else {
						return 0;
					}
				}

			});

			File olderFile = getFileWithName(FILE_NAME_INITIAL
					+ allFiles[0].getName());

			if (olderFile.exists()) {

				boolean isOldFileDelete = olderFile.delete();

				if (isOldFileDelete) {

					Log.i("OLD FILE", "DELETED");

				} else {

					Log.i("NEW FILE", "NOT DELETED");

				}
			}

		}

	}

	private void writeToFile(File file, String message) {

		try {

			// BufferedWriter for performance, true to set append to
			Log.e("FILE", "Writing");

			BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
			// _cacheString += message;

			buf.append(message);

			buf.newLine();

			buf.flush();

			buf.close();

			Log.i("LOG CACHING ", "FILENAME:" + file.getName() + " DATA:"
					+ message);

		} catch (IOException e) {

			e.printStackTrace();

		} catch (OutOfMemoryError e) {

			e.printStackTrace();

		}

	}

	public File getFileWithName(String fileName) {

		ContextWrapper contextWrapper = new ContextWrapper(IngogoApp
				.getSharedApplication().getApplicationContext());

		File directory = contextWrapper.getDir("MyFileStorage",
				Context.MODE_PRIVATE);

		File file = new File(directory, fileName);

		return file;
	}

	public File fetchCachedResponse(String mobileNumber) {

		if (mobileNumber == null) {

			mobileNumber = IngogoApp.getSharedApplication().getUserId();
		}
		ArrayList<String> fileList = getAlllFilesInFolder();

		if (fileList.size() > 0) {

			try {

				zip(fileList, mobileNumber + ".zip");

			} catch (IOException e) {

				e.printStackTrace();

				return null;
			}
			File zippedFile = getFileWithName(mobileNumber + ".zip");

			if (zippedFile.exists()) {

				return zippedFile;
			}
			return null;
		}
		return null;
	}

	public ArrayList<String> getAlllFilesInFolder() {

		ArrayList<String> fileList = new ArrayList<String>();

		File folder = getFileWithName(KCACHED_LOG_FOLDER_NAME);

		if (folder.exists()) {

			String files[] = folder.list();

			if (files != null && files.length > 0) {

				for (String fileName : files) {

					fileList.add(FILE_NAME_INITIAL + fileName);

				}
			}
		}
		return fileList;
	}

	private void removeCacheLog(String fileName) {

		File file = getFileWithName(fileName);

		if (file.exists()) {

			boolean status = file.delete();

			if (status) {

				Log.e("FILE", "Deleted " + fileName);
				Log.e("FILE REMOVAL", "REMOVED");
			} else {
				Log.e("FILE REMOVAL", "NOT REMOVED");
			}
		}
	}

	// private void refreshCacheLog(String fileName) {
	//
	// // Get the text file
	// File file = getFileWithName(fileName);
	//
	// // Read text from file
	//
	// StringBuilder builder = new StringBuilder();
	// try {
	// BufferedReader br = new BufferedReader(new FileReader(file));
	// String line;
	//
	// while ((line = br.readLine()) != null) {
	// /*
	// * if (line.length() > SINGLE_LINE_LENGTH &&
	// * !(line.contains("~_"))) {
	// *
	// * printLargeReceiptItem(line, bluetoothHelper); continue; }
	// */
	// builder.append(line + "\n");
	// }
	// } catch (IOException e) {
	// // You'll need to add proper error handling here
	// }
	// _cacheString = builder.toString();
	//
	// }

	public void removeAllCachedLog(String mobileNumber) {
		// Don't need to remove the log files.
		// long currentTime = System.currentTimeMillis();
		// for (long i = currentTime; i > (currentTime - TWO_DAYS); i -=
		// ONE_DAY) {
		// String fileName = IGUtility.getDateStringFromFormat(
		// String.valueOf(i), "dd MM yyyy")
		// + ".txt";
		// removeCacheLog(fileName);
		// }
		// Remove zip file
		removeCacheLog(mobileNumber + ".zip");
	}

	public void zip(ArrayList<String> files, String zipFileName)

	throws IOException {
		File file = getFileWithName(zipFileName);
		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(file);
		ZipOutputStream out = new ZipOutputStream(
				new BufferedOutputStream(dest));
		try {
			byte data[] = new byte[10];
			for (int i = 0; i < files.size(); i++) {
				File test = getFileWithName(files.get(i));
				FileInputStream fi = new FileInputStream(test);
				origin = new BufferedInputStream(fi, 10);
				try {
					ZipEntry entry = new ZipEntry(files.get(i).substring(
							files.get(i).lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, 10)) != -1) {
						out.write(data, 0, count);
					}
				} finally {
					origin.close();
				}
			}

		} finally {
			out.close();
		}
	}

}
