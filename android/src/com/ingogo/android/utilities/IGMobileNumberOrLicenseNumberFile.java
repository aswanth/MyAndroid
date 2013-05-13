package com.ingogo.android.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import com.ingogo.android.app.IngogoApp;

public class IGMobileNumberOrLicenseNumberFile {
    private static ArrayList<String> _arrayList = null;
    private static String _fileName;
    private static File _file ;
	public static void writeToFile(String fileName,String value){
		_fileName = fileName;
		_file = IngogoApp.getSharedApplication().getFileStreamPath(_fileName);
		if(_file.exists()) {
			_arrayList = readFromFile(_fileName);
			for(int i=0;i<_arrayList.size();i++) {
				if(_arrayList.get(i).equals(value)){
					return;
				}
				
			}
			if(_arrayList.size() == 2){
				_arrayList.remove(0);
				_arrayList.add(value);
			}else{
				_arrayList.add(value);
			}
			write();
			
			
		}else {
			_arrayList = new ArrayList<String>();
			_arrayList.add(value);
			write();
		}
		
		
	}
	
	private static void write() {
		FileOutputStream fos = null;
		ObjectOutputStream os = null;
		try {
			fos = new FileOutputStream(_file);
			
			os = new ObjectOutputStream(fos);
			os.writeObject(_arrayList);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static ArrayList<String> readFromFile(String fileName){
		_fileName = fileName;
		_file = IngogoApp.getSharedApplication().getFileStreamPath(_fileName);
		ArrayList<String> arrayList = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(_file);
			try {
				ObjectInputStream is = new ObjectInputStream(fis);
				try {
					arrayList = (ArrayList<String>) is.readObject();
					is.close();
				} catch (ClassNotFoundException e) {
					return null;
				}
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			return null;
		}
		return arrayList;
		
	}

}
